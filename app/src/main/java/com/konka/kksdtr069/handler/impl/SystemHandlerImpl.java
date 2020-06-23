package com.konka.kksdtr069.handler.impl;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;

import com.konka.kksdtr069.base.BaseApplication;
import com.konka.kksdtr069.handler.SystemHandler;
import com.konka.kksdtr069.util.DownloadUtil;
import com.konka.kksdtr069.util.LinuxUtil;
import com.konka.kksdtr069.util.LogUtil;
import com.konka.kksdtr069.util.PropertyUtil;

import net.sunniwell.cwmp.protocol.sdk.aidl.AppID;
import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPDownloadRequest;
import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPDownloadResult;
import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPParameter;
import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPSetParameterAttributesStruct;
import net.sunniwell.cwmp.protocol.sdk.aidl.ICWMPProtocolService;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SystemHandlerImpl implements SystemHandler {

    private DBHandlerImpl dbHandler;

    private Context context;

    private static SystemHandlerImpl instance;

    public static final String TAG = SystemHandlerImpl.class.getSimpleName();

    private SystemHandlerImpl() {
        context = BaseApplication.instance.getApplicationContext();
        dbHandler = DBHandlerImpl.getInstance();
    }

    public static SystemHandlerImpl getInstance() {
        if (instance == null) {
            instance = new SystemHandlerImpl();
        }
        return instance;
    }

    @Override
    public void appUninstall(final List<AppID> list, final ICWMPProtocolService protocolService) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                for (AppID app : list) {
                    String resultCode = "";
                    try {
                        if (!LinuxUtil.isAppInstalled(app.packageName)) {
                            app.result = 2;
                            LogUtil.d(TAG, "uninstalled apk does not exist");
                            continue;
                        }
                        resultCode = LinuxUtil.execCommandForString("pm", "uninstall", app.packageName);
                        if (resultCode.contains("Failure")) {
                            app.result = 0;
                        } else {
                            app.result = 1;
                        }
                    } catch (InterruptedException | IOException e) {
                        e.printStackTrace();
                        app.result = 0;
                    }
                    LogUtil.d(TAG, "uninstall apk " + app.packageName
                            + " result : " + app.result + " resultCode : " + resultCode);
                }
                try {
                    DownloadUtil.reportApkInfo(dbHandler, protocolService, false);
                    protocolService.onUninstallFinish(list);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public void download(CWMPDownloadRequest request, final ICWMPProtocolService protocolService) {
        LogUtil.d(TAG, "download type : " + request.getType() + "\n" +
                "平台下发的url : " + request.getUrl() + "\n" +
                "commandKey : " + request.getCommandKey() + "\n" +
                "file_md5 : " + request.getMd5() + "\n" +
                "is silent upgrade: " + request.isSilent() + "\n" +
                "is force upgrade: " + request.isForce());
        Intent intent = new Intent();
        intent.putExtra("type", request.getType());
        intent.putExtra("update_url", request.getUrl());
        intent.putExtra("commandKey", request.getCommandKey());
        intent.putExtra("file_md5", request.getMd5());
        final CWMPDownloadResult result = new CWMPDownloadResult();
        result.setType(request.getType());
        result.setCommandKey(request.getCommandKey());
        switch (request.getType()) {
            case CWMPDownloadRequest.TYPE_FIRMWARE:
                LogUtil.d(TAG, "start update firmware");
                // 固件升级
                intent.setAction("com.android.system.update");
                intent.putExtra("silent_upgrade", request.isSilent());
                intent.putExtra("force_upgrade", request.isForce());
                context.sendBroadcast(intent);
                // result.setState(CWMPDownloadResult.STATE_SUCCESS);

                // 广播升级参数
                String sfversion = PropertyUtil.getProperty("ro.build.version.incremental");
                Intent updateIntent = new Intent();
                updateIntent.setAction("android.intent.action.TRANSFER_INFORM");
                Bundle bundle = new Bundle();
                bundle.putSerializable("protocolService", (Serializable) protocolService);
                bundle.putSerializable("cwmpDownloadResult", (Serializable) result);
                bundle.putString("oldSfVersion",sfversion);
                updateIntent.putExtras(bundle);
                context.sendBroadcast(updateIntent);
                LogUtil.d(TAG, "send update firmware parameter broadcast");
                break;
            case CWMPDownloadRequest.TYPE_APK:
                // 应用升级或安装
                LogUtil.d(TAG, "start update or install apk");
                final String filename = TextUtils.isEmpty(request.getFileName()) ? "download.apk" :
                        request.getFileName();
                final String path = "/data/data/com.konka.kksdtr069/cache/apk/" + filename;
                LinuxUtil.removeSubFile("/data/data/com.konka.kksdtr069/cache/apk/");
                new DownloadUtil.httpClient(new DownloadUtil.MCallBack() {
                    @Override
                    public void onSuccess() {
                        result.setState(CWMPDownloadResult.STATE_SUCCESS);
                        try {
                            LinuxUtil.execCommand("chmod", "666", path);
                            int status = LinuxUtil.execCommand("pm", "install", "-r", path);
                            if (status == 0) {
                                String oldValue = dbHandler.queryByNameForString(
                                        "Device.X_00E0FC.SoftwareVersionList");
                                LogUtil.d(TAG, oldValue);
                                String newValue = "";
                                if (TextUtils.isEmpty(oldValue)) {
                                    String[] strings;
                                    strings = DownloadUtil.getApkInfo(context, path);
                                    newValue = strings[0] + "|" + strings[1] + "|" + strings[2];
                                } else {
                                    String[] strings = DownloadUtil.getApkInfo(context, path);
                                    if (oldValue.contains(strings[2])) {
                                        String[] apkInfo = oldValue.split(",");
                                        StringBuilder newValueBuilder = new StringBuilder();
                                        for (String i : apkInfo) {
                                            if (i.contains(strings[2])) {
                                                newValueBuilder.append(strings[0] + "|" + strings[1]
                                                        + "|" + strings[2]);
                                            } else {
                                                newValueBuilder.append(i);
                                            }
                                        }
                                        newValue = newValueBuilder.toString();
                                    }
                                }
                                LogUtil.d(TAG, "down apk info : " + newValue);
                                DownloadUtil.reportApkInfo(dbHandler, protocolService, false);
                            }
                        } catch (IOException | InterruptedException | RemoteException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(String string) {
                        result.setState(CWMPDownloadResult.STATE_FAILURE);
                        result.setReason(string);
                    }
                }).doWork(request.getUrl(), path);
                try {
                    protocolService.onDownloadFinish(result);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }


    @Override
    public void FactoryReset() {
        LogUtil.d(TAG, "factoryReset");
        Intent intent = new Intent("android.intent.action.MASTER_CLEAR");
        context.sendBroadcast(intent);
    }

    @Override
    public void reboot() {
        LogUtil.d(TAG, "reboot");
        Intent intent = new Intent(Intent.ACTION_REBOOT);
        intent.putExtra("nowait", 1);
        intent.putExtra("interval", 1);
        intent.putExtra("window", 0);
        context.sendBroadcast(intent);
    }

    @Override
    public void onLogin(int type, boolean isSuccess) {
        String rs = type == 1 ? "重启时登入" : "首次开机登入";
        LogUtil.d(TAG, "onLogin()" + "\n"
                + "Login Type: " + rs + "\n"
                + "Login Success: " + isSuccess);
    }
}
