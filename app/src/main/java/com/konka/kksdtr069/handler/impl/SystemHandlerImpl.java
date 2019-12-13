package com.konka.kksdtr069.handler.impl;

import android.content.Context;
import android.content.Intent;

import com.konka.kksdtr069.base.BaseApplication;
import com.konka.kksdtr069.handler.SystemHandler;
import com.konka.kksdtr069.observer.ProtocolObserver;
import com.konka.kksdtr069.util.LinuxUtils;
import com.konka.kksdtr069.util.LogUtils;

import net.sunniwell.cwmp.protocol.sdk.aidl.AppID;
import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPDownloadRequest;

import java.util.ArrayList;
import java.util.List;

public class SystemHandlerImpl implements SystemHandler {

    private ProtocolObserver protocolObserver = ProtocolObserver.getInstance();

    private Context context;

    private static SystemHandlerImpl instance;

    public static final String TAG = SystemHandlerImpl.class.getSimpleName();

    private SystemHandlerImpl() {
        context = BaseApplication.instance.getApplicationContext();
    }

    public static SystemHandlerImpl getInstance() {
        if (instance == null) {
            instance = new SystemHandlerImpl();
        }
        return instance;
    }

    @Override
    public void appUninstall(List<AppID> list) {
        List<AppID> resultList = new ArrayList<AppID>();
        for (AppID app : list) {
            int result = LinuxUtils.execCommand("pm", "uninstall", app.packageName);
            resultList.add(new AppID(app.packageName, result));
        }
        protocolObserver.uninstallFinish(resultList);
    }

    @Override
    public void download(CWMPDownloadRequest request) {
        LogUtils.i(TAG, "download type:" + request.getType()
                + " url:" + request.getUrl()
                + " commandKey:" + request.getCommandKey());
        Intent intent = new Intent();
        intent.putExtra("type", request.getType());
        intent.putExtra("url", request.getUrl());
        intent.putExtra("commandKey", request.getCommandKey());
        intent.setAction("com.android.system.update");
        context.sendBroadcast(intent);
    }

    @Override
    public void FactoryReset() {
        LogUtils.d(TAG, "factoryReset");
        Intent intent = new Intent("android.intent.action.MASTER_CLEAR");
        context.sendBroadcast(intent);
    }

    @Override
    public void reboot() {
        LogUtils.d(TAG, "reboot");
        Intent intent = new Intent(Intent.ACTION_REBOOT);
        intent.putExtra("nowait", 1);
        intent.putExtra("interval", 1);
        intent.putExtra("window", 0);
        context.sendBroadcast(intent);
    }

    @Override
    public void onLogin(int type, boolean isSuccess) {
        String rs = type == 1 ? "重启时登入" : "首次开机登入";
        LogUtils.d(TAG, "onLogin()" + "\n"
                + "Login Type: " + rs + "\n"
                + "Login Success: " + isSuccess);
    }
}
