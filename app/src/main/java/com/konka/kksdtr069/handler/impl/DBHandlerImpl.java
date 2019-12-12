package com.konka.kksdtr069.handler.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;

import com.konka.kksdtr069.base.BaseApplication;
import com.konka.kksdtr069.handler.DBHandler;

import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPParameter;

import java.util.ArrayList;
import java.util.List;

public class DBHandlerImpl implements DBHandler {

    public static final String URI_AUTH = "content://tr069/datamodel";

    public static final Uri URI = Uri.parse(URI_AUTH);

    public static final String COLUMN_NAME = "name";

    public static final String COLUMN_VALUE = "value";

    public static final String COLUMN_TYPE = "type";

    public static final String COLUMN_WRITABLE = "writable";

    public static final String COLUMN_SECURE = "secure";

    public static final String COLUMN_NOTIFICATION = "notification";

    private static final String SELECTION = "name=?";

    private Context context;

    private static DBHandlerImpl instance;

    private DBHandlerImpl() {
        this.context = BaseApplication.getInstance().getApplicationContext();
    }

    public static DBHandlerImpl getInstance() {
        if (instance == null) {
            synchronized (DBHandlerImpl.class) {
                if (instance == null) {
                    instance = new DBHandlerImpl();
                }
            }
        }
        return instance;
    }

    @Override
    public int update(String name, String value) throws RemoteException {
        ContentValues cv = new ContentValues();
        cv.put("value", value);
        return context.getContentResolver().update(URI, cv, SELECTION, new String[]{name});
    }

    @Override
    public int update(CWMPParameter parameter) throws RemoteException {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_VALUE, parameter.getValue());
        cv.put(COLUMN_TYPE, parameter.getType());
        cv.put(COLUMN_WRITABLE, parameter.isWritable() ? 1 : 0);
        cv.put(COLUMN_SECURE, parameter.isSecure() ? 1 : 0);
        cv.put(COLUMN_NOTIFICATION, parameter.getNotification());
        return context.getContentResolver().update(URI, cv, SELECTION,
                new String[]{parameter.getName()});
    }

    @Override
    public Cursor queryByNameForCursor(String name) throws RemoteException {
        return context.getContentResolver().query(Uri.withAppendedPath(URI, name), null,
                SELECTION, new String[]{name}, null);
    }

    @Override
    public String queryByNameForString(String name) throws RemoteException {
        Cursor cursor = null;
        String value = "";
        cursor = context.getContentResolver().query(Uri.withAppendedPath(URI, name), null,
                SELECTION, new String[]{name}, null);
        if (cursor != null && cursor.moveToNext()) {
            value = cursor.getString(cursor.getColumnIndex(COLUMN_VALUE));
            cursor.close();
        }
        return value;
    }

    @Override
    public Cursor fuzzyQueryByName(String path) throws RemoteException {
        return context.getContentResolver().query(URI, null,
                "name like '%" + path + "%'", null, null);
    }

    @Override
    public Cursor fuzzyQueryByNames(String[] names) throws RemoteException {
        StringBuilder selection = new StringBuilder();
        selection.append("name REGEXP ");
        for (int i = 0; i < names.length; i++) {
            if (i == 0) {
                selection.append("' " + names[i] + " |");
            } else if (i == names.length - 1) {
                selection.append(" " + names[i] + " '");
            } else {
                selection.append(" " + names[i] + " |");
            }
        }
        return context.getContentResolver().query(URI, null, selection.toString()
                , null, null);
    }

    @Override
    public CWMPParameter cursorToCWMPParameter(Cursor cursor) throws RemoteException {
        return new CWMPParameter(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)),
                cursor.getString(cursor.getColumnIndex(COLUMN_VALUE)),
                cursor.getString(cursor.getColumnIndex(COLUMN_TYPE)),
                cursor.getInt(cursor.getColumnIndex(COLUMN_WRITABLE)) == 1,
                cursor.getInt(cursor.getColumnIndex(COLUMN_SECURE)) == 1,
                cursor.getInt(cursor.getColumnIndex(COLUMN_NOTIFICATION)));
    }

    @Override
    public List<CWMPParameter> cursorToList(Cursor cursor) throws RemoteException {
        ArrayList<CWMPParameter> list = new ArrayList<CWMPParameter>();
        CWMPParameter parameter = null;
        while (cursor.moveToNext()) {
            parameter = cursorToCWMPParameter(cursor);
            list.add(parameter);
        }
        return list;
    }

    @Override
    public boolean isDifferentFromDB(String targetValue, String queryStr) throws RemoteException {
        String dbSourceValue = queryByNameForString(queryStr);
        return !targetValue.equals(dbSourceValue);
    }

    @Override
    public int delete(String name) throws RemoteException {
        return context.getContentResolver().delete(URI,
                "name like '%" + name + "%'", null);
    }

    @Override
    public Uri insert(ContentValues cv) throws RemoteException {
        return context.getContentResolver().insert(URI, cv);
    }
}
