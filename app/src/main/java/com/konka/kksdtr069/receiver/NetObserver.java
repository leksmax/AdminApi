package com.konka.kksdtr069.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.RemoteException;

import com.konka.kksdtr069.base.BaseApplication;
import com.konka.kksdtr069.base.BaseObserver;
import com.konka.kksdtr069.handler.impl.NetworkHandlerImpl;
import com.konka.kksdtr069.observer.ProtocolObserver;

import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPParameter;

import java.util.ArrayList;

public class NetObserver extends BaseObserver {

    private Context context;

    private static NetObserver instance;

    private NetReceiver mNetReceiver;

    private NetObserver() {
        context = BaseApplication.instance.getApplicationContext();
    }

    public static NetObserver getInstance() {
        if (instance == null) {
            instance = new NetObserver();
        }
        return instance;
    }

    public void registerNetReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mNetReceiver = new NetReceiver(NetworkHandlerImpl.getInstance());
        context.registerReceiver(mNetReceiver, filter);
    }

    public void unregisterNetReceiver() {
        if (mNetReceiver != null) {
            context.unregisterReceiver(mNetReceiver);
        }
    }

    private class NetReceiver extends BroadcastReceiver {

        private NetworkHandlerImpl mNetworkHandler;

        NetReceiver(NetworkHandlerImpl networkHandler) {
            mNetworkHandler = networkHandler;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                // 当网络发生变化时，更新网络类型和IP地址
                ArrayList<CWMPParameter> parameterCacheList = new ArrayList<CWMPParameter>();
                ProtocolObserver protocolObserver = ProtocolObserver.getInstance();
                mNetworkHandler.updateNetwork(parameterCacheList, protocolObserver);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
