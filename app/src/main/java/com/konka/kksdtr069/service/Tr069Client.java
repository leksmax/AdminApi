package com.konka.kksdtr069.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

import com.konka.amlogicmiddleware.EthUtil;
import com.konka.kksdtr069.base.BaseReceiver;
import com.konka.kksdtr069.util.DataBaseUtils;
import com.konka.kksdtr069.util.LogUtil;
import com.konka.kksdtr069.util.Utils;

import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPParameter;
import net.sunniwell.cwmp.protocol.sdk.aidl.ICWMPProtocolService;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class Tr069Client extends Service {

    private static final String TAG = "Tr069_Client";

    private static final String ETHERNET_CONN_MODE_DHCP = "DHCP";
    private static final String ETHERNET_CONN_MODE_MANUAL = "Static";
    private static final String ETHERNET_CONN_MODE_PPPOE = "PPPoE";
    private static final String ETHERNET_CONN_MODE_IPOE = "IPoE";
    private static final String WLAN_CONN_MODE_WIFI = "WIFI";
    private String defaultMode = "";

    private ICWMPProtocolService mProtocolService;
    private List<CWMPParameter> parameterCacheList;// 需要上报的参数缓存

    private DataBaseUtils dbUtils;

    private NetBroadcastReceiver mNetReceiver;

    // 绑定服务回调接口实现
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtil.i(TAG, "onServiceConnected");
            if (service == null) {
                LogUtil.w(TAG, "onServiceConnected Binder is null!");
                // 获取不到 service，3秒后重试
                Utils.ThreadSleep(3000);
                bindCWMPService();
                return;
            }
            mProtocolService = ICWMPProtocolService.Stub.asInterface(service);// 获取到朝歌中间件协议服务对象
            LogUtil.d(TAG, "ICWMPProtocolService connect successfully");
            Utils.ThreadSleep(2000);// 加入延时，解决偶发mProtocolService方法无法调用问题
            try {
                // 提供本地接口服务对象给朝歌中间件
                mProtocolService.setNativeService(new CWMPService(getApplication(), mProtocolService));
                mProtocolService.onBoot();// 通知朝歌中间件启动完成
                LogUtil.i(TAG, "ProtocolService boot finished.");
                updateNet(getApplicationContext());// 每次启动更新网络类型和IP地址
            } catch ( RemoteException e ) {
                LogUtil.e(TAG, "ICWMPProtocolService error");
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtil.w(TAG, "onServiceDisconnected");
            mProtocolService = null;
            Utils.ThreadSleep(3000);
            bindCWMPService();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.i(TAG, "TR069_Tr069Client start.");
        init();
    }

    public void init() {
        dbUtils = DataBaseUtils.getInstance(getApplicationContext());
        parameterCacheList = new ArrayList<CWMPParameter>();
        bindCWMPService();
        registerNetReceiver();
    }

    /**
     * 绑定朝歌中间件
     */
    private void bindCWMPService() {
        LogUtil.i(TAG, "bindCWMPService");
        Intent service = new Intent("net.sunniwell.action.START_CWMP_SERVICE");
        bindService(service, mConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * 注册网络变化广播接收器
     */
    private void registerNetReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mNetReceiver = new NetBroadcastReceiver();
        registerReceiver(mNetReceiver, filter);
    }

    /**
     * 更新数据库中的网络类型和IP地址，并上报到网管平台
     *
     * @param context 传入上下文对象
     */
    private void updateNet(Context context) throws RemoteException {
        LogUtil.i(TAG, "updateNet");
        String netMode;// 网络类型
        String ipAddress;// 当前IP地址
        String oldIpAddress;// 数据库中保存的旧IP

        ipAddress = getClientIpAddress();
        netMode = getNetMode(context);

        // 若当前网络未连接，直接return,不需要更新数据库和上报
        if (TextUtils.isEmpty(netMode) || TextUtils.isEmpty(ipAddress)) {
            LogUtil.i(TAG, "updateNet: network is disconnect");
            return;
        }

        // 因为底层无法有效区分IPoE和DHCP协议，故只要IP是“192.”开头的，都认为是走DHCP协议
        // IPv6环境下需注意修改
        if (ETHERNET_CONN_MODE_IPOE.equals(netMode) && ipAddress.startsWith("192.")) {
            netMode = ETHERNET_CONN_MODE_DHCP;
        }

        LogUtil.i(TAG, String.format("updateNet: ipAddress = %s,netMode = %s", ipAddress, netMode));

        boolean isNetModeChanged = dbUtils.isDifferentFromDB(netMode, "Device.LAN.AddressingType");
        boolean isIpAddressChanged = dbUtils.isDifferentFromDB(ipAddress, "Device.LAN.IPAddress");

        if (isNetModeChanged) {// 网络类型发生变化
            dbUtils.updateDb("Device.LAN.AddressingType", netMode);
            if ("PPPoE".equals(netMode)) {// 若当前是PPPoE网络，将PPPoE账号参数加入上报缓存
                CWMPParameter pppoeParameter = dbUtils.cursorToParameter(
                        dbUtils.query("Device.X_CMCC_OTV.ServiceInfo.PPPoEID"));
                LogUtil.i(TAG, "updateNet: PPPoE id is" + pppoeParameter.getValue());
                parameterCacheList.add(pppoeParameter);
            }
            // 将网络类型参数加入上报缓存
            CWMPParameter netParameter = dbUtils.cursorToParameter(dbUtils.query("Device.LAN.AddressingType"));
            parameterCacheList.add(netParameter);
        }

        if (isIpAddressChanged) {// IP地址发生变化
            oldIpAddress = dbUtils.queryValue("Device.LAN.IPAddress");
            dbUtils.updateDb("Device.LAN.IPAddress", ipAddress);
            CWMPParameter netParameter = dbUtils.cursorToParameter(dbUtils.query("Device.LAN.IPAddress"));
            parameterCacheList.add(netParameter);
            mProtocolService.onNetworkChanged(ipAddress, oldIpAddress);// 上报新旧IP
            LogUtil.i(TAG, "updateNet: onNetworkChanged");
        }

        if (!parameterCacheList.isEmpty()) {// 如果参数上报缓存不为空，向平台上报
            mProtocolService.onValueChange(parameterCacheList);
            LogUtil.i(TAG, "updateNet: report parameters in parameterCacheList.");
            parameterCacheList.clear();
        }
    }

    /**
     * 获取当前使用的网络协议类型
     *
     * @param context 上下文对象
     * @return ethMode 网络协议类型，若无网络连接，返回空字符串
     */
    private String getNetMode(Context context) {
        String ethMode;
        int mode = EthUtil.getInstance(context).EthUtil_GetEthMode();
        switch (mode) {
            case EthUtil.ETH_MODE_DHCP:
                ethMode = ETHERNET_CONN_MODE_DHCP;
                break;

            case EthUtil.ETH_MODE_MANUAL:
                ethMode = ETHERNET_CONN_MODE_MANUAL;
                break;

            case EthUtil.ETH_MODE_PPPOE:
                ethMode = ETHERNET_CONN_MODE_PPPOE;
                break;

            case EthUtil.ETH_MODE_IPOE:
                ethMode = ETHERNET_CONN_MODE_IPOE;
                break;

            default:
                ethMode = defaultMode;
                break;
        }
        return ethMode;
    }

    /**
     * 根据使用的网络协议返回有线或无线IP地址
     *
     * @return 若无网络连接，返回空字符串
     */
    private String getClientIpAddress() {
        try {
            // 获取本地设备的所有网络接口
            Enumeration<NetworkInterface> enumerationNi = NetworkInterface.getNetworkInterfaces();
            String ethHostAddress = "";
            String wlan0HostAddress = "";
            defaultMode = "";
            while (enumerationNi.hasMoreElements()) {
                NetworkInterface networkInterface = enumerationNi.nextElement();
                String interfaceName = networkInterface.getDisplayName();
                LogUtil.i(TAG, "网络名字" + interfaceName);

                if ("ppp0".equals(interfaceName)) {// PPPoE协议
                    // 更新数据库中的PPPoE账号，将参数加入上报缓存
                    String pppoeAcount = Utils.getProperty("persist.sys.pppoeaccount", "Unknow");
                    dbUtils.updateDb("Device.X_CMCC_OTV.ServiceInfo.PPPoEID", pppoeAcount);
                    ethHostAddress = getIP(networkInterface);
                    break;// 测试发现DHCP切换成PPPoE会有两个ip，多一个eth0的，但是会先扫描到ppp0，获取到IP地址后直接跳出while循环
                } else if ("eth0".equals(interfaceName)) {// DHCP或IPoE协议
                    ethHostAddress = getIP(networkInterface);
                } else if ("wlan0".equals(interfaceName)) {// Wlan协议
                    defaultMode = WLAN_CONN_MODE_WIFI;
                    wlan0HostAddress = getIP(networkInterface);
                }
            }

            if (!TextUtils.isEmpty(ethHostAddress)) {
                return ethHostAddress;
            } else {
                return wlan0HostAddress;
            }
        } catch ( SocketException e ) {
            LogUtil.e(TAG, "getClientIpAddress: something made mistake." );
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 获取当前网络接口的IP地址，仅支持IPv4协议
     *
     * @param networkInterface 当前网络接口
     * @return IP              字符串类型的IPv4地址
     */
    private String getIP(NetworkInterface networkInterface) {
        Enumeration<InetAddress> enumIpAddr = networkInterface.getInetAddresses();
        String IP = "";

        while (enumIpAddr.hasMoreElements()) {
            // 返回枚举集合中的下一个IP地址信息
            InetAddress inetAddress = enumIpAddr.nextElement();
            // 不是回环地址，并且是ipv4的地址
            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                IP = inetAddress.getHostAddress();
            }
        }
        return IP;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        LogUtil.i(TAG, "TR069_Tr069Client destroy.");
        unregisterReceiver(mNetReceiver);
        super.onDestroy();
    }

    class NetBroadcastReceiver extends BaseReceiver {

        public NetBroadcastReceiver() {
            super();
            LogUtil.d(TAG, "NetBroadcastReceiver: create success");
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            super.onReceive(context, intent);
            LogUtil.i(TAG, "onReceive: Net status changed.");
            try {
                updateNet(getApplicationContext());// 当网络发生变化时，更新网络类型和IP地址
            } catch ( RemoteException e ) {
                e.printStackTrace();
            }
        }
    }
}
