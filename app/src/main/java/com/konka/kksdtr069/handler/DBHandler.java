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

    Cursor queryByNameForCursor(String name) throws RemoteException;

    String queryByNameForString(String name) throws RemoteException;

    Cursor fuzzyQueryByName(String path) throws RemoteException;

    Cursor fuzzyQueryByNames(String[] names) throws RemoteException;

    CWMPParameter cursorToCWMPParameter(Cursor cursor) throws RemoteException;

    List<CWMPParameter> cursorToList(Cursor cursor) throws RemoteException;

    boolean isDifferentFromDB(String targetValue, String queryStr) throws RemoteException;
}
