package com.konka.kksdtr069.handler;

import android.os.RemoteException;

import net.sunniwell.cwmp.protocol.sdk.aidl.AppID;
import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPDownloadRequest;

import java.util.List;

public interface SystemHandler {

    void appUninstall(List<AppID> list) throws RemoteException;

    void download(CWMPDownloadRequest request) throws RemoteException;

    void FactoryReset() throws RemoteException;

    void reboot() throws RemoteException;

    void onLogin(int type, boolean isSuccess) throws RemoteException;


}
