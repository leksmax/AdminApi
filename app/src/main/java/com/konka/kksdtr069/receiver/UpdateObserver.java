package com.konka.kksdtr069.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.RemoteException;

import com.konka.kksdtr069.base.BaseApplication;
import com.konka.kksdtr069.base.BaseObserver;
import com.konka.kksdtr069.util.DownloadUtil;
import com.konka.kksdtr069.util.LogUtil;

import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPDownloadResult;
import net.sunniwell.cwmp.protocol.sdk.aidl.ICWMPProtocolService;

public class UpdateObserver extends BaseObserver {

    public static String TAG = UpdateObserver.class.getSimpleName();

    private static UpdateObserver instance;

    private Context context;

    private UpdateReceiver mUpdateReceiver;

    private UpdateObserver() {
        context = BaseApplication.instance.getApplicationContext();
    }

    public static UpdateObserver getInstance() {
        if (instance == null) {
            instance = new UpdateObserver();
        }
        return instance;
    }

    public void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.TRANSFER_COMPLETED");
        filter.addAction("android.intent.action.TRANSFER_INFORM");
        mUpdateReceiver = new UpdateReceiver();
        context.registerReceiver(mUpdateReceiver, filter);
    }

    public void unregisterReceiver() {
        if (mUpdateReceiver != null) {
            context.unregisterReceiver(mUpdateReceiver);
        }
    }

    private class UpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            ICWMPProtocolService protocolService = null;
            CWMPDownloadResult result = null;
            if ("android.intent.action.TRANSFER_INFORM".equals(intent.getAction())) {
                Bundle bundle = intent.getExtras();
                result = (CWMPDownloadResult) bundle.get("cwmpDownloadResult");
                protocolService = (ICWMPProtocolService) bundle.get("protocolService");
                LogUtil.d(TAG, "update inform: " + "\n"
                        + "CWMPResult = " + result + "\n"
                        + "ICWMPProtocolService = " + protocolService);
            }
            if ("android.intent.action.TRANSFER_COMPLETED".equals(intent.getAction())
                    && result != null && protocolService != null) {
                String isTransferCompleted = intent.getStringExtra("isTransferCompleted");
                if ("true".equals(isTransferCompleted)) {
                    result.setState(DownloadUtil.TRANSFER_SUCCESS);
                } else {
                    result.setState(DownloadUtil.TRANSFER_FAILURE);
                }
                try {
                    // 上报升级的结果
                    protocolService.onDownloadFinish(result);
                    LogUtil.d(TAG, "report update result: " + result.getState());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
