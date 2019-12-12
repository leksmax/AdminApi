package com.konka.kksdtr069.observer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.OnLifecycleEvent;

import com.konka.kksdtr069.base.BaseApplication;
import com.konka.kksdtr069.base.BaseObserver;
import com.konka.kksdtr069.handler.impl.NetworkHandlerImpl;
import com.konka.kksdtr069.service.CWMPService;
import com.konka.kksdtr069.util.LogUtils;

import net.sunniwell.cwmp.protocol.sdk.aidl.AppID;
import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPParameter;
import net.sunniwell.cwmp.protocol.sdk.aidl.ICWMPProtocolService;

import java.lang.ref.WeakReference;
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

    private WeakReference<NetworkHandlerImpl> networkHandler =
            new WeakReference<NetworkHandlerImpl>(NetworkHandlerImpl.getInstance());

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
                networkHandler.get().updateNetwork(parameterCacheList);
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
            if (mProtocolService == null) return;
            mProtocolService.asBinder().unlinkToDeath(mDeathRecipient, 0);
            mProtocolService = null;
        }
    };

    private ProtocolObserver() {
        this.context = BaseApplication.getInstance().getApplicationContext();
        this.parameterCacheList = new ArrayList<CWMPParameter>();
    }

    public static ProtocolObserver getInstance() {
        if (instance == null) {
            synchronized (ProtocolObserver.class) {
                if (instance == null) {
                    instance = new ProtocolObserver();
                }
            }
        }
        return instance;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
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
                    service.linkToDeath(mDeathRecipient, 0);
                    LogUtils.d(TAG, "ICWMPProtocolService connect successfully");
                }
            }
        };
    }

    public void initNativeService() throws RemoteException {
        // 提供本地接口服务对象给朝歌中间件
        mProtocolService.setNativeService(new CWMPService());
        // 通知朝歌中间件启动完成
        mProtocolService.onBoot();
        LogUtils.i(TAG, "ProtocolService boot finished.");
    }

    public void networkChanged(String ipAddress, String oldIpAddress) throws RemoteException {
        mProtocolService.onNetworkChanged(ipAddress, oldIpAddress);
    }

    public void valueChanged(List<CWMPParameter> parameterCacheList) throws RemoteException {
        mProtocolService.onValueChange(parameterCacheList);
    }

    public void diagnosisFinish() throws RemoteException {
        mProtocolService.onDiagnosisFinish();

    }

    public void uninstallFinish(List<AppID> list) throws RemoteException {
        mProtocolService.onUninstallFinish(list);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void release() {
        if (mProtocolService != null) {
            mProtocolService = null;
        }
    }
}
