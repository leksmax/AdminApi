package com.konka.kksdtr069.handler;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;

import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPParameter;

import java.util.List;

public interface DBHandler {

    int update(String name, String value) throws RemoteException;

    int update(CWMPParameter parameter) throws RemoteException;

    int delete(String name) throws RemoteException;

    Uri insert(ContentValues cv) throws RemoteException;

    CWMPParameter queryByName(String name) throws RemoteException;

    String queryByNameForString(String name) throws RemoteException;

    List<CWMPParameter> fuzzyQueryByName(String path) throws RemoteException;

    List<CWMPParameter> queryInformParameters() throws RemoteException;

    boolean isDifferentFromDB(String targetValue, String queryStr) throws RemoteException;
}
