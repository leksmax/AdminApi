package com.konka.kksdtr069.handler.impl;

import android.content.Context;
import android.os.RemoteException;
import android.text.TextUtils;

import com.konka.amlogicmiddleware.EthUtil;
import com.konka.kksdtr069.base.BaseApplication;
import com.konka.kksdtr069.handler.NetworkHandler;
import com.konka.kksdtr069.util.LogUtil;
import com.konka.kksdtr069.util.PropertyUtil;

import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPParameter;
import net.sunniwell.cwmp.protocol.sdk.aidl.ICWMPProtocolService;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

public class NetworkHandlerImpl implements NetworkHandler {

    private static NetworkHandlerImpl instance;

    private Context context;

    private DBHandlerImpl dbHandler;

    private static final String ETHERNET_CONN_MODE_DHCP = "DHCP";

    private static final String ETHERNET_CONN_MODE_MANUAL = "Static";

    private static final String ETHERNET_CONN_MODE_PPPOE = "PPPoE";

    private static final String ETHERNET_CONN_MODE_IPOE = "IPoE";

    private static final String WLAN_CONN_MODE_WIFI = "WIFI";

    private String defaultMode;

    public static final String TAG = NetworkHandlerImpl.class.getSimpleName();

    private NetworkHandlerImpl() {
        this.context = BaseApplication.instance.getApplicationContext();
        this.dbHandler = DBHandlerImpl.getInstance();
        LogUtil.d(TAG, "new DBhandlerImpl for NetworkHandlerImpl");
    }

    public static NetworkHandlerImpl getInstance() {
        if (instance == null) {
            instance = new NetworkHandlerImpl();
        }
        return instance;
    }

    @Override
    public void updateNetwork(List<CWMPParameter> parameterCacheList,
                              ICWMPProtocolService protocolService) throws RemoteException {
        LogUtil.i(TAG, "updateNet");
        String netMode;// 网络类型
        String ipAddress;// 当前IP地址
        String oldIpAddress;// 数据库中保存的旧IP

        ipAddress = getClientIpAddress();
        netMode = getNetMode();

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

        boolean isNetModeChanged = dbHandler.isDifferentFromDB(netMode,
                "Device.LAN.AddressingType");
        boolean isIpAddressChanged = dbHandler.isDifferentFromDB(ipAddress,
                "Device.LAN.IPAddress");

        if (isNetModeChanged) {
            // 网络类型发生变化
            dbHandler.update("Device.LAN.AddressingType", netMode);
            if ("PPPoE".equals(netMode)) {
                // 若当前是PPPoE网络，将PPPoE账号参数加入上报缓存
                CWMPParameter pppoeParameter = dbHandler.queryByName("Device.X_CMCC_OTV." +
                        "ServiceInfo.PPPoEID");
                LogUtil.i(TAG, "updateNet: PPPoE id is" + pppoeParameter.getValue());
                parameterCacheList.add(pppoeParameter);
            }
            // 将网络类型参数加入上报缓存
            CWMPParameter netParameter = dbHandler.queryByName("Device.LAN.AddressingType");
            parameterCacheList.add(netParameter);
        }

        if (isIpAddressChanged) {
            // IP地址发生变化
            oldIpAddress = dbHandler.queryByNameForString("Device.LAN.IPAddress");
            dbHandler.update("Device.LAN.IPAddress", ipAddress);
            CWMPParameter netParameter = dbHandler.queryByName("Device.LAN.IPAddress");
            parameterCacheList.add(netParameter);
            // 上报新旧IP
            protocolService.onNetworkChanged(ipAddress, oldIpAddress);
            LogUtil.i(TAG, "updateNet: onNetworkChanged");
        }

        if (!parameterCacheList.isEmpty()) {
            // 如果参数上报缓存不为空，向平台上报
            protocolService.onValueChange(parameterCacheList);
            LogUtil.i(TAG, "updateNet: report parameters in parameterCacheList.");
            parameterCacheList.clear();
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        dbHandler.update("Device.DeviceInfo.UpTime", format.format(new Date()));
    }

    /**
     * 获取当前使用的网络协议类型
     *
     * @return ethMode 网络协议类型，若无网络连接，返回空字符串
     */
    public String getNetMode() {
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
    public String getClientIpAddress() {
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

                if ("ppp0".equals(interfaceName)) {
                    // PPPoE协议
                    // 更新数据库中的PPPoE账号，将参数加入上报缓存
                    String pppoeAcount = PropertyUtil.getProperty("persist.sys.pppoeaccount",
                            "Unknow");
                    dbHandler.update("Device.X_CMCC_OTV.ServiceInfo.PPPoEID",
                            pppoeAcount);
                    ethHostAddress = getIP(networkInterface);
                    /*测试发现DHCP切换成PPPoE会有两个ip，多一个eth0的，但是会先扫描到ppp0，
                    获取到IP地址后直接跳出while循环*/
                    break;
                } else if ("eth0".equals(interfaceName)) {
                    // DHCP或IPoE协议
                    ethHostAddress = getIP(networkInterface);
                } else if ("wlan0".equals(interfaceName)) {
                    // Wlan协议
                    defaultMode = WLAN_CONN_MODE_WIFI;
                    wlan0HostAddress = getIP(networkInterface);
                }
            }

            if (!TextUtils.isEmpty(ethHostAddress)) {
                return ethHostAddress;
            } else {
                return wlan0HostAddress;
            }
        } catch (SocketException e) {
            LogUtil.e(TAG, "getClientIpAddress: something made mistake.");
            e.printStackTrace();
            return "";
        } catch (RemoteException e) {
            LogUtil.e(TAG, "getClientIpAddress: something made mistake.");
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
}
