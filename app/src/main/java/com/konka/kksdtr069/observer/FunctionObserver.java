package com.konka.kksdtr069.observer;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.kkbasic.BasicMain;
import com.kkbasic.MsgCallBack;
import com.kkbasic.Tcpdump;
import com.konka.kksdtr069.base.BaseApplication;
import com.konka.kksdtr069.base.BaseObserver;
import com.konka.kksdtr069.handler.impl.DBHandlerImpl;
import com.konka.kksdtr069.handler.impl.ParameterHandlerImpl;
import com.konka.kksdtr069.model.PacketCaptureRequest;
import com.konka.kksdtr069.model.SysLog;
import com.konka.kksdtr069.model.SysLogRequest;
import com.konka.kksdtr069.util.LinuxUtils;
import com.konka.kksdtr069.util.LogUtils;
import com.konka.kksdtr069.util.NetMeasureUtils;
import com.konka.kksdtr069.util.SFTPUtils;
import com.konka.kksdtr069.util.UploadLogUtils;

import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPParameter;
import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPPingRequest;
import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPPingResult;
import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPTraceRouteRequest;
import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPTraceRouteResult;
import net.sunniwell.cwmp.protocol.sdk.aidl.SetParameterValuesFault;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

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

    private Disposable mCapturePackageDisposable;

    private Disposable mSpeedDisposable;

    private Disposable mWifiDisposable;

    private ParameterHandlerImpl parameterHandler = ParameterHandlerImpl.getInstance();

    private DBHandlerImpl dbHandler = DBHandlerImpl.getInstance();

    private DBObserver dbObserver = DBObserver.getInstance();

    private Context context = BaseApplication.instance.getApplicationContext();

    private int tcpdumpState = 1;

    private ProtocolObserver mprotocolObserver = ProtocolObserver.getInstance();

    public static final String TAG = FunctionObserver.class.getSimpleName();

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
                .observeOn(AndroidSchedulers.mainThread())
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
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
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

    public void observerCapturePackage(final PacketCaptureRequest request) {
        if (mCapturePackageDisposable != null & !mCapturePackageDisposable.isDisposed()) {
            removeObserver(mCapturePackageDisposable);
        }
        mCapturePackageDisposable = Observable.create(
                new ObservableOnSubscribe<PacketCaptureRequest>() {
                    @Override
                    public void subscribe(ObservableEmitter<PacketCaptureRequest> emitter)
                            throws Exception {
                        // 正在抓包
                        tcpdump(request);
                        // 下发新参数，删除之前的网络包，重新抓包
                        if (request.getState() == 3 && isNewPacketCaptureComm(request)) {
                            updatePacketCaptureComm(request);
                            LinuxUtils.removeSubFile("/data/data/com.konka" +
                                    ".kksdtr069/cache/pcap/");
                            tcpdump(request);
                        }
                        emitter.onNext(request);
                        emitter.onComplete();
                    }
                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<PacketCaptureRequest>() {
                    @Override
                    public void accept(final PacketCaptureRequest request) throws Exception {
                        final String packetIP;
                        final String packetPort;
                        final String packetUsername = request.getUsername();
                        final String packetPassword = request.getPassword();
                        if (TextUtils.isEmpty(request.getIp())) {
                            packetIP = request.getUploadURL()
                                    .split("//")[1]
                                    .split("/")[0]
                                    .split(":")[0];
                        } else {
                            packetIP = request.getIp();
                        }
                        if (request.getPort() == 0) {
                            packetPort = request.getUploadURL()
                                    .split("//")[1]
                                    .split("/")[0]
                                    .split(":")[1];
                        } else {
                            packetPort = request.getPort() + "";
                        }
                        final String uploadPath = request.getUploadURL()
                                .split("//")[1]
                                .substring(request.getUploadURL()
                                        .split("//")[1].indexOf("/"));
                        BasicMain basicMain = BasicMain.GetInstance();
                        basicMain.regMsgHandler(context.getMainLooper(), new MsgCallBack() {
                            @Override
                            public int tcpdumpFinish(int error) {
                                try {
                                    request.setState("5");
                                    dbHandler.update("Device.X_00E0FC.PacketCapture" +
                                            ".State", request.getState() + "");
                                    tcpdumpState = 1;
                                    SFTPUtils sftpUtils = new SFTPUtils(packetIP, packetPort,
                                            packetUsername, packetPassword);
                                    sftpUtils.connect();
                                    // 下发新参数，重新抓包
                                    if (request.getState() == 5
                                            && isNewPacketCaptureComm(request)) {
                                        tcpdumpState = 0;
                                        updatePacketCaptureComm(request);
                                        tcpdump(request);
                                    }
                                    // 上传网络包
                                    Boolean uploadResult = sftpUtils.uploadFile(uploadPath,
                                            request.getFileName(), request.getLocalPath(),
                                            request.getFileName());
                                    if (uploadResult) {
                                        dbHandler.update("Device.X_00E0FC.PacketCapture" +
                                                ".State", "6");
                                        dbHandler.update("Device.X_00E0FC.PacketCapture" +
                                                ".State", "1");
                                        sftpUtils.disconnect();
                                    } else {
                                        dbHandler.update("Device.X_00E0FC.PacketCapture" +
                                                ".State", "7");
                                    }
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                                return super.tcpdumpFinish(error);
                            }
                        });
                    }
                });
        addObserver(mCapturePackageDisposable);
    }

    public void speedMeasurement() {
        if (mSpeedDisposable != null && !mSpeedDisposable.isDisposed()) {
            removeObserver(mSpeedDisposable);
        }
        mSpeedDisposable = Observable.create(new ObservableOnSubscribe<NetMeasureUtils>() {
            @Override
            public void subscribe(ObservableEmitter<NetMeasureUtils> emitter) throws Exception {
                NetMeasureUtils netMeasureUtils = NetMeasureUtils.getInstance();
                netMeasureUtils.speedTest(context);
                emitter.onNext(netMeasureUtils);
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<NetMeasureUtils>() {
                    @Override
                    public void accept(NetMeasureUtils netMeasureUtils) throws Exception {
                        netMeasureUtils.setSpeedTestCompletedListener(
                                new NetMeasureUtils.SpeedTestCompletedListener() {
                                    @Override
                                    public void submitResult(boolean success) {
                                        Log.d(TAG, "Speed test: succeed");
                                        mprotocolObserver.diagnosisFinish();
                                    }
                                });
                    }
                });

        addObserver(mSpeedDisposable);
    }

    public void observerWifiEnable(final String isWifiEnable,
                                   final List<CWMPParameter> parameterCacheList,
                                   final List<SetParameterValuesFault> faultList) {
        if (mWifiDisposable != null && !mWifiDisposable.isDisposed()) {
            removeObserver(mWifiDisposable);
        }
        mWifiDisposable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                if (!TextUtils.isEmpty(isWifiEnable)) {
                    Intent intent = new Intent();
                    intent.setAction("com.android.settings");
                    if ("0".equals(isWifiEnable)) {
                        LogUtils.d(TAG, "set parameters : make wifi enable");
                        intent.putExtra("WifiEnable", true);
                    } else {
                        LogUtils.d(TAG, "set parameters : make wifi disable");
                        intent.putExtra("WifiEnable", false);
                    }
                    context.sendBroadcast(intent);
                    emitter.onNext(isWifiEnable);
                    emitter.onComplete();
                }
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String isWifiEnable) throws Exception {
                        if (!TextUtils.isEmpty(isWifiEnable)) {
                            WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                                    .getSystemService(Context.WIFI_SERVICE);
                            if (isWifiEnable(isWifiEnable, wifiManager)) {
                                /* 如果下发关闭wifi，但是由于用户正在使用关闭失败，移除参数改变上报,恢复打开状态 */
                                for (CWMPParameter parameter : parameterCacheList) {
                                    if (parameter.getName()
                                            .contains("Device.X_CMCC_OTV.ServiceInfo.WiFiEnable")) {
                                        parameterCacheList.remove(parameter);
                                        parameter.setValue("0");
                                        dbHandler.update(parameter);
                                    }
                                }
                                dbObserver.notifyChange(dbObserver, parameterCacheList);
                                SetParameterValuesFault fault = new SetParameterValuesFault();
                                fault.setFaultCode(9002);
                                fault.setFaultString("Closing wifi failure，wifi is using.");
                                fault.setParameterName("Device.X_CMCC_OTV.ServiceInfo.WiFiEnable");
                                faultList.add(fault);
                            }
                        }
                    }
                });

        addObserver(mWifiDisposable);

    }

    private boolean isWifiEnable(String wifiEnable, WifiManager wifiManager) {
        return (!"0".equals(wifiEnable)) && wifiManager.isWifiEnabled();

    }

    private boolean isNewPacketCaptureComm(PacketCaptureRequest request) throws RemoteException {
        String ip = dbHandler.queryByName("Device.X_00E0FC.PacketCapture.IP")
                .getValue();
        String port = dbHandler.queryByName("Device.X_00E0FC.PacketCapture.Port")
                .getValue();
        return !ip.equals(request.getIp()) || !port.equals(String.valueOf(request.getPort()));
    }

    private void updatePacketCaptureComm(PacketCaptureRequest request) throws RemoteException {
        String ip = dbHandler.queryByName("Device.X_00E0FC.PacketCapture.IP")
                .getValue();
        String port = dbHandler.queryByName("Device.X_00E0FC.PacketCapture.Port")
                .getValue();
        request.setIp(ip);
        request.setPort(port);
    }

    private void tcpdump(PacketCaptureRequest request) throws RemoteException {
        request.setState("3");
        dbHandler.update("Device.X_00E0FC.PacketCapture.State",
                request.getState() + "");
        Tcpdump tcpdump = Tcpdump.getInstance();
        if (tcpdumpState != 1) {
            tcpdump.stop();
        }
        tcpdumpState = 0;
        request.setFileName(LinuxUtils.getPacketCaptureName());
        request.setLocalPath("/data/data/com.konka.kksdtr069/cache/pcap/");
        String localPath = request.getLocalPath() + request.getFileName();
        if (TextUtils.isEmpty(request.getIp()) && request.getPort() == 0) {
            tcpdump.start(request.getDuration(), localPath);
        } else if (TextUtils.isEmpty(request.getIp())) {
            tcpdump.start(request.getPort(), request.getDuration(), localPath);
        } else if (request.getPort() == 0) {
            tcpdump.start(request.getIp(), request.getDuration(), localPath);
        } else {
            tcpdump.start(request.getPort(), request.getIp(), request.getDuration(),
                    localPath);
        }
    }

}
