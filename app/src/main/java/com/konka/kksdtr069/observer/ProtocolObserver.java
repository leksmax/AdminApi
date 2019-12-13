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

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class ProtocolObserver extends BaseObserver {

    private static ProtocolObserver instance;

    private Context context;

    private ICWMPProtocolService mProtocolService;

    private Disposable mProtocolDisposable;

    private NetworkHandlerImpl networkHandler = NetworkHandlerImpl.getInstance();

    private List<CWMPParameter> parameterCacheList;

    public static final String TAG = ProtocolObserver.class.getSimpleName();

    private Consumer<Boolean> bindCWMPServiceConsumer;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, final IBinder service) {
            LogUtils.i(TAG, "onServiceConnected");
            bindCWMPServiceConsumer = bindCWMPServiceConsumer(service);
            try {
                initNativeService();
                // 每次启动更新网络类型和IP地址
                networkHandler.updateNetwork(parameterCacheList);
            } catch (RemoteException e) {
                LogUtils.e(TAG, "ICWMPProtocolService error");
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtils.w(TAG, "onServiceDisconnected");
            release();
            bindCWMPServiceObserver(mConnection, bindCWMPServiceConsumer);
        }
    };

    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            if (mProtocolService == null) {
                Intent service = new Intent("net.sunniwell.action.START_CWMP_SERVICE");
                context.bindService(service, mConnection, Context.BIND_AUTO_CREATE);
                LogUtils.d(TAG, "[binderDied] service has been reconnected");
                return;
            }
            mProtocolService.asBinder().unlinkToDeath(mDeathRecipient, 0);
        }
    };

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

    public void initCWMPService() {
        bindCWMPServiceObserver(mConnection, bindCWMPServiceConsumer);
    }

    public void bindCWMPServiceObserver(final ServiceConnection mConnection, Consumer consumer) {
        if (mProtocolDisposable != null && !mProtocolDisposable.isDisposed()) {
            removeObserver(mProtocolDisposable);
        }
        mProtocolDisposable = Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
                LogUtils.i(TAG, "bindCWMPService");
                Intent service = new Intent("net.sunniwell.action.START_CWMP_SERVICE");
                emitter.onNext(context.bindService(service, mConnection, Context.BIND_AUTO_CREATE));
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(consumer);
        addObserver(mProtocolDisposable);
    }

    public Consumer<Boolean> bindCWMPServiceConsumer(final IBinder service) {
        return new Consumer<Boolean>() {
            @Override
            public void accept(Boolean bindServiceResult) throws Exception {
                if (bindServiceResult) {
                    mProtocolService = ICWMPProtocolService.Stub.asInterface(service);
                    LogUtils.d(TAG, "ICWMPProtocolService connect successfully," +
                            "service = " + service);
                    mProtocolService.asBinder().linkToDeath(mDeathRecipient, 0);
                } else {
                    Intent service = new Intent("net.sunniwell.action.START_CWMP_SERVICE");
                    context.bindService(service, mConnection, Context.BIND_AUTO_CREATE);
                    LogUtils.d(TAG, "[bindCWMPServiceConsumer] service has been reconnected,"
                            + "service = " + service);
                }
            }
        };
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
                    LogUtils.i(TAG, "ProtocolService boot finished.");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }.start();
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


    public void release() {
        if (mProtocolService != null) {
            mProtocolService = null;
        }
    }
}
