package com.konka.kksdtr069.handler.impl;

import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;

import com.konka.kksdtr069.handler.ParameterHandler;

import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPParameter;

import java.util.List;

public class ParameterHandlerImpl implements ParameterHandler {

    private static ParameterHandlerImpl instance;

    public static final Uri URI = DBHandlerImpl.URI;

    private static DBHandlerImpl dbHandler = DBHandlerImpl.getInstance();

    public static ParameterHandlerImpl getInstance() {
        if (instance == null) {
            instance = new ParameterHandlerImpl();
        }
        return instance;
    }

    @Override
    public List<CWMPParameter> getParameterBySuperName(String path) throws RemoteException {
        Cursor cursor = dbHandler.fuzzyQueryByName(path);
        return dbHandler.cursorToList(cursor);
    }

    @Override
    public List<CWMPParameter> getInformParameters() throws RemoteException {
        String[] names = new String[]{
                "Device.DeviceInfo", "Device.ManagementServer", "Device.Time",
                "Device.LAN", "Device.X_CMCC_OTV.STBInfo", "Device.X_CMCC_OTV.ServiceInfo",
                "Device.X_00E0FC.SoftwareVersionList"};
        Cursor cursor = dbHandler.fuzzyQueryByNames(names);
        return dbHandler.cursorToList(cursor);
    }

    @Override
    public CWMPParameter getParameter(String name) throws RemoteException {
        Cursor cursor = dbHandler.queryByNameForCursor(name);
        return dbHandler.cursorToCWMPParameter(cursor);
    }

    @Override
    public String getParameterValue(String name) throws RemoteException {
        return dbHandler.queryByNameForString(name);
    }

    @Override
    public int setParameterValue(String name, String value) throws RemoteException {
        return dbHandler.update(name, value);
    }
}
