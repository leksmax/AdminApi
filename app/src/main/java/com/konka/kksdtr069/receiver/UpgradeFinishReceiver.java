package com.konka.kksdtr069.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;

import com.konka.kksdtr069.handler.DBHandler;
import com.konka.kksdtr069.handler.impl.DBHandlerImpl;
import com.konka.kksdtr069.util.LogUtil;
import com.konka.kksdtr069.util.PropertyUtil;

public class UpgradeFinishReceiver extends BroadcastReceiver {

    public static final String TAG = UpgradeFinishReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Boolean isUpgradeFinished = intent.getBooleanExtra("isUpgradeFinished", false);
            if (isUpgradeFinished) {
                DBHandler dbHandler = DBHandlerImpl.getInstance();
                String sfversion = PropertyUtil.getProperty("ro.build.version.incremental");
                sfversion = PropertyUtil.formatSoftwareVersion(sfversion);
                dbHandler.update("Device.DeviceInfo.SoftwareVersion", sfversion);
                LogUtil.d(TAG, "finished update, software version: " +
                        dbHandler.queryByNameForString("Device.DeviceInfo.SoftwareVersion"));
            } else {
                LogUtil.d(TAG, "update failure");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
