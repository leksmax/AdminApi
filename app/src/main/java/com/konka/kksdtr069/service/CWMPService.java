package com.konka.kksdtr069.service;

import android.os.RemoteException;

import com.konka.kksdtr069.handler.SystemHandler;
import com.konka.kksdtr069.handler.impl.FunctionHandlerImpl;
import com.konka.kksdtr069.handler.impl.ParameterHandlerImpl;
import com.konka.kksdtr069.handler.impl.SystemHandlerImpl;

import net.sunniwell.cwmp.protocol.sdk.aidl.AppID;
import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPDownloadRequest;
import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPParameter;
import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPSetParameterAttributesStruct;
import net.sunniwell.cwmp.protocol.sdk.aidl.ICWMPNativeService;
import net.sunniwell.cwmp.protocol.sdk.aidl.SetParameterValuesFault;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class CWMPService extends ICWMPNativeService.Stub {

    private static final String TAG = "Tr069_CWMPService";

    private WeakReference<FunctionHandlerImpl> functionHandler =
            new WeakReference<FunctionHandlerImpl>(FunctionHandlerImpl.getInstance());

    private ParameterHandlerImpl parameterHandler = ParameterHandlerImpl.getInstance();

    private WeakReference<SystemHandler> systemHandler =
            new WeakReference<SystemHandler>(SystemHandlerImpl.getInstance());

    /**
     * @return 获取Inform报文需要的参数列表
     */
    @Override
    public List<CWMPParameter> getInformParameters() throws RemoteException {
        return parameterHandler.getInformParameters();
    }

    /**
     * @return 获取心跳需要上报的参数列表，同Inform报文的参数列表
     */
    @Override
    public List<CWMPParameter> getPeriodicParameters() throws RemoteException {
        return parameterHandler.getInformParameters();
    }

    /**
     * @param path 查询父对象的参数名，如Device
     * @return 父对象下所属所有子对象和参数，封装在List<CWMPParameter>中返回
     */
    @Override
    public List<CWMPParameter> getParameters(String path) throws RemoteException {
        return parameterHandler.getParameterBySuperName(path);
    }

    /**
     * @param name 输入需要查询的参数名称
     * @return 查询参数名对应的参数值
     * @Exception RemoteException
     */
    @Override
    public String getParameterValue(String name) throws RemoteException {
        return parameterHandler.getParameterValue(name);
    }

    /**
     * @param name  需要设置的参数名
     * @param value 设置参数名对应的参数值
     * @return 设置参数所在数据库对应表的行数
     */
    @Override
    public int setParameterValue(String name, String value) throws RemoteException {
        return parameterHandler.setParameterValue(name, value);
    }

    /**
     * @param name 参数名称
     * @return 获取指定参数名的CWMPParameter
     */
    @Override
    public CWMPParameter getParameter(String name) throws RemoteException {
        return parameterHandler.getParameter(name);
    }

    /**
     * 设置参数方式完成特殊功能，ping诊断、trace route诊断、远程抓包、终端测速、
     * 抓取上传日志、开关wifi、修改电视二维码显示
     *
     * @param list 请求实现功能，需要传入的设置参数列表
     * @return 功能完成后，返回参数
     */
    @Override
    public List<SetParameterValuesFault> setParameters(List<CWMPParameter> list) throws RemoteException {
        ArrayList<SetParameterValuesFault> faultList = new ArrayList<SetParameterValuesFault>();
        faultList.addAll(functionHandler.get().pingDiagnosis(list));
        faultList.addAll(functionHandler.get().traceRouteDiagnosis(list));
        faultList.addAll(functionHandler.get().remoteNetPacketCapture(list)); // 未实现
        faultList.addAll(functionHandler.get().terminalSpeedMeasurement(list)); // 未实现
        faultList.addAll(functionHandler.get().captureAndUploadLog(list)); // 未实现
        faultList.addAll(functionHandler.get().wifiEnable(list)); // 未实现
        faultList.addAll(functionHandler.get().modifyQRCodeDisplay(list)); // 未实现
        return faultList;
    }

    /**
     * 远程重启
     */
    @Override
    public void reboot() throws RemoteException {
        systemHandler.get().reboot();
    }

    /**
     * 恢复出厂设置，默认擦除用户数据
     *
     * @param clearUserData true则擦除用户数据；false则不擦除用户数据；参数无意义
     */
    @Override
    public void factoryReset(boolean clearUserData) throws RemoteException {
        systemHandler.get().FactoryReset();
    }

    /**
     * Download 事件: 应用下载安装或升级、开机动画升级、固件版本升级。
     *
     * @param cwmpDownloadRequest 请求下载需要传递的参数
     */
    @Override
    public void download(CWMPDownloadRequest cwmpDownloadRequest) throws RemoteException {
        systemHandler.get().download(cwmpDownloadRequest);
    }

    /**
     * 告知终端登入网管平台的状态以及是否成功
     *
     * @param type      登入状态， 0表示首次开机登入；1表示重启时登入。
     * @param isSuccess 登入是否成功
     */
    @Override
    public void onLogin(int type, boolean isSuccess) throws RemoteException {
        systemHandler.get().onLogin(type, isSuccess);
    }

    /**
     * 卸载app
     *
     * @param list 需要卸载的apk列表信息
     */
    @Override
    public void uninstall(List<AppID> list) throws RemoteException {
        systemHandler.get().appUninstall(list);
    }

    @Override
    public int addObject(String s) throws RemoteException {
        return 0;
    }

    @Override
    public void deleteObject(String s) throws RemoteException {

    }

    @Override
    public List<SetParameterValuesFault> setParameterAttributes(List<CWMPSetParameterAttributesStruct> list) throws RemoteException {
        return null;
    }
}
