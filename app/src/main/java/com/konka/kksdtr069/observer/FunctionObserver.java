package com.konka.kksdtr069.observer;

import android.content.ContentValues;
import android.os.Looper;
import android.os.RemoteException;

import com.konka.kksdtr069.base.BaseObserver;
import com.konka.kksdtr069.handler.impl.DBHandlerImpl;
import com.konka.kksdtr069.handler.impl.ParameterHandlerImpl;
import com.konka.kksdtr069.util.LinuxUtils;

import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPPingRequest;
import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPPingResult;
import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPTraceRouteRequest;
import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPTraceRouteResult;

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

    private Disposable mTraceRouteDisposable;

    private ParameterHandlerImpl parameterHandler = ParameterHandlerImpl.getInstance();

    private DBHandlerImpl dbHandler = DBHandlerImpl.getInstance();

    public static FunctionObserver getInstance() {
        if (instance == null) {
            instance = new FunctionObserver();
        }
        return instance;
    }

    public void observerPing(final CWMPPingRequest request) {
        if (mPingDisposable != null & !mPingDisposable.isDisposed()) {
            removeObserver(mPingDisposable);
        }
        mPingDisposable = Observable.create(new ObservableOnSubscribe<CWMPPingResult>() {
            @Override
            public void subscribe(ObservableEmitter<CWMPPingResult> emitter) throws Exception {
                CWMPPingResult pingResult = LinuxUtils.ping(request);
                emitter.onNext(pingResult);
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.from(Looper.myLooper()))
                .subscribe(new Consumer<CWMPPingResult>() {
                    @Override
                    public void accept(CWMPPingResult pingResult) throws RemoteException {
                        parameterHandler.setParameterValue(
                                "Device.LAN.IPPingDiagnostics.MaximumResponseTime",
                                pingResult.getMaximumResponseTime() + "");
                        parameterHandler.setParameterValue(
                                "Device.LAN.IPPingDiagnostics.MinimumResponseTime",
                                pingResult.getMinimumResponseTime() + "");
                        parameterHandler.setParameterValue(
                                "Device.LAN.IPPingDiagnostics.AverageResponseTime",
                                pingResult.getAverageResponseTime() + "");
                        parameterHandler.setParameterValue(
                                "Device.LAN.IPPingDiagnostics.FailureCount",
                                pingResult.getFailureCount() + "");
                        parameterHandler.setParameterValue(
                                "Device.LAN.IPPingDiagnostics.SuccessCount",
                                pingResult.getSuccessCount() + "");
                        parameterHandler.setParameterValue(
                                "Device.LAN.IPPingDiagnostics.DiagnosticsState",
                                pingResult.getDiagnosticsState());
                    }
                });
        addObserver(mPingDisposable);
    }

    public void observerTraceRoute(final CWMPTraceRouteRequest request) {
        if (mTraceRouteDisposable != null && !mTraceRouteDisposable.isDisposed()) {
            removeObserver(mTraceRouteDisposable);
        }
        mTraceRouteDisposable = Observable.create(new ObservableOnSubscribe<CWMPTraceRouteResult>() {
            @Override
            public void subscribe(ObservableEmitter<CWMPTraceRouteResult> emitter)
                    throws Exception {
                CWMPTraceRouteResult traceResult = LinuxUtils.traceRoute(request);
                emitter.onNext(traceResult);
                emitter.onComplete();

            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.from(Looper.myLooper()))
                .subscribe(new Consumer<CWMPTraceRouteResult>() {
                    @Override
                    public void accept(CWMPTraceRouteResult traceResult) throws RemoteException {
                        parameterHandler.setParameterValue(
                                "Device.LAN.TraceRouteDiagnostics.MaximumResponseTime",
                                traceResult.getMaximumResponseTime() + "");
                        parameterHandler.setParameterValue(
                                "Device.LAN.TraceRouteDiagnostics.MinimumResponseTime",
                                traceResult.getMinimumResponseTime() + "");
                        parameterHandler.setParameterValue(
                                "Device.LAN.TraceRouteDiagnostics.AverageResponseTime",
                                traceResult.getAverageResponseTime() + "");
                        parameterHandler.setParameterValue(
                                "Device.LAN.TraceRouteDiagnostics.FailureCount",
                                traceResult.getFailureCount() + "");
                        parameterHandler.setParameterValue(
                                "Device.LAN.TraceRouteDiagnostics.ResponseTime",
                                traceResult.getResponseTime() + "");
                        parameterHandler.setParameterValue(
                                "Device.LAN.TraceRouteDiagnostics.DiagnosticsState",
                                traceResult.getDiagnosticsState());
                        parameterHandler.setParameterValue(
                                "Device.LAN.TraceRouteDiagnostics.NumberOfRouteHops",
                                traceResult.getNumberOfRouteHops() + "");
                        dbHandler.delete("TraceRouteDiagnostics.RouteHops");
                        for (int i = 0; i < traceResult.getRouteHops().size(); i++) {
                            ContentValues cv = new ContentValues();
                            cv.put(DBHandlerImpl.COLUMN_NAME,
                                    String.format("Device.LAN.TraceRouteDiagnostics." +
                                            "RouteHops.%s.HopHost", i + 1));
                            cv.put(DBHandlerImpl.COLUMN_VALUE, traceResult.getRouteHops().get(i));
                            cv.put(DBHandlerImpl.COLUMN_TYPE, "string(256)");
                            cv.put(DBHandlerImpl.COLUMN_WRITABLE, "0");
                            cv.put(DBHandlerImpl.COLUMN_SECURE, "0");
                            cv.put(DBHandlerImpl.COLUMN_NOTIFICATION, "0");
                        }
                    }
                });
        addObserver(mTraceRouteDisposable);
    }
}
