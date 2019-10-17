package com.konka.kksdtr069.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPParameter;

public class DataBaseUtils {

    public static final String URI_AUTH = "content://tr069/datamodel";
    public static final Uri URI = Uri.parse(URI_AUTH);
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_VALUE = "value";
    private static final String SELECTION = "name=?";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_WRITABLE = "writable";
    private static final String COLUMN_SECURE = "secure";
    private static final String COLUMN_NOTIFICATION = "notification";


    private Context context;

    private static DataBaseUtils instance;

    private DataBaseUtils(Context context) {
        this.context = context;
    }

    public static synchronized DataBaseUtils getInstance(Context context) {
        if (instance == null) {
            instance = new DataBaseUtils(context);
        }
        return instance;
    }

    public int updateDb(String dname, String dvalue) {
        ContentValues cv = new ContentValues();
        cv.put("value", dvalue);
        return context.getContentResolver().update(URI, cv, SELECTION, new String[]{dname});
    }

    public Cursor query(String name) {
        return context.getContentResolver().query(Uri.withAppendedPath(URI, name), null,
                SELECTION, new String[]{name}, null);
    }

    public String queryValue(String name) {
        Cursor c = null;
        String value = "";
        c = context.getContentResolver().query(Uri.withAppendedPath(URI, name), null,
                SELECTION, new String[]{name}, null);
        if (c != null && c.moveToNext()) {
            value = c.getString(c.getColumnIndex(COLUMN_VALUE));
            c.close();
        }
        return value;
    }

    public CWMPParameter cursorToParameter(Cursor c) {
        return new CWMPParameter(c.getString(c.getColumnIndex(COLUMN_NAME)),
                c.getString(c.getColumnIndex(COLUMN_VALUE)),
                c.getString(c.getColumnIndex(COLUMN_TYPE)),
                c.getInt(c.getColumnIndex(COLUMN_WRITABLE)) == 1,
                c.getInt(c.getColumnIndex(COLUMN_SECURE)) == 1,
                c.getInt(c.getColumnIndex(COLUMN_NOTIFICATION)));
    }

    public boolean isDifferentFromDB(String value, String s) {
        String v = queryValue(s);
        return !value.equals(v);
    }
}
