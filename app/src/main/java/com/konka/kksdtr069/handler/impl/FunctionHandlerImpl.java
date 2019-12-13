package com.konka.kksdtr069.handler.impl;

import android.content.Context;
import android.os.RemoteException;

import com.konka.kksdtr069.base.BaseApplication;
import com.konka.kksdtr069.handler.FunctionHandler;
import com.konka.kksdtr069.observer.DBObserver;
import com.konka.kksdtr069.observer.FunctionObserver;
import com.konka.kksdtr069.observer.ProtocolObserver;
import com.konka.kksdtr069.util.LinuxUtils;

import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPParameter;
import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPPingRequest;
import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPTraceRouteRequest;
import net.sunniwell.cwmp.protocol.sdk.aidl.SetParameterValuesFault;

import java.util.ArrayList;
import java.util.List;

public class FunctionHandlerImpl implements FunctionHandler {

    private static FunctionHandlerImpl instance;

    private Context context;

    private DBHandlerImpl dbHandler = DBHandlerImpl.getInstance();

    private FunctionObserver functionPresenter = FunctionObserver.getInstance();

    private ProtocolObserver protocolPresenter = ProtocolObserver.getInstance();

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
    public List<SetParameterValuesFault> pingDiagnosis(List<CWMPParameter> list) throws RemoteException {
        String pingState = "";
        String pingHost = "";
        String pingRepeat = "";
        String pingTimeout = "";
        String pingDBS = "";
        String pingDSCP = "";
        List<SetParameterValuesFault> faultList = new ArrayList<SetParameterValuesFault>();
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
            functionPresenter.observerPing(request);
        }
        protocolPresenter.diagnosisFinish();
        return faultList;
    }

    @Override
    public List<SetParameterValuesFault> traceRouteDiagnosis(List<CWMPParameter> list) throws RemoteException {
        List<SetParameterValuesFault> faultList = new ArrayList<SetParameterValuesFault>();

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
            functionPresenter.observerTraceRoute(request);
        }
        protocolPresenter.diagnosisFinish();
        return faultList;
    }

    @Override
    public List<SetParameterValuesFault> terminalSpeedMeasurement(List<CWMPParameter> list) throws RemoteException {
        List<SetParameterValuesFault> faultList = new ArrayList<SetParameterValuesFault>();
        for (CWMPParameter parameter : list) {
            dbHandler.update(parameter);
        }
        // 终端测试待实现
        return faultList;
    }

    @Override
    public List<SetParameterValuesFault> remoteNetPacketCapture(List<CWMPParameter> list) throws RemoteException {
        List<SetParameterValuesFault> faultList = new ArrayList<SetParameterValuesFault>();
        for (CWMPParameter parameter : list) {
            dbHandler.update(parameter);
        }
        // 远程抓包、上传待实现
        return null;
    }

    @Override
    public List<SetParameterValuesFault> captureAndUploadLog(List<CWMPParameter> list) throws RemoteException {
        List<SetParameterValuesFault> faultList = new ArrayList<SetParameterValuesFault>();
        for (CWMPParameter parameter : list) {
            dbHandler.update(parameter);
        }
        // 抓取、上报日志待实现
        return null;
    }

    @Override
    public List<SetParameterValuesFault> wifiEnable(List<CWMPParameter> list) throws RemoteException {
        ArrayList<SetParameterValuesFault> faultList = new ArrayList<SetParameterValuesFault>();
        ArrayList<CWMPParameter> parameterCacheList = new ArrayList<CWMPParameter>();
        for (CWMPParameter parameter : list) {
            dbHandler.update(parameter);
        }
        // 打开、关闭WiFi待实现
        dbObserver.notifyChange(dbObserver, parameterCacheList);
        return faultList;
    }

    @Override
    public List<SetParameterValuesFault> modifyQRCodeDisplay(List<CWMPParameter> list) throws RemoteException {
        ArrayList<SetParameterValuesFault> faultList = new ArrayList<SetParameterValuesFault>();
        for (CWMPParameter parameter : list) {
            dbHandler.update(parameter);
        }
        // 修改电视二维码显示文字待实现
        return faultList;
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
