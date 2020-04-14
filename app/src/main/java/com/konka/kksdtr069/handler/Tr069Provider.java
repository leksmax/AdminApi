package com.konka.kksdtr069.handler;


import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.konka.kksdtr069.util.LogUtil;
import com.konka.kksdtr069.util.PropertyUtil;

public class Tr069Provider extends ContentProvider {

    private static final String AUTHORITY = "tr069";
    private static final String URI_AUTH = "content://tr069/datamodel";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_VALUE = "value";
    private static final String SELECTION = "name=?";
    private static final int SINGLE_ROW = 101;
    private static final int ALL_ROW = 102;
    private static final String TAG = Tr069Provider.class.getSimpleName();

    private static UriMatcher sUriMatcher;
    private SQLiteDatabase db;

    private String[][] props = {
            {"ro.mac", "Device.LAN.MACAddress"},
            {"ro.build.version.incremental", "Device.DeviceInfo.SoftwareVersion"},
            {"ro.product.model", "Device.DeviceInfo.ModelName"},
            {"ro.serialno", "Device.X_CMCC_OTV.STBInfo.STBID"},
            {"ro.product.model", "Device.DeviceInfo.ProductClass"},
            {"ro.serialno", "Device.DeviceInfo.SerialNumber"}
            // {"ro.mac", "Device.X_CMCC_OTV.ServiceInfo.UserID"}
    };

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, "datamodel", ALL_ROW);
        sUriMatcher.addURI(AUTHORITY, "datamodel/*", SINGLE_ROW);
    }

    public boolean onCreate() {
        LogUtil.i(TAG, "provider onCreate");
        db = new DBHelper(getContext()).getWritableDatabase();
        initData();
        return true;
    }

    private void initData() {
        LogUtil.d(TAG, "initData");
        int ret = -100;
        String prop, dname;

        for (String[] s : props) {
            prop = s[0];
            dname = s[1];

            String propValue = "";
            if (dname.contains("Device.X_CMCC_OTV.ServiceInfo.UserID")) {
                propValue = PropertyUtil.getProperty(prop).replace(":", "")
                        .toUpperCase();
            } else if (dname.contains("Device.DeviceInfo.SoftwareVersion")) {
                propValue = PropertyUtil.formatSoftwareVersion();
            } else {
                propValue = PropertyUtil.getProperty(prop);
            }
            ContentValues cv = new ContentValues();
            cv.put("value", propValue);
            ret = update(Uri.parse(URI_AUTH), cv, "name=?", new String[]{dname});
            LogUtil.i(TAG, "update:{" + dname + "} ret=" + ret);
        }
        updateDb("Device.ManagementServer.ConnectionRequestUsername", "cpe");
        updateDb("Device.ManagementServer.ConnectionRequestPassword", "cpe");

        savedUpdateValue("Device.X_CMCC_OTV.ServiceInfo.UserID",
                PropertyUtil.getProperty("ro.mac").replace(":", "").toUpperCase());
        savedUpdateValue("Device.ManagementServer.PeriodicInformEnable", "true");
        savedUpdateValue("Device.ManagementServer.PeriodicInformInterval", "7200");
        savedUpdateValue("Device.ManagementServer.PeriodicInformTime",
                "2018-10-24 10:24:24");

        updateDb("Device.ManagementServer.STUNMaximumKeepAlivePeriod", 50);
        updateDb("Device.ManagementServer.STUNMinimumKeepAlivePeriod", 50);
    }

    private void savedUpdateValue(String paramName, String defaultValue) {
        String savedValue = "";
        Cursor cursor = db.query("datamodel", new String[]{"value"}, "name=?",
                new String[]{paramName}, null, null, null, null);
        if (cursor != null && cursor.moveToNext()) {
            savedValue = cursor.getString(cursor.getColumnIndex("value"));
            LogUtil.d(TAG, "query updated value, " + paramName + " = " + savedValue);
            cursor.close();
        }
        if (savedValue.equals("")) {
            LogUtil.d(TAG, "updated value is empty, update default value");
            updateDb(paramName, defaultValue);
        }
    }

    private void updateDb(String dname, String dvalue) {
        // TODO Auto-generated method stub
        LogUtil.i(TAG, "updateDb");
        int ret = -100;

        ContentValues cv = new ContentValues();
        cv.put("value", dvalue);
        ret = update(Uri.parse(URI_AUTH), cv, "name=?", new String[]{dname});
        LogUtil.i(TAG, "update:{" + dname + ", " + dvalue + "} ret=" + ret);
    }

    private void updateDb(String dname, boolean dvalue) {
        // TODO Auto-generated method stub
        LogUtil.i(TAG, "updateDb");
        int ret = -100;

        ContentValues cv = new ContentValues();
        cv.put("value", dvalue);
        ret = update(Uri.parse(URI_AUTH), cv, "name=?", new String[]{dname});
        LogUtil.i(TAG, "update:{" + dname + "} ret=" + ret);
    }

    private void updateDb(String dname, int dvalue) {
        // TODO Auto-generated method stub
        LogUtil.i(TAG, "updateDb");
        int ret = -100;

        ContentValues cv = new ContentValues();
        cv.put("value", dvalue);
        ret = update(Uri.parse(URI_AUTH), cv, "name=?", new String[]{dname});
        LogUtil.i(TAG, "update:{" + dname + "} ret=" + ret);
    }


    public String getType(Uri uri) {
        LogUtil.i(TAG, "getType(" + uri.toString() + ")");
        return null;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        LogUtil.i(TAG, "query(" + uri.toString() + ")");
        String select = null;
        String[] selectArgs = null;
        String table = (String) uri.getPathSegments().get(0);
        switch (sUriMatcher.match(uri)) {
            case ALL_ROW:
                select = selection;
                selectArgs = selectionArgs;
                break;

            case SINGLE_ROW:
                select = SELECTION;
                selectArgs = new String[]{(String) uri.getPathSegments().get(1)};
                break;
        }
        return db.query(table, projection, select, selectArgs, null, null, sortOrder);
    }

    public Uri insert(Uri uri, ContentValues values) {
        LogUtil.i(TAG, "insert(" + uri.toString() + ")");
        long r = -1;
        String table = (String) uri.getPathSegments().get(0);
        r = db.insert(table, null, values);
        LogUtil.i(TAG, "insert " + r);
        return Uri.withAppendedPath(uri, r + "");
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        LogUtil.i(TAG, "delete(" + uri.toString() + ")");
        String select = null;
        String[] selectArgs = null;
        String table = (String) uri.getPathSegments().get(0);
        switch (sUriMatcher.match(uri)) {
            case ALL_ROW:
                select = selection;
                selectArgs = selectionArgs;
                break;

            case SINGLE_ROW:
                select = SELECTION;
                selectArgs = new String[]{(String) uri.getPathSegments().get(1)};
                break;
        }
        return db.delete(table, select, selectArgs);
    }

    public int update(Uri uri, ContentValues cv, String selection, String[] selectionArgs) {
        String name = cv.get(COLUMN_NAME) == null ? "" : cv.get(COLUMN_NAME).toString();
        String value = cv.get(COLUMN_VALUE) == null ? "" : cv.get(COLUMN_VALUE).toString();
        if ((name == null) || (name.equals(""))) {
            if (selectionArgs != null) {
                name = selectionArgs[0];
                cv.put(COLUMN_NAME, name);
            }
        }
        LogUtil.i(TAG, "update(" + uri.toString() + ")");
        LogUtil.i(TAG, "name=[" + name + "]");
        LogUtil.d(TAG, "value=[" + value + "]");
        String select = null;
        String selectArgs[] = null;
        String table = (String) uri.getPathSegments().get(0);
        switch (sUriMatcher.match(uri)) {
            case ALL_ROW:
                select = selection;
                selectArgs = selectionArgs;
                break;

            case SINGLE_ROW:
                select = SELECTION;
                selectArgs = new String[]{(String) uri.getPathSegments().get(1)};
                break;
        }
        int result = db.update(table, cv, select, selectArgs);
        LogUtil.d(TAG, "update result : " + result);
        return result;
    }

    public void onLowMemory() {
        super.onLowMemory();
        if (db != null) {
            db.close();
        }
    }

    private boolean checkData(Uri uri, Object name) {
        StringBuffer tmp = new StringBuffer();
        tmp.append(uri);
        tmp.append("/");
        tmp.append(name.toString());
        Uri uri_tmp = Uri.parse(tmp.toString());
        LogUtil.i(TAG, "uri_tmp=[" + uri_tmp + "]");
        Cursor mCursor = query(uri_tmp, null, null, null, null);
        if (mCursor != null) {
            if (!mCursor.moveToFirst()) {
                mCursor.close();
                return false;
            }
            mCursor.close();
            return true;
        }
        LogUtil.e(TAG, "query failed!! ");
        return false;
    }

}