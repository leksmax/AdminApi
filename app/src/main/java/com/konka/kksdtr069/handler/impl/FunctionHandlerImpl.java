package com.konka.kksdtr069.handler.impl;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.konka.kksdtr069.base.BaseApplication;
import com.konka.kksdtr069.handler.FunctionHandler;
import com.konka.kksdtr069.model.PacketCaptureRequest;
import com.konka.kksdtr069.model.SysLog;
import com.konka.kksdtr069.observer.DBObserver;
import com.konka.kksdtr069.observer.FunctionObserver;
import com.konka.kksdtr069.observer.ProtocolObserver;
import com.konka.kksdtr069.util.LinuxUtils;
import com.konka.kksdtr069.util.UploadLogUtils;

import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPParameter;
import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPPingRequest;
import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPTraceRouteRequest;
import net.sunniwell.cwmp.protocol.sdk.aidl.SetParameterValuesFault;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class FunctionHandlerImpl implements FunctionHandler {

    public static final String TAG = FunctionHandler.class.getSimpleName();

    private static FunctionHandlerImpl instance;

    private Context context;

    private DBHandlerImpl dbHandler = DBHandlerImpl.getInstance();

    private FunctionObserver functionObserver = FunctionObserver.getInstance();

    private ProtocolObserver protocolObserver = ProtocolObserver.getInstance();

    private DBObserver dbObserver = DBObserver.getInstance();

    private FunctionHandlerImpl() {
        context = BaseApplication.instance.getApplicationContext();
    }

    public static FunctionHandlerImpl getInstance() {
        if (instance == null) {
            instance = new FunctionHandlerImpl();
        }
        return instance;
    }

    @Override
    public void pingDiagnosis(List<CWMPParameter> list) throws RemoteException {
        String pingState = "";
        String pingHost = "";
        String pingRepeat = "";
        String pingTimeout = "";
        String pingDBS = "";
        String pingDSCP = "";
        for (CWMPParameter parameter : list) {
            dbHandler.update(parameter);
            String name = parameter.getName();

            if (name.contains("IPPingDiagnostics.DiagnosticsState") & pingState.equals("")) {
                pingState = parameter.getValue();
            } else if (name.contains("IPPingDiagnostics.Host") & pingHost.equals("")) {
                pingHost = parameter.getValue();
            } else if (name.contains("IPPingDiagnostics.NumberOfRepetitions")
                    & pingRepeat.equals("")) {
                pingRepeat = parameter.getValue();
            } else if (name.contains("IPPingDiagnostics.Timeout") & pingTimeout.equals("")) {
                pingTimeout = parameter.getValue();
            } else if (name.contains("IPPingDiagnostics.DataBlockSize") & pingDBS.equals("")) {
                pingDBS = parameter.getValue();
            } else if (name.contains("IPPingDiagnostics.DSCP") & pingDSCP.equals("")) {
                pingDSCP = parameter.getValue();
            }
        }

        if (!pingHost.isEmpty()) {
            final CWMPPingRequest request = initCWMPPingRequest(pingHost, pingState, pingRepeat,
                    pingTimeout, pingDBS, pingDSCP);
            functionObserver.observerPing(request);
        }
        protocolObserver.diagnosisFinish();
    }

    @Override
    public void traceRouteDiagnosis(List<CWMPParameter> list) throws RemoteException {
        String traceState = "";
        String traceHost = "";
        String traceMHC = "";
        String traceTimeout = "";
        String traceDBS = "";
        String traceDSCP = "";

        for (CWMPParameter mCWMPParameter : list) {
            dbHandler.update(mCWMPParameter);
            String name = mCWMPParameter.getName();

            if (name.contains("TraceRouteDiagnostics.DiagnosticsState") && traceState.equals("")) {
                traceState = mCWMPParameter.getValue();
            } else if (name.contains("TraceRouteDiagnostics.Host") && traceHost.equals("")) {
                traceHost = mCWMPParameter.getValue();
            } else if (name.contains("TraceRouteDiagnostics.MaxHopCount") && traceMHC.equals("")) {
                traceMHC = mCWMPParameter.getValue();
            } else if (name.contains("TraceRouteDiagnostics.Timeout") && traceTimeout.equals("")) {
                traceTimeout = mCWMPParameter.getValue();
            } else if (name.contains("TraceRouteDiagnostics.DataBlockSize")
                    && traceDBS.equals("")) {
                traceDBS = mCWMPParameter.getValue();
            } else if (name.contains("TraceRouteDiagnostics.DSCP") && traceDSCP.equals("")) {
                traceDSCP = mCWMPParameter.getValue();
            }
        }

        if (!traceHost.isEmpty()) {
            LinuxUtils.varifyFile(context, "traceroute");
            final CWMPTraceRouteRequest request = initCWMPTraceRouteRequest(traceHost, traceState,
                    traceMHC, traceTimeout, traceDBS, traceDSCP);
            functionObserver.observerTraceRoute(request);
        }
        protocolObserver.diagnosisFinish();
    }

    /* 陕西RMS暂时无需实现终端测速的接口 */
    @Override
    public void terminalSpeedMeasurement(List<CWMPParameter> list) throws RemoteException {
        String speedTest = "";
        for (CWMPParameter parameter : list) {
            dbHandler.update(parameter);
            String name = parameter.getName();
            if (name.contains("Device.X_CMCC_OTV.BandwidthDiagnostics.DiagnosticsState")
                    && speedTest.equals("")) {
                speedTest = parameter.getValue();
            }
        }
        if ("Requested".equals(speedTest)) {
            functionObserver.speedMeasurement();
        }
    }

    @Override
    public void remoteNetPacketCapture(List<CWMPParameter> list) throws RemoteException {
        String packetState = "";
        String packetDuration = "";
        String packetIP = "";
        String packetPort = "";
        String packetUploadURL = "";
        String packetUsername = "";
        String packetPassword = "";

        for (CWMPParameter mCWMPParameter : list) {
            dbHandler.update(mCWMPParameter);
            String name = mCWMPParameter.getName();

            if (name.contains("Device.X_00E0FC.PacketCapture.State")
                    && packetState.equals("")) {
                packetState = mCWMPParameter.getValue();
            } else if (name.contains("Device.X_00E0FC.PacketCapture.Duration")
                    && packetDuration.equals("")) {
                packetDuration = mCWMPParameter.getValue();
            } else if (name.contains("Device.X_00E0FC.PacketCapture.IP")
                    && packetIP.equals("")) {
                packetIP = mCWMPParameter.getValue();
            } else if (name.contains("Device.X_00E0FC.PacketCapture.Port")
                    && packetPort.equals("")) {
                packetPort = mCWMPParameter.getValue();
            } else if (name.contains("Device.X_00E0FC.PacketCapture.UploadURL")
                    && packetUploadURL.equals("")) {
                packetUploadURL = mCWMPParameter.getValue();
            } else if (name.contains("Device.X_00E0FC.PacketCapture.Username")
                    && packetUsername.equals("")) {
                packetUsername = mCWMPParameter.getValue();
            } else if (name.contains("Device.X_00E0FC.PacketCapture.Password")
                    && packetPassword.equals("")) {
                packetPassword = mCWMPParameter.getValue();
            }
        }
        if ("2".equals(packetState)) {
            PacketCaptureRequest request = new PacketCaptureRequest(packetState, packetDuration,
                    packetIP, packetPort, packetUploadURL, packetUsername, packetPassword);
            functionObserver.observerCapturePackage(request);
        }
    }

    /* 暂时无需发送SFTP日志 */
    @Override
    public void captureAndUploadLog(List<CWMPParameter> list) throws RemoteException {
        String syslogServer = "";// Syslog上报服务器
        String syslogLevel = "";// 输出日志的级别 0:不过滤 3:Error 6:Info 7:Debug
        String syslogType = "";// 输出日志的类型 0:不过滤 16:操作日志 17:运行日志 19:用户日志 20:用户日志
        String syslogOutputType = "";/*日志输出方式 0:关闭日志功能 1:仅发送SFTP日志 2:仅发送实时日志
                                       3:SFTP和即时日志都发送*/
        String syslogStartTime = "";// 开始时间
        String syslogContinueTime = "";// 日志输出持续时间，单位：分钟
        String syslogTimer = "";// SFTP日志自动上传定时器值。单位：秒
        String syslogFTPServer = "";// FTP格式的URL，sftp://ftpuser:111111@192.168.0.8

        for (CWMPParameter mCWMPParameter : list) {
            dbHandler.update(mCWMPParameter);
            String name = mCWMPParameter.getValue();

            if (name.contains("Device.X_00E0FC.LogParaConfiguration.SyslogServer")
                    && syslogServer.equals("")) {
                syslogServer = mCWMPParameter.getValue();
            } else if (name.contains("Device.X_00E0FC.LogParaConfiguration.LogLevel")
                    && syslogLevel.equals("")) {
                syslogLevel = mCWMPParameter.getValue();
            } else if (name.contains("Device.X_00E0FC.LogParaConfiguration.LogType")
                    && syslogType.equals("")) {
                syslogType = mCWMPParameter.getValue();
            } else if (name.contains("Device.X_00E0FC.LogParaConfiguration.LogOutPutType")
                    && syslogOutputType.equals("")) {
                syslogOutputType = mCWMPParameter.getValue();
            } else if (name.contains("Device.X_00E0FC.LogParaConfiguration.SyslogStartTime")
                    && syslogStartTime.equals("")) {
                syslogStartTime = mCWMPParameter.getValue();
                Log.d(TAG, syslogStartTime);
            } else if (name.contains("Device.X_00E0FC.LogParaConfiguration.SyslogContinueTime")
                    && syslogContinueTime.equals("")) {
                syslogContinueTime = mCWMPParameter.getValue();
            } else if (name.contains("Device.X_00E0FC.LogParaConfiguration.LogTimer")
                    && syslogTimer.equals("")) {
                syslogTimer = mCWMPParameter.getValue();
            } else if (name.contains("Device.X_00E0FC.LogParaConfiguration.LogFtpServer")
                    && syslogFTPServer.equals("")) {
                syslogFTPServer = mCWMPParameter.getValue();
            }
        }
        UploadLogUtils uploadLogUtils = UploadLogUtils.getInstance();
        if (!isUploadSysLog(syslogOutputType)) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                SysLog sysLog = new SysLog(Integer.parseInt(syslogOutputType),
                        Integer.parseInt(syslogLevel),
                        Integer.parseInt(syslogType),
                        syslogServer,
                        format.parse(syslogStartTime),
                        Integer.parseInt(syslogContinueTime),
                        syslogFTPServer);
                uploadLogUtils.start(sysLog);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            uploadLogUtils.stopSendToSyslogServer();
        }
    }

    private boolean isUploadSysLog(String syslogOutputType) {
        return TextUtils.isEmpty(syslogOutputType)
                || "0".equals(syslogOutputType);
    }

    @Override
    public List<SetParameterValuesFault> wifiEnable(List<CWMPParameter> list) throws RemoteException {
        ArrayList<SetParameterValuesFault> faultList = new ArrayList<SetParameterValuesFault>();
        ArrayList<CWMPParameter> parameterCacheList = new ArrayList<CWMPParameter>();
        String wifiEnable = "";// 是否打开wifi 0：允许 1：不允许
        for (CWMPParameter parameter : list) {
            dbHandler.update(parameter);
            String name = parameter.getValue();
            if (name.contains("Device.X_CMCC_OTV.ServiceInfo.WiFiEnable")
                    && wifiEnable.equals("")) {
                wifiEnable = parameter.getValue();
            }
            if (parameter.getNotification() == 2) {
                parameterCacheList.add(parameter);
            }
        }
        functionObserver.observerWifiEnable(wifiEnable, parameterCacheList, faultList);
        return faultList;
    }

    /* 暂时无需实现 */
    @Override
    public void modifyQRCodeDisplay(List<CWMPParameter> list) throws RemoteException {
        for (CWMPParameter parameter : list) {
            dbHandler.update(parameter);
        }
    }

    private CWMPPingRequest initCWMPPingRequest(String pingHost, String pingState,
                                                String pingRepeat, String pingTimeout,
                                                String pingDBS, String pingDSCP) {
        CWMPPingRequest pingRequest = new CWMPPingRequest();
        pingRequest.setHost(pingHost);
        pingRequest.setDiagnosticsState(pingState);
        pingRequest.setNumberOfRepetitions(Integer.parseInt(pingRepeat));
        pingRequest.setTimeout(Long.parseLong(pingTimeout));
        pingRequest.setDataBlockSize(Long.parseLong(pingDBS));
        pingRequest.setDSCP(pingDSCP);
        return pingRequest;
    }

    private CWMPTraceRouteRequest initCWMPTraceRouteRequest(String traceHost, String traceState,
                                                            String traceMHC, String traceTimeout,
                                                            String traceDBS, String traceDSCP) {
        CWMPTraceRouteRequest traceRequest = new CWMPTraceRouteRequest();
        traceRequest.setHost(traceHost);
        traceRequest.setDiagnosticsState(traceState);
        traceRequest.setMaxHopCount(Integer.parseInt(traceMHC));
        traceRequest.setTimeout(Long.parseLong(traceTimeout));
        traceRequest.setDataBlockSize(Long.parseLong(traceDBS));
        traceRequest.setDSCP(traceDSCP);
        return traceRequest;
    }
}
