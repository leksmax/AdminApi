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

    private Disposable mCapturePackageDisposable;

    private DBHandlerImpl dbHandler;

    private Context context;

    private int tcpdumpState = 1;

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

    public void observerCapturePackage(final PacketCaptureRequest request) {
        if (mCapturePackageDisposable != null && !mCapturePackageDisposable.isDisposed()) {
            removeObserver(mCapturePackageDisposable);
        }
        mCapturePackageDisposable = Observable.create(
                new ObservableOnSubscribe<PacketCaptureRequest>() {
                    @Override
                    public void subscribe(ObservableEmitter<PacketCaptureRequest> emitter)
                            throws Exception {
                        LogUtils.d(TAG, "capture package request : " + "\n"
                                + "PacketCapture.State = " + request.getState() + "\n"
                                + "PacketCapture.Duration = " + request.getDuration() + "\n"
                                + "PacketCapture.IP = " + request.getIp() + "\n"
                                + "PacketCapture.Port = " + request.getPort() + "\n"
                                + "PacketCapture.UploadURL = " + request.getUploadURL() + "\n"
                                + "PacketCapture.Username = " + request.getUsername() + "\n"
                                + "PacketCapture.Password = " + request.getPassword() + "\n");
                        // 正在抓包
                        tcpdump(request);
                        LogUtils.d(TAG, "start tcpdump");
                        // 下发新参数，删除之前的网络包，重新抓包
                        if (request.getState() == 3 && isNewPacketCaptureComm(request)) {
                            updatePacketCaptureComm(request);
                            LinuxUtils.removeSubFile("/data/data/com.konka" +
                                    ".kksdtr069/cache/pcap/");
                            tcpdump(request);
                            LogUtils.d(TAG, "Issue new parameters" + "\n"
                                    + "delete the previous network packet" + "\n"
                                    + "recapture the packet has been completed" + "\n");
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
                                        LogUtils.d(TAG, "Issue new parameters" + "\n"
                                                + "recapture the packet has been completed" + "\n");
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
                                        LogUtils.d(TAG, "Upload network package succeeded");
                                    } else {
                                        dbHandler.update("Device.X_00E0FC.PacketCapture" +
                                                ".State", "7");
                                        LogUtils.d(TAG, "Failed to upload network package");
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
