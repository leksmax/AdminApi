package com.konka.kksdtr069.handler;

import android.os.RemoteException;

import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPParameter;
import net.sunniwell.cwmp.protocol.sdk.aidl.ICWMPProtocolService;

import java.util.List;

public interface ParameterHandler {

    List<CWMPParameter> getParameterBySuperName(String path) throws RemoteException;

    List<CWMPParameter> getInformParameters() throws RemoteException;

    String getParameterValue(String name) throws RemoteException;

    CWMPParameter getParameter(String name) throws RemoteException;

    int setParameterValue(String name, String value) throws RemoteException;
}
