package com.konka.kksdtr069.service;

import android.os.RemoteException;

import com.konka.kksdtr069.handler.SystemHandler;
import com.konka.kksdtr069.handler.impl.FunctionHandlerImpl;
import com.konka.kksdtr069.handler.impl.ParameterHandlerImpl;
import com.konka.kksdtr069.handler.impl.SystemHandlerImpl;
import com.konka.kksdtr069.util.LogUtil;

import net.sunniwell.cwmp.protocol.sdk.aidl.AppID;
import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPDownloadRequest;
import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPParameter;
import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPSetParameterAttributesStruct;
import net.sunniwell.cwmp.protocol.sdk.aidl.ICWMPNativeService;
import net.sunniwell.cwmp.protocol.sdk.aidl.ICWMPProtocolService;
import net.sunniwell.cwmp.protocol.sdk.aidl.SetParameterValuesFault;

import java.util.ArrayList;
import java.util.List;

public class CWMPService extends ICWMPNativeService.Stub {

    private static final String TAG = CWMPService.class.getSimpleName();

    private FunctionHandlerImpl functionHandler;

    private ParameterHandlerImpl parameterHandler;

    private SystemHandler systemHandler;

    private ICWMPProtocolService mProtocolService;

    public CWMPService(ICWMPProtocolService protocolService) {
        functionHandler = FunctionHandlerImpl.getInstance();
        parameterHandler = ParameterHandlerImpl.getInstance();
        systemHandler = SystemHandlerImpl.getInstance();
        mProtocolService = protocolService;
    }

    /**
     * @return 获取Inform报文需要的参数列表
     */
    @Override
    public List<CWMPParameter> getInformParameters() throws RemoteException {
        List<CWMPParameter> list = parameterHandler.getInformParameters(mProtocolService);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i == 0) {
                stringBuilder.append("getInformParameters() result : " + "\n");
            }
            stringBuilder.append("name : " + list.get(i).getName() + "\n"
                    + "value :" + list.get(i).getValue() + "\n");

        }
        LogUtil.d(TAG, stringBuilder.toString());
        return list;
    }

    /**
     * @return 获取心跳需要上报的参数列表，同Inform报文的参数列表
     */
    @Override
    public List<CWMPParameter> getPeriodicParameters() throws RemoteException {
        List<CWMPParameter> list = parameterHandler.getInformParameters(mProtocolService);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i == 0) {
                stringBuilder.append("getPeriodicParameters() result : " + "\n");
            } else {
                stringBuilder.append("name : " + list.get(i).getName() + "\n"
                        + "value :" + list.get(i).getValue() + "\n");
            }
        }
        LogUtil.d(TAG, stringBuilder.toString());
        return list;
    }

    /**
     * @param path 查询父对象的参数名，如Device
     * @return 父对象下所属所有子对象和参数，封装在List<CWMPParameter>中返回
     */
    @Override
    public List<CWMPParameter> getParameters(String path) throws RemoteException {
        List<CWMPParameter> list = parameterHandler.getParameterBySuperName(path);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i == 0) {
                stringBuilder.append("getParameters() result : " + "\n");
            } else {
                stringBuilder.append("name : " + list.get(i).getName() + "\n"
                        + "value :" + list.get(i).getValue() + "\n");
            }
        }
        LogUtil.d(TAG, stringBuilder.toString());
        return list;
    }

    /**
     * @param name 输入需要查询的参数名称
     * @return 查询参数名对应的参数值
     * @Exception RemoteException
     */
    @Override
    public String getParameterValue(String name) throws RemoteException {
        String value = parameterHandler.getParameterValue(name);
        LogUtil.d(TAG, "getParameterValue() result : " + "\n"
                + "name : " + name + "\n"
                + "value : " + value + "\n");
        return value;
    }

    /**
     * @param name  需要设置的参数名
     * @param value 设置参数名对应的参数值
     * @return 设置参数所在数据库对应表的行数
     */
    @Override
    public int setParameterValue(String name, String value) throws RemoteException {
        int result = parameterHandler.setParameterValue(name, value);
        LogUtil.d(TAG, "setParameterValue() result : " + "\n"
                + "name : " + name + "\n"
                + "value : " + value + "\n"
                + "set parameter result : " + result + "\n");
        return result;
    }

    /**
     * @param name 参数名称
     * @return 获取指定参数名的CWMPParameter
     */
    @Override
    public CWMPParameter getParameter(String name) throws RemoteException {
        CWMPParameter parameter = parameterHandler.getParameter(name);
        LogUtil.d(TAG, "getParameter() result : " + "\n"
                + "name : " + parameter.getName() + "\n"
                + "value : " + parameter.getValue() + "\n");
        return parameter;
    }

    /**
     * 设置参数方式完成特殊功能，ping诊断、trace route诊断、远程抓包、
     * 抓取上传日志、开关wifi
     *
     * @param list 请求实现功能，需要传入的设置参数列表
     * @return 功能完成后，返回参数
     */
    @Override
    public List<SetParameterValuesFault> setParameters(List<CWMPParameter> list) throws RemoteException {
        ArrayList<SetParameterValuesFault> faultList = new ArrayList<SetParameterValuesFault>();
        functionHandler.pingDiagnosis(list, mProtocolService);
        functionHandler.traceRouteDiagnosis(list, mProtocolService);
        functionHandler.remoteNetPacketCapture(list);
        functionHandler.captureAndUploadLog(list);
        faultList.addAll(functionHandler.wifiEnable(list));
        LogUtil.d(TAG, "setParameters() Device.ManagementServer.PeriodicInformInterval = "
                + parameterHandler.getParameterValue("Device.ManagementServer.PeriodicInformInterval"));
        return faultList;
    }


    /**
     * 远程重启
     */
    @Override
    public void reboot() {
        LogUtil.d(TAG, "reboot()");
        systemHandler.reboot();
    }

    /**
     * 恢复出厂设置，默认擦除用户数据
     *
     * @param clearUserData true则擦除用户数据；false则不擦除用户数据；参数无意义
     */
    @Override
    public void factoryReset(boolean clearUserData) {
        LogUtil.d(TAG, "factoryReset()");
        systemHandler.FactoryReset();
    }

    /**
     * Download 事件: 应用下载安装或升级、固件版本升级。
     *
     * @param cwmpDownloadRequest 请求下载需要传递的参数
     */
    @Override
    public void download(CWMPDownloadRequest cwmpDownloadRequest) {
        LogUtil.d(TAG, "download()");
        systemHandler.download(cwmpDownloadRequest, mProtocolService);
    }

    /**
     * 告知终端登入网管平台的状态以及是否成功
     *
     * @param type      登入状态， 0表示首次开机登入；1表示重启时登入。
     * @param isSuccess 登入是否成功
     */
    @Override
    public void onLogin(int type, boolean isSuccess) {
        LogUtil.d(TAG, "onLogin()");
        systemHandler.onLogin(type, isSuccess);
    }

    /**
     * 卸载app
     *
     * @param list 需要卸载的apk列表信息
     */
    @Override
    public void uninstall(List<AppID> list) {
        LogUtil.d(TAG, "uninstall()");
        systemHandler.appUninstall(list, mProtocolService);
    }

    @Override
    public int addObject(String s) throws RemoteException {
        return 0;
    }

    @Override
    public void deleteObject(String s) throws RemoteException {

    }

    @Override
    public List<SetParameterValuesFault> setParameterAttributes(List<CWMPSetParameterAttributesStruct> list)
            throws RemoteException {
        return null;
    }
}
