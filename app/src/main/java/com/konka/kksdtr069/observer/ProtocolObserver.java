package com.konka.kksdtr069.observer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.konka.kksdtr069.base.BaseApplication;
import com.konka.kksdtr069.base.BaseObserver;
import com.konka.kksdtr069.handler.impl.NetworkHandlerImpl;
import com.konka.kksdtr069.service.CWMPService;
import com.konka.kksdtr069.util.LogUtils;

import net.sunniwell.cwmp.protocol.sdk.aidl.AppID;
import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPParameter;
import net.sunniwell.cwmp.protocol.sdk.aidl.ICWMPProtocolService;

import java.util.ArrayList;
import java.util.List;

public class ProtocolObserver{

    private static ProtocolObserver instance;

    private Context context;

    private ICWMPProtocolService mProtocolService;

    private List<CWMPParameter> parameterCacheList;

    public static final String TAG = ProtocolObserver.class.getSimpleName();

    public ProtocolObserver(ICWMPProtocolService protocolService) {
        this.context = BaseApplication.instance.getApplicationContext();
        this.parameterCacheList = new ArrayList<CWMPParameter>();
        this.mProtocolService = protocolService;
    }

    private ProtocolObserver() {
        this.context = BaseApplication.instance.getApplicationContext();
        this.parameterCacheList = new ArrayList<CWMPParameter>();
    }

    public static ProtocolObserver getInstance() {
        if (instance == null) {
            instance = new ProtocolObserver();
        }
        return instance;
    }

    public void networkChanged(final String ipAddress, final String oldIpAddress) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    mProtocolService.onNetworkChanged(ipAddress, oldIpAddress);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void valueChanged(final List<CWMPParameter> parameterCacheList) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    mProtocolService.onValueChange(parameterCacheList);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void diagnosisFinish() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    mProtocolService.onDiagnosisFinish();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void uninstallFinish(final List<AppID> list) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    mProtocolService.onUninstallFinish(list);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
