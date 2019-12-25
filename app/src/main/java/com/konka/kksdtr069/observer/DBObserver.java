package com.konka.kksdtr069.observer;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

import com.konka.kksdtr069.base.BaseApplication;
import com.konka.kksdtr069.handler.impl.DBHandlerImpl;
import com.konka.kksdtr069.util.LogUtils;

import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPParameter;

import java.util.List;

public class DBObserver extends ContentObserver {

    private Context context;

    public static final String TAG = DBObserver.class.getSimpleName();

    private static final Uri OBSERVER_URI = Uri.withAppendedPath(DBHandlerImpl.URI,
            "notification");

    private List<CWMPParameter> parameterCacheList;

    private static DBObserver instance;

    private ProtocolObserver protocolPresenter = ProtocolObserver.getInstance();

    private DBObserver(Handler handler) {
        super(handler);
        this.context = BaseApplication.instance.getApplicationContext();
    }

    public static DBObserver getInstance() {
        if (instance == null) {
            instance = new DBObserver(new Handler());
        }
        return instance;
    }

    public void notifyChange(List<CWMPParameter> parameterCacheList) {
        this.parameterCacheList = parameterCacheList;
        context.getContentResolver().notifyChange(OBSERVER_URI, this);
    }

    public void registerDBObserver() {
        context.getContentResolver().registerContentObserver(OBSERVER_URI,
                false, this);
    }

    public void unRegisterDBObserver() {
        context.getContentResolver().unregisterContentObserver(this);
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        protocolPresenter.valueChanged(parameterCacheList);
    }
}
