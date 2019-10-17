package com.konka.kksdtr069.service;

import android.content.Context;
import android.os.RemoteException;

import net.sunniwell.cwmp.protocol.sdk.aidl.AppID;
import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPDownloadRequest;
import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPParameter;
import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPSetParameterAttributesStruct;
import net.sunniwell.cwmp.protocol.sdk.aidl.ICWMPNativeService;
import net.sunniwell.cwmp.protocol.sdk.aidl.ICWMPProtocolService;
import net.sunniwell.cwmp.protocol.sdk.aidl.SetParameterValuesFault;

import java.util.List;

public class CWMPService extends ICWMPNativeService.Stub {

    private static final String TAG = "Tr069_CWMPService";

    public CWMPService(Context context, ICWMPProtocolService cwmpProtocolService){

    }

    @Override
    public List<CWMPParameter> getInformParameters() throws RemoteException {
        return null;
    }

    @Override
    public List<CWMPParameter> getPeriodicParameters() throws RemoteException {
        return null;
    }

    @Override
    public List<CWMPParameter> getParameters(String s) throws RemoteException {
        return null;
    }

    @Override
    public String getParameterValue(String s) throws RemoteException {
        return null;
    }

    @Override
    public int setParameterValue(String s, String s1) throws RemoteException {
        return 0;
    }

    @Override
    public CWMPParameter getParameter(String s) throws RemoteException {
        return null;
    }

    @Override
    public List<SetParameterValuesFault> setParameters(List<CWMPParameter> list) throws RemoteException {
        return null;
    }

    @Override
    public void reboot() throws RemoteException {

    }

    @Override
    public void factoryReset(boolean b) throws RemoteException {

    }

    @Override
    public void download(CWMPDownloadRequest cwmpDownloadRequest) throws RemoteException {

    }

    @Override
    public void onLogin(int i, boolean b) throws RemoteException {

    }

    @Override
    public void uninstall(List<AppID> list) throws RemoteException {

    }

    @Override
    public int addObject(String s) throws RemoteException {
        return 0;
    }

    @Override
    public void deleteObject(String s) throws RemoteException {

    }

    @Override
    public List<SetParameterValuesFault> setParameterAttributes(List<CWMPSetParameterAttributesStruct> list) throws RemoteException {
        return null;
    }
}
