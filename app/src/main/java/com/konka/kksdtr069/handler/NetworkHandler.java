package com.konka.kksdtr069.handler;

import android.os.RemoteException;

import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPParameter;
import net.sunniwell.cwmp.protocol.sdk.aidl.ICWMPProtocolService;

import java.util.List;

public interface NetworkHandler {

    void updateNetwork(List<CWMPParameter> parameterCacheList,
                       ICWMPProtocolService protocolService) throws RemoteException;
}
