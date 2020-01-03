package com.konka.kksdtr069.handler;

import android.os.RemoteException;

import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPParameter;
import net.sunniwell.cwmp.protocol.sdk.aidl.ICWMPProtocolService;
import net.sunniwell.cwmp.protocol.sdk.aidl.SetParameterValuesFault;

import java.util.List;

public interface FunctionHandler {

    void pingDiagnosis(List<CWMPParameter> list, ICWMPProtocolService protocolService) throws RemoteException;

    void traceRouteDiagnosis(List<CWMPParameter> list, ICWMPProtocolService protocolService) throws RemoteException;

    void terminalSpeedMeasurement(List<CWMPParameter> list, ICWMPProtocolService protocolService) throws RemoteException;

    void remoteNetPacketCapture(List<CWMPParameter> list) throws RemoteException;

    void captureAndUploadLog(List<CWMPParameter> list) throws RemoteException;

    List<SetParameterValuesFault> wifiEnable(List<CWMPParameter> list) throws RemoteException;

    void modifyQRCodeDisplay(List<CWMPParameter> list) throws RemoteException;
}
