package com.konka.kksdtr069.client;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import com.konka.kksdtr069.handler.DBHandler;
import com.konka.kksdtr069.handler.impl.DBHandlerImpl;
import com.konka.kksdtr069.handler.impl.NetworkHandlerImpl;
import com.konka.kksdtr069.receiver.NetObserver;
import com.konka.kksdtr069.service.CWMPService;
import com.konka.kksdtr069.util.PropertyUtil;

import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPParameter;
import net.sunniwell.cwmp.protocol.sdk.aidl.ICWMPProtocolService;

import java.util.ArrayList;
import java.util.List;

public class Tr069Client extends Service {

    private static final String TAG = Tr069Client.class.getSimpleName();

    private List<CWMPParameter> parameterCacheList;

    private ICWMPProtocolService mProtocolService;

    private NetworkHandlerImpl networkHandler;

    private NetObserver netObserver;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, final IBinder service) {
            Log.d(TAG, "onServiceConnected()");
            if (service == null) {
                Log.d(TAG, "onServiceConnected() Binder is null!");
                // 获取不到 service，3秒后重试
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                bindCWMPService();
                return;
            }
            mProtocolService = ICWMPProtocolService.Stub.asInterface(service);
            Log.d(TAG, "onServiceConnected() ICWMPProtocolService connect successfully," +
                    "service = " + service);
            try {
                initNativeService();
                // 每次启动更新网络类型和IP地址
                networkHandler.updateNetwork(parameterCacheList, mProtocolService);
            } catch (RemoteException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected()");
            release();
            bindCWMPService();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate() TR069_Tr069Client start init.");
        init();
    }

    private void init() {
        formatSoftwareVersion();
        parameterCacheList = new ArrayList<>();
        networkHandler = NetworkHandlerImpl.getInstance();
        bindCWMPService();
        netObserver = NetObserver.getInstance(mProtocolService);
        netObserver.registerNetReceiver();
    }

    private void formatSoftwareVersion() {
        DBHandler dbHandler = DBHandlerImpl.getInstance();
        String sfversion = PropertyUtil.getProperty("ro.build.version.incremental");
        sfversion = PropertyUtil.formatSoftwareVersion(sfversion);
        try {
            dbHandler.update("Device.DeviceInfo.SoftwareVersion", sfversion);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void bindCWMPService() {
        Log.d(TAG, "bindCWMPService() bind cwmp service");
        Intent service = new Intent("net.sunniwell.action.START_CWMP_SERVICE");
        getApplication().bindService(service, mConnection, Context.BIND_AUTO_CREATE);
    }

    private void initNativeService() throws RemoteException, InterruptedException {
        Thread.sleep(2 * 1000);
        // 提供本地接口服务对象给朝歌中间件
        mProtocolService.setNativeService(new CWMPService(mProtocolService));
        // 通知朝歌中间件启动完成
        mProtocolService.onBoot();
        Log.d(TAG, "onServiceConnected() ProtocolService boot " +
                "and init native service finished.");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy() TR069_Tr069Client destroy.");
        release();
        super.onDestroy();
    }

    private void release() {
        netObserver.unregisterNetReceiver();
        if (mProtocolService != null) {
            mProtocolService = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
