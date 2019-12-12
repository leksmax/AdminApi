package com.konka.kksdtr069.handler;

import android.os.RemoteException;

import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPParameter;
import net.sunniwell.cwmp.protocol.sdk.aidl.SetParameterValuesFault;

import java.util.List;

public interface FunctionHandler {

    List<SetParameterValuesFault> pingDiagnosis(List<CWMPParameter> list) throws RemoteException;

    List<SetParameterValuesFault> traceRouteDiagnosis(List<CWMPParameter> list) throws RemoteException;

    List<SetParameterValuesFault> terminalSpeedMeasurement(List<CWMPParameter> list) throws RemoteException;

    List<SetParameterValuesFault> remoteNetPacketCapture(List<CWMPParameter> list) throws RemoteException;

    List<SetParameterValuesFault> captureAndUploadLog(List<CWMPParameter> list) throws RemoteException;

    List<SetParameterValuesFault> wifiEnable(List<CWMPParameter> list) throws RemoteException;

    List<SetParameterValuesFault> modifyQRCodeDisplay(List<CWMPParameter> list) throws RemoteException;
}
