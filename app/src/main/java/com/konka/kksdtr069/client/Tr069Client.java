package com.konka.kksdtr069.client;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.konka.kksdtr069.handler.impl.NetworkHandlerImpl;
import com.konka.kksdtr069.observer.DBObserver;
import com.konka.kksdtr069.observer.ProtocolObserver;
import com.konka.kksdtr069.receiver.NetObserver;
import com.konka.kksdtr069.service.CWMPService;
import com.konka.kksdtr069.util.LogUtils;

import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPParameter;
import net.sunniwell.cwmp.protocol.sdk.aidl.ICWMPProtocolService;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class Tr069Client extends Service {

    private static final String TAG = "Tr069_Client";

    private Consumer<Boolean> bindCWMPServiceConsumer;

    private List<CWMPParameter> parameterCacheList;

    private ICWMPProtocolService mProtocolService;

    private ProtocolObserver mProtocolObserver;

    private DBObserver dbObserver = DBObserver.getInstance();

    private NetworkHandlerImpl networkHandler = NetworkHandlerImpl.getInstance();

    private NetObserver netObserver = NetObserver.getInstance();

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, final IBinder service) {
            LogUtils.i(TAG, "onServiceConnected");
            if (service == null) {
                LogUtils.w(TAG, "onServiceConnected Binder is null!");
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
            LogUtils.d(TAG, "ICWMPProtocolService connect successfully," +
                    "service = " + service);
            try {
                initNativeService();
                // 每次启动更新网络类型和IP地址
                networkHandler.updateNetwork(parameterCacheList, mProtocolObserver);
            } catch (RemoteException e) {
                LogUtils.e(TAG, "ICWMPProtocolService error");
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtils.w(TAG, "onServiceDisconnected");
            release();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            bindCWMPService();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.i(TAG, "TR069_Tr069Client start.");
        init();
    }

    public void init() {
        bindCWMPService();
        netObserver.registerNetReceiver();
        dbObserver.registerDBObserver();
    }

    public void bindCWMPService() {
        LogUtils.i(TAG, "bindCWMPService");
        Intent service = new Intent("net.sunniwell.action.START_CWMP_SERVICE");
        bindService(service, mConnection, Context.BIND_AUTO_CREATE);
    }

    public void initNativeService() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    // 提供本地接口服务对象给朝歌中间件
                    mProtocolService.setNativeService(new CWMPService());
                    // 通知朝歌中间件启动完成
                    mProtocolService.onBoot();
                    mProtocolObserver = new ProtocolObserver(mProtocolService);
                    LogUtils.i(TAG, "ProtocolService boot finished.");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public void onDestroy() {
        LogUtils.i(TAG, "TR069_Tr069Client destroy.");
        release();
        super.onDestroy();
    }

    private void release() {
        dbObserver.unRegisterDBObserver();
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
