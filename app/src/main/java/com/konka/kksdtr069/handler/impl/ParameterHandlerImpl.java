package com.konka.kksdtr069.handler.impl;

import android.net.Uri;
import android.os.RemoteException;

import com.konka.kksdtr069.handler.ParameterHandler;
import com.konka.kksdtr069.util.LogUtils;

import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPParameter;

import java.util.List;

public class ParameterHandlerImpl implements ParameterHandler {

    private static ParameterHandlerImpl instance;

    public static final Uri URI = DBHandlerImpl.URI;

    private static DBHandlerImpl dbHandler;

    public static final String TAG = ParameterHandlerImpl.class.getSimpleName();

    private ParameterHandlerImpl() {
        dbHandler = DBHandlerImpl.getInstance();
        LogUtils.d(TAG, "new DBhandlerImpl for ParameterHandlerImpl");
    }

    public static ParameterHandlerImpl getInstance() {
        if (instance == null) {
            synchronized (ParameterHandlerImpl.class) {
                if (instance == null) {
                    instance = new ParameterHandlerImpl();
                }
            }
        }
        return instance;
    }

    @Override
    public List<CWMPParameter> getParameterBySuperName(String path) throws RemoteException {
        return dbHandler.fuzzyQueryByName(path);
    }

    @Override
    public List<CWMPParameter> getInformParameters() throws RemoteException {
        return dbHandler.queryInformParameters();
    }

    @Override
    public CWMPParameter getParameter(String name) throws RemoteException {
        return dbHandler.queryByName(name);
    }

    @Override
    public String getParameterValue(String name) throws RemoteException {
        return dbHandler.queryByNameForString(name);
    }

    @Override
    public int setParameterValue(String name, String value) throws RemoteException {
        int result = 0;
        if (name.equals("Device.LAN.IPAddress") || name.equals("Device.LAN.DefaultGateway") ||
                name.equals("Device.LAN.DNSServers2")) {
            String addressingType = dbHandler.queryByNameForString("Device.LAN.AddressingType");
            LogUtils.d(TAG, "addressing type = " + addressingType);
            if (addressingType.equals("STATIC")) {
                result = dbHandler.update(name, value);
                LogUtils.d(TAG, "update static ip result = " + result);
            }
        } else {
            result = dbHandler.update(name, value);
        }
        return result;

    }
}
