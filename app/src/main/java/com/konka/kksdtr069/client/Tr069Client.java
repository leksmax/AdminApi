package com.konka.kksdtr069.client;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.konka.kksdtr069.observer.DBObserver;
import com.konka.kksdtr069.observer.ProtocolObserver;
import com.konka.kksdtr069.receiver.NetObserver;
import com.konka.kksdtr069.util.LogUtils;

public class Tr069Client extends AppCompatActivity {

    private static final String TAG = "Tr069_Client";

    private DBObserver dbObserver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.i(TAG, "TR069_Tr069Client start.");
        init();
    }

    public void init() {
        getLifecycle().addObserver(ProtocolObserver.getInstance());
        getLifecycle().addObserver(NetObserver.getInstance());
        dbObserver = DBObserver.getInstance();
        dbObserver.registerDBObserver(dbObserver);
    }

    @Override
    public void onDestroy() {
        LogUtils.i(TAG, "TR069_Tr069Client destroy.");
        release();
        super.onDestroy();
    }

    private void release() {
        dbObserver.unRegisterDBObserver(dbObserver);
    }
}
