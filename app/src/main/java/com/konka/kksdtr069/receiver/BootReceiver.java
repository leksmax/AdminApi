package com.konka.kksdtr069.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.konka.kksdtr069.service.Tr069Client;
import com.konka.kksdtr069.util.LogUtil;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "KONKA_BootReceiver";

    public BootReceiver() {
        super();
        LogUtil.d(TAG, "BootReceiver create");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.i(TAG, "startService Tr069Client.class");
        Intent i = new Intent(context, Tr069Client.class);
        context.startService(i);
    }
}
