package com.konka.kksdtr069.handler.impl;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.RemoteException;

import com.konka.kksdtr069.base.BaseApplication;
import com.konka.kksdtr069.handler.ParameterHandler;
import com.konka.kksdtr069.util.LogUtil;
import com.konka.kksdtr069.util.PropertyUtil;

import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPDownloadRequest;
import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPDownloadResult;
import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPParameter;
import net.sunniwell.cwmp.protocol.sdk.aidl.ICWMPProtocolService;

import java.util.List;

public class ParameterHandlerImpl implements ParameterHandler {

    private static ParameterHandlerImpl instance;

    public static final Uri URI = DBHandlerImpl.URI;

    private static DBHandlerImpl dbHandler;

    public static final String TAG = ParameterHandlerImpl.class.getSimpleName();

    private ParameterHandlerImpl() {
        dbHandler = DBHandlerImpl.getInstance();
        LogUtil.d(TAG, "new DBhandlerImpl for ParameterHandlerImpl");
    }

    public static ParameterHandlerImpl getInstance() {
        if (instance == null) {
            synchronized (ParameterHandlerImpl.class) {
                if (instance == null) {
                    instance = new ParameterHandlerImpl();
                }
            }
        }
        return instance;
    }

    @Override
    public List<CWMPParameter> getParameterBySuperName(String path) throws RemoteException {
        return dbHandler.fuzzyQueryByName(path);
    }

    @Override
    public List<CWMPParameter> getInformParameters() throws RemoteException {
        updateSoftwareVersionDisplay();
        isTransferCompleted();
        return dbHandler.queryInformParameters();
    }

    @Override
    public CWMPParameter getParameter(String name) throws RemoteException {
        return dbHandler.queryByName(name);
    }

    @Override
    public String getParameterValue(String name) throws RemoteException {
        return dbHandler.queryByNameForString(name);
    }

    @Override
    public int setParameterValue(String name, String value) throws RemoteException {
        int result = 0;
        if (name.equals("Device.LAN.IPAddress") || name.equals("Device.LAN.DefaultGateway") ||
                name.equals("Device.LAN.DNSServers2")) {
            String addressingType = dbHandler.queryByNameForString("Device.LAN.AddressingType");
            LogUtil.d(TAG, "addressing type = " + addressingType);
            if (addressingType.equals("STATIC")) {
                result = dbHandler.update(name, value);
                LogUtil.d(TAG, "update static ip result = " + result);
            }
        } else {
            result = dbHandler.update(name, value);
        }
        return result;

    }

    private void isTransferCompleted() {
        // 广播升级的结果
        String sfversion = PropertyUtil.getProperty("ro.build.version.incremental");
        Context context = BaseApplication.instance.getApplicationContext();
        Intent intent = new Intent();
        intent.setAction("android.intent.action.TRANSFER_COMPLETED");
        if ("1.1.20".equals(sfversion)) {
            intent.putExtra("isTransferCompleted", "true");
        } else {
            intent.putExtra("isTransferCompleted", "false");
        }
        context.sendBroadcast(intent);
        LogUtil.d(TAG, "send if transfer completed broadcast");
    }

    private void updateSoftwareVersionDisplay() throws RemoteException {
        // 升级完成之后，数据进行初始化对软件版本号进行规划化显示
        String sfversion = PropertyUtil.formatSoftwareVersion();
        dbHandler.update("Device.DeviceInfo.SoftwareVersion", sfversion);
    }
}
