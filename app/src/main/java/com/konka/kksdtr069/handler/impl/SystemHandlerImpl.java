package com.konka.kksdtr069.handler.impl;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.text.TextUtils;

import com.konka.amlogicmiddleware.logotool.LogoTools;
import com.konka.kksdtr069.base.BaseApplication;
import com.konka.kksdtr069.handler.SystemHandler;
import com.konka.kksdtr069.util.DownloadUtil;
import com.konka.kksdtr069.util.LinuxUtils;
import com.konka.kksdtr069.util.LogUtils;

import net.sunniwell.cwmp.protocol.sdk.aidl.AppID;
import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPDownloadRequest;
import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPDownloadResult;
import net.sunniwell.cwmp.protocol.sdk.aidl.ICWMPProtocolService;

import java.io.IOException;
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
    public void appUninstall(List<AppID> list, ICWMPProtocolService protocolService) {
        List<AppID> resultList = new ArrayList<AppID>();
        int result = -1;
        for (AppID app : list) {
            try {
                LogUtils.d(TAG, "uninstall apk : " + app.packageName);
                // 检查卸载的apk是否存在
                List<String> apkList = LinuxUtils.exeCommand("ls -l data/data");
                String apks = apkList.toString();
                if (!(apks.contains(app.packageName))) {
                    result = 2;
                    LogUtils.d(TAG, "uninstalled apk does not exist");
                }
                if (result != 2) {
                    result = LinuxUtils.execCommand("pm", "uninstall", app.packageName);
                    result = result == 0 ? 1 : 0; // 0：卸载失败；1：卸载成功
                }
                LogUtils.d(TAG, "uninstall apk result : " + result);
                resultList.add(new AppID(app.packageName, result));
                protocolService.onUninstallFinish(resultList);
            } catch (RemoteException | InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void download(CWMPDownloadRequest request, ICWMPProtocolService protocolService) {
        LogUtils.d(TAG, "download type : " + request.getType() + "\n" +
                " url : " + request.getUrl() + "\n" +
                "commandKey : " + request.getCommandKey() + "\n" +
                "file_md5 : " + request.getMd5());
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
                LogUtils.d(TAG, "start update firmware");
                // 固件升级
                intent.setAction("com.android.system.update");
                intent.putExtra("silent_upgrade", request.isSilent());
                intent.putExtra("force_upgrade", request.isForce());
                context.sendBroadcast(intent);
                result.setState(CWMPDownloadResult.STATE_SUCCESS);
                LogUtils.d(TAG, "update firmware finish");
                break;
            case CWMPDownloadRequest.TYPE_APK:
                // 应用升级或安装
                LogUtils.d(TAG, "start update or install apk");
                final String filename = TextUtils.isEmpty(request.getFileName()) ? "download.apk" :
                        request.getFileName();
                final String path = "/data/data/com.konka.kksdtr069/cache/apk/" + filename;
                LinuxUtils.removeSubFile("/data/data/com.konka.kksdtr069/cache/apk/");
                new DownloadUtil.httpClient(new DownloadUtil.MCallBack() {
                    @Override
                    public void onSuccess() {
                        result.setState(CWMPDownloadResult.STATE_SUCCESS);
                        try {
                            LinuxUtils.execCommand("chmod", "666", path);
                            int status = LinuxUtils.execCommand("pm", "install", "-r", path);
                            if (status == 0) {
                                String oldValue = dbHandler.queryByNameForString(
                                        "Device.X_00E0FC.SoftwareVersionList");
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
                                LogUtils.d(TAG, "down apk info : " + newValue);
                                dbHandler.update("Device.X_00E0FC.SoftwareVersionList", newValue);
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
                break;
            default:
                break;
        }
        try {
            protocolService.onDownloadFinish(result);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
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
