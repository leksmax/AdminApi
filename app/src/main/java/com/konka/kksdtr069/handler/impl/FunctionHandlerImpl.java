package com.konka.kksdtr069.handler.impl;

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
import com.konka.kksdtr069.handler.FunctionHandler;
import com.konka.kksdtr069.model.SysLog;
import com.konka.kksdtr069.util.LinuxUtils;
import com.konka.kksdtr069.util.LogUtils;
import com.konka.kksdtr069.util.NetMeasureUtils;
import com.konka.kksdtr069.util.PropertyUtils;
import com.konka.kksdtr069.util.SFTPUtils;
import com.konka.kksdtr069.util.UploadLogUtils;

import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPParameter;
import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPPingRequest;
import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPPingResult;
import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPTraceRouteRequest;
import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPTraceRouteResult;
import net.sunniwell.cwmp.protocol.sdk.aidl.ICWMPProtocolService;
import net.sunniwell.cwmp.protocol.sdk.aidl.SetParameterValuesFault;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FunctionHandlerImpl implements FunctionHandler {

    public static final String TAG = FunctionHandlerImpl.class.getSimpleName();

    private static FunctionHandlerImpl instance;

    private Context context;

    private DBHandlerImpl dbHandler;

    private int pacgStatus = 1;

    private FunctionHandlerImpl() {
        context = BaseApplication.instance.getApplicationContext();
        dbHandler = DBHandlerImpl.getInstance();
        LogUtils.d(TAG, "new DBHandlerImpl for FunctionHandlerImpl");
    }

    public static FunctionHandlerImpl getInstance() {
        if (instance == null) {
            instance = new FunctionHandlerImpl();
        }
        return instance;
    }

    @Override
    public void pingDiagnosis(List<CWMPParameter> list, final ICWMPProtocolService protocolService) throws RemoteException {
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
            LogUtils.d(TAG, "ping request : " + "\n"
                    + "pingDiagnosticsState = " + request.getDiagnosticsState() + "\n"
                    + "pingHost = " + request.getHost() + "\n"
                    + "pingNumberOfRepetitions = " + request.getNumberOfRepetitions() + "\n"
                    + "pingTimeout = " + request.getTimeout() + "\n"
                    + "pingDataBlockSize = " + request.getDataBlockSize() + "\n"
                    + "pingDSCP = " + request.getDSCP());
            new Thread() {
                @Override
                public void run() {
                    CWMPPingResult pingResult = LinuxUtils.ping(request);
                    if (pingResult.getMaximumResponseTime() == 0
                            || pingResult.getMinimumResponseTime() == 0
                            || pingResult.getAverageResponseTime() == 0) {
                        pingResult.setDiagnosticsState("Failure");
                    }
                    LogUtils.d(TAG, "exe command : ping" +
                            " -Q " + request.getDSCP() +
                            " -s " + request.getDataBlockSize() +
                            " -w " + request.getTimeout() +
                            " -c " + request.getNumberOfRepetitions() +
                            " " + request.getHost() + "\n"
                            + "MaximumResponseTime = " + pingResult.getMaximumResponseTime() + "\n"
                            + "MinimumResponseTime = " + pingResult.getMinimumResponseTime() + "\n"
                            + "AverageResponseTime = " + pingResult.getAverageResponseTime() + "\n"
                            + "FailureCount = " + pingResult.getFailureCount() + "\n"
                            + "SuccessCount = " + pingResult.getSuccessCount() + "\n"
                            + "DiagnosticsState = " + pingResult.getDiagnosticsState() + "\n");
                    try {
                        dbHandler.update("Device.LAN.IPPingDiagnostics.MaximumResponseTime",
                                pingResult.getMaximumResponseTime() + "");
                        dbHandler.update("Device.LAN.IPPingDiagnostics.MinimumResponseTime",
                                pingResult.getMinimumResponseTime() + "");
                        dbHandler.update("Device.LAN.IPPingDiagnostics.AverageResponseTime",
                                pingResult.getAverageResponseTime() + "");
                        dbHandler.update("Device.LAN.IPPingDiagnostics.FailureCount", pingResult
                                .getFailureCount() + "");
                        dbHandler.update("Device.LAN.IPPingDiagnostics.SuccessCount", pingResult
                                .getSuccessCount() + "");
                        dbHandler.update("Device.LAN.IPPingDiagnostics.DiagnosticsState",
                                pingResult.getDiagnosticsState());
                        protocolService.onDiagnosisFinish();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }

    @Override
    public void traceRouteDiagnosis(List<CWMPParameter> list, final ICWMPProtocolService protocolService) throws RemoteException {
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
            new Thread() {
                @Override
                public void run() {
                    LogUtils.d(TAG, "start trace route");
                    CWMPTraceRouteResult traceResult = LinuxUtils.traceRoute(request);
                    if (traceResult.getNumberOfRouteHops() == 0 || traceResult.getResponseTime() == 0) {
                        traceResult.setDiagnosticsState("Failure");
                    } else {
                        traceResult.setDiagnosticsState("Complete");
                    }
                    LogUtils.d(TAG, "exe command : trace route" +
                            " -m " + request.getMaxHopCount() +
                            " -w " + request.getTimeout() +
                            " " + request.getHost() + " " + request.getDataBlockSize() + "\n"
                            + "MaximumResponseTime = " + traceResult.getMaximumResponseTime() + "\n"
                            + "MinimumResponseTime = " + traceResult.getMinimumResponseTime() + "\n"
                            + "AverageResponseTime = " + traceResult.getAverageResponseTime() + "\n"
                            + "FailureCount = " + traceResult.getFailureCount() + "\n"
                            + "ResponseTime = " + traceResult.getResponseTime() + "\n"
                            + "DiagnosticsState = " + traceResult.getDiagnosticsState() + "\n"
                            + "NumberOfRouteHops = " + traceResult.getNumberOfRouteHops() + "\n");
                    try {
                        dbHandler.update(
                                "Device.LAN.TraceRouteDiagnostics.MaximumResponseTime",
                                traceResult.getMaximumResponseTime() + "");
                        dbHandler.update(
                                "Device.LAN.TraceRouteDiagnostics.MinimumResponseTime",
                                traceResult.getMinimumResponseTime() + "");
                        dbHandler.update(
                                "Device.LAN.TraceRouteDiagnostics.AverageResponseTime",
                                traceResult.getAverageResponseTime() + "");
                        dbHandler.update(
                                "Device.LAN.TraceRouteDiagnostics.FailureCount",
                                traceResult.getFailureCount() + "");
                        dbHandler.update(
                                "Device.LAN.TraceRouteDiagnostics.ResponseTime",
                                traceResult.getResponseTime() + "");
                        dbHandler.update(
                                "Device.LAN.TraceRouteDiagnostics.DiagnosticsState",
                                traceResult.getDiagnosticsState());
                        dbHandler.update(
                                "Device.LAN.TraceRouteDiagnostics.NumberOfRouteHops",
                                traceResult.getNumberOfRouteHops() + "");
                        dbHandler.delete("TraceRouteDiagnostics.RouteHops");
                        StringBuilder RouteHops = new StringBuilder();
                        for (int i = 0; i < traceResult.getRouteHops().size(); i++) {
                            RouteHops.append(traceResult.getRouteHops().get(i)).append("#");
                        }
                        ContentValues cv = new ContentValues();
                        cv.put(DBHandlerImpl.COLUMN_NAME, "Device.LAN.TraceRouteDiagnostics.RouteHops.");
                        String routeHops = RouteHops.toString();
                        if (!routeHops.isEmpty()) {
                            routeHops = routeHops.substring(0, routeHops.length() - 1);
                        }
                        cv.put(DBHandlerImpl.COLUMN_VALUE, routeHops);
                        cv.put(DBHandlerImpl.COLUMN_TYPE, "string(256)");
                        cv.put(DBHandlerImpl.COLUMN_WRITABLE, "0");
                        cv.put(DBHandlerImpl.COLUMN_SECURE, "0");
                        cv.put(DBHandlerImpl.COLUMN_NOTIFICATION, "0");
                        dbHandler.insert(cv);
                        protocolService.onDiagnosisFinish();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }

    /* 陕西RMS暂时无需终端测速的接口 */
    @Override
    public void terminalSpeedMeasurement(List<CWMPParameter> list, final ICWMPProtocolService protocolService) throws RemoteException {
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
            NetMeasureUtils netMeasureUtils = NetMeasureUtils.getInstance();
            netMeasureUtils.speedTest();
            netMeasureUtils.setSpeedTestCompletedListener(
                    new NetMeasureUtils.SpeedTestCompletedListener() {
                        @Override
                        public void submitResult(boolean success) {
                            Log.d(TAG, "Speed test: succeed");
                            try {
                                protocolService.onDiagnosisFinish();
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }

                        }
                    });
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
            LogUtils.d(TAG, String.format("packetState = %s" + "\n" +
                            "packetDuration = %s" + "\n" +
                            "packetIP = %s" + "\n" +
                            "packetPort = %s" + "\n" +
                            "packetUploadURL = %s" + "\n" +
                            "packetUsername = %s" + "\n" +
                            "packetPassword = %s" + "\n" +
                            packetState,
                    packetDuration,
                    packetIP,
                    packetPort,
                    packetUploadURL,
                    packetUsername,
                    packetPassword));
            LogUtils.d(TAG, "Received a crawl network packet request");

            LinuxUtils.removeSubFile("/data/data/com.konka.kksdtr069/cache/pcap/");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String date = sdf.format(new Date());
            final String fileName = PropertyUtils.getProperty("ro.mac")
                    .replace(":", "") + "_" + date + "" + ".pcap";
            dbHandler.update("Device.X_00E0FC.PacketCapture.State", "3");

            final String finalPacketIP;
            if (TextUtils.isEmpty(packetIP)) {
                finalPacketIP = packetUploadURL.split("//")[1]
                        .split("/")[0].split(":")[0];
            } else {
                finalPacketIP = packetIP;
            }

            final String finalPacketPort;
            if (TextUtils.isEmpty(packetPort)) {
                finalPacketPort = packetUploadURL.split("//")[1]
                        .split("/")[0].split(":")[1];
            } else {
                finalPacketPort = packetPort;
            }

            final String finalPacketUsername = packetUsername;
            final String finalPacketPassword = packetPassword;
            final String finalUploadPath = packetUploadURL.split("//")[1]
                    .substring(packetUploadURL.split("//")[1].indexOf("/"));

            BasicMain basicMain = BasicMain.GetInstance();
            basicMain.regMsgHandler(context.getMainLooper(), new MsgCallBack() {
                @Override
                public int tcpdumpFinish(int error) {
                    // Do something
                    try {
                        dbHandler.update("Device.X_00E0FC.PacketCapture.State", "5");
                        Thread.sleep(1000);
                        pacgStatus = 1;
                        LogUtils.d(TAG, "tcpdump finish");
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                LogUtils.d(TAG, "start upload file");
                                SFTPUtils sftpUtils = new SFTPUtils(finalPacketIP, finalPacketPort,
                                        finalPacketUsername, finalPacketPassword);
                                sftpUtils.connect();
                                LogUtils.d(TAG, "sftp connected");
//                                Boolean flag = sftpUtils.uploadFile(finalUploadPath, fileName,
//                                        "/data/data/com.konka.kksdtr069/cache/pcap/", fileName);
//                                LogUtils.d(TAG, "upload file finish");
//                                try {
//                                    if (flag == true) {
//                                        dbHandler.update("Device.X_00E0FC.PacketCapture.State",
//                                                "6");
//                                        dbHandler.update("Device.X_00E0FC.PacketCapture.State",
//                                                "1");
//                                        sftpUtils.disconnect();
//                                        LogUtils.d(TAG, "upload packet success");
//                                    } else {
//                                        dbHandler.update("Device.X_00E0FC.PacketCapture.State",
//                                                "7");
//                                        LogUtils.d(TAG, "upload packet failed");
//                                    }
//                                } catch (RemoteException e) {
//                                    e.printStackTrace();
//                                }

                            }
                        }).start();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return 0;
                }
            });

            Tcpdump tcpdump = Tcpdump.getInstance();
            LogUtils.d(TAG, "Crawling network packet");
            if (pacgStatus != 1) {
                tcpdump.stop();
            }
            pacgStatus = 0;
            String localPath = "/data/data/com.konka.kksdtr069/cache/pcap/" + fileName;
            if (TextUtils.isEmpty(packetIP) && TextUtils.isEmpty(packetPort)) {
                tcpdump.start(Integer.parseInt(packetDuration), localPath);
            } else if (TextUtils.isEmpty(packetIP)) {
                tcpdump.start(Integer.parseInt(packetPort), Integer.parseInt(packetDuration),
                        localPath);
            } else if (TextUtils.isEmpty(packetPort)) {
                tcpdump.start(packetIP, Integer.parseInt(packetDuration), localPath);
            } else {
                tcpdump.start(Integer.parseInt(packetPort), packetIP, Integer.parseInt
                        (packetDuration), localPath);
            }
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
            String name = mCWMPParameter.getName();

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
        LogUtils.d(TAG, "sys log : " + "\n"
                + "SysLogOutputType = " + syslogOutputType + "\n"
                + "SysLogLevel = " + syslogLevel + "\n"
                + "SysLogType = " + syslogType + "\n"
                + "SysLogServer = " + syslogServer + "\n"
                + "SysLogStartTime = " + syslogStartTime + "\n"
                + "SysLogContinueTime = " + syslogContinueTime + "\n"
                + "SysLogFTPServer = " + syslogFTPServer + "\n");
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
                LogUtils.d(TAG, "captureAndUploadLog() start");
                uploadLogUtils.start(sysLog);
                LogUtils.d(TAG, "captureAndUploadLog() finish");
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            if (uploadLogUtils != null) {
                uploadLogUtils.stopSendToSyslogServer();
                LogUtils.d(TAG, "captureAndUploadLog() stop send syslog to server");
            }
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
        if (!TextUtils.isEmpty(wifiEnable)) {
            Intent intent = new Intent();
            intent.setAction("com.android.settings");
            if ("0".equals(wifiEnable)) {
                LogUtils.d(TAG, "set parameters : make wifi enable");
                intent.putExtra("WifiEnable", true);
            } else {
                LogUtils.d(TAG, "set parameters : make wifi disable");
                intent.putExtra("WifiEnable", false);
            }
            LogUtils.d(TAG, "WifiEnable() start");
            context.sendBroadcast(intent);
            try {
                Thread.sleep(2500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            LogUtils.d(TAG, "WifiEnable() finish");
            WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService
                    (Context.WIFI_SERVICE);
            if ((!"0".equals(wifiEnable)) && manager.isWifiEnabled()) {
                // 如果下发关闭wifi，但是由于用户正在使用关闭失败，移除参数改变上报,恢复打开状态
                if (parameterCacheList.size() > 0) {
                    for (CWMPParameter parameter : parameterCacheList) {
                        if (parameter.getName().contains("Device.X_CMCC_OTV.ServiceInfo" +
                                ".WiFiEnable")) {
                            parameterCacheList.remove(parameter);
                            parameter.setValue("0");
                            dbHandler.update(parameter);
                            LogUtils.d(TAG, "the WiFi closed commend is issued" + "\n" +
                                    "but the user is using the shutdown failure" + "\n" +
                                    "remove the parameter change report and restore the open status");
                        }
                    }
                }
                SetParameterValuesFault fault = new SetParameterValuesFault();
                fault.setFaultCode(9002);
                fault.setFaultString("Closing wifi failure，wifi is using.");
                fault.setParameterName("Device.X_CMCC_OTV.ServiceInfo.WiFiEnable");
                faultList.add(fault);
            }
        }
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
