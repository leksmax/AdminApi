package com.konka.kksdtr069.observer;

import android.content.Context;
import android.os.RemoteException;

import com.konka.kksdtr069.base.BaseApplication;
import com.konka.kksdtr069.base.BaseObserver;
import com.konka.kksdtr069.handler.impl.DBHandlerImpl;
import com.konka.kksdtr069.util.LinuxUtils;
import com.konka.kksdtr069.util.LogUtils;

import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPPingRequest;
import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPPingResult;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class FunctionObserver extends BaseObserver {

    private static FunctionObserver instance;

    private Disposable mPingDisposable;

    private DBHandlerImpl dbHandler;

    private Context context;

    public static final String TAG = FunctionObserver.class.getSimpleName();

    private FunctionObserver() {
        dbHandler = DBHandlerImpl.getInstance();
        context = BaseApplication.instance.getApplicationContext();
    }

    public static FunctionObserver getInstance() {
        if (instance == null) {
            instance = new FunctionObserver();
        }
        return instance;
    }

    public void observerPing(final CWMPPingRequest request) {
        if (mPingDisposable != null && !mPingDisposable.isDisposed()) {
            removeObserver(mPingDisposable);
        }
        mPingDisposable = Observable.create(new ObservableOnSubscribe<CWMPPingResult>() {
            @Override
            public void subscribe(ObservableEmitter<CWMPPingResult> emitter) throws Exception {
                CWMPPingResult pingResult = LinuxUtils.ping(request);
                emitter.onNext(pingResult);
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<CWMPPingResult>() {
                    @Override
                    public void accept(CWMPPingResult pingResult) throws RemoteException {
                        LogUtils.d(TAG, "ping result : " + "\n"
                                + "MaximumResponseTime = " + pingResult.getMaximumResponseTime() + "\n"
                                + "MinimumResponseTime = " + pingResult.getMinimumResponseTime() + "\n"
                                + "AverageResponseTime = " + pingResult.getAverageResponseTime() + "\n"
                                + "FailureCount = " + pingResult.getFailureCount() + "\n"
                                + "SuccessCount = " + pingResult.getSuccessCount() + "\n"
                                + "DiagnosticsState = " + pingResult.getDiagnosticsState() + "\n");

                        dbHandler.update(
                                "Device.LAN.IPPingDiagnostics.MaximumResponseTime",
                                pingResult.getMaximumResponseTime() + "");
                        dbHandler.update(
                                "Device.LAN.IPPingDiagnostics.MinimumResponseTime",
                                pingResult.getMinimumResponseTime() + "");
                        dbHandler.update(
                                "Device.LAN.IPPingDiagnostics.AverageResponseTime",
                                pingResult.getAverageResponseTime() + "");
                        dbHandler.update(
                                "Device.LAN.IPPingDiagnostics.FailureCount",
                                pingResult.getFailureCount() + "");
                        dbHandler.update(
                                "Device.LAN.IPPingDiagnostics.SuccessCount",
                                pingResult.getSuccessCount() + "");
                        dbHandler.update(
                                "Device.LAN.IPPingDiagnostics.DiagnosticsState",
                                pingResult.getDiagnosticsState());
                    }
                });
        addObserver(mPingDisposable);
    }

}
