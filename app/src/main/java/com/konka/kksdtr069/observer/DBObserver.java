package com.konka.kksdtr069.observer;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.RemoteException;

import com.konka.kksdtr069.base.BaseApplication;
import com.konka.kksdtr069.handler.impl.DBHandlerImpl;

import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPParameter;

import java.lang.ref.WeakReference;
import java.util.List;

public class DBObserver extends ContentObserver {

    private Context context;

    private static final Uri OBSERVER_URI = Uri.withAppendedPath(DBHandlerImpl.URI, "2");

    private List<CWMPParameter> parameterCacheList;

    private static DBObserver instance;

    private WeakReference<ProtocolObserver> protocolPresenter =
            new WeakReference<ProtocolObserver>(ProtocolObserver.getInstance());

    private DBObserver(Handler handler) {
        super(handler);
        this.context = BaseApplication.getInstance().getApplicationContext();
    }

    public static DBObserver getInstance() {
        if (instance == null) {
            synchronized (DBObserver.class) {
                if (instance == null) {
                    instance = new DBObserver(new Handler());
                }
            }
        }
        return instance;
    }

    public void notifyChange(ContentObserver dbObserver,
                             List<CWMPParameter> parameterCacheList) {
        this.parameterCacheList = parameterCacheList;
        context.getContentResolver().notifyChange(OBSERVER_URI, dbObserver);
    }

    public void registerDBObserver(ContentObserver dbObserver) {
        context.getContentResolver().registerContentObserver(OBSERVER_URI,
                false, dbObserver);
    }

    public void unRegisterDBObserver(ContentObserver dbObserver) {
        context.getContentResolver().unregisterContentObserver(dbObserver);
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        try {
            protocolPresenter.get().valueChanged(this.parameterCacheList);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
