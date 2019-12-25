package com.konka.kksdtr069.handler;

import android.os.RemoteException;

import com.konka.kksdtr069.observer.ProtocolObserver;

import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPParameter;

import java.util.List;

public interface NetworkHandler {

    void updateNetwork(List<CWMPParameter> parameterCacheList,
                       ProtocolObserver protocolObserver) throws RemoteException;
}
