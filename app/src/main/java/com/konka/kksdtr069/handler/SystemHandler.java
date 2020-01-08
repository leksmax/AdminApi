package com.konka.kksdtr069.handler;

import android.os.RemoteException;

import net.sunniwell.cwmp.protocol.sdk.aidl.AppID;
import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPDownloadRequest;
import net.sunniwell.cwmp.protocol.sdk.aidl.ICWMPProtocolService;

import java.util.List;

public interface SystemHandler {

    void appUninstall(List<AppID> list, ICWMPProtocolService protocolService);

    void download(CWMPDownloadRequest request, ICWMPProtocolService protocolService);

    void FactoryReset();

    void reboot();

    void onLogin(int type, boolean isSuccess);


}
