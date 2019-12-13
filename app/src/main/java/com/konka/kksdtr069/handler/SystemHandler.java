package com.konka.kksdtr069.handler;

import net.sunniwell.cwmp.protocol.sdk.aidl.AppID;
import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPDownloadRequest;

import java.util.List;

public interface SystemHandler {

    void appUninstall(List<AppID> list);

    void download(CWMPDownloadRequest request);

    void FactoryReset();

    void reboot();

    void onLogin(int type, boolean isSuccess);


}
