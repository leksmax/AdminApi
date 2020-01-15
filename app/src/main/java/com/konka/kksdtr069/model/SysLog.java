package com.konka.kksdtr069.model;

import android.text.TextUtils;

import com.konka.kksdtr069.util.PropertyUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SysLog {

    private int outPutType;
    private int level;
    private int type;
    private String serverIP;
    private String serverPort;
    private Date startTime;
    private int continueTime;
    private String remotePath;
    private String remoteFileName;
    private String localPath;
    private String localFileName;
    private String sftpIP;
    private String sftpPort;
    private String sftpUsername;
    private String sftpPassword;


    public SysLog(int logOutputType, int logLevel, int logType, String sysLogServerAd,
                  Date time, int sysLogContinueTime, String sftpServerAd) {
        this.outPutType = logOutputType;
        this.level = logLevel;
        this.type = logType;

        if (!TextUtils.isEmpty(sysLogServerAd)) {
            String[] strings = sysLogServerAd.split(":");
            this.serverIP = strings[0];
            if (!TextUtils.isEmpty(strings[1])) {
                this.serverPort = strings[1];
            } else {
                this.serverPort = "37027";
            }
        }

        this.startTime = time;
        this.continueTime = sysLogContinueTime;

        String sftpIP = "";
        String sftpUsername = "";
        String sftpPsw = "";
        if (!TextUtils.isEmpty(sftpServerAd)) {
            sftpIP = sftpServerAd.split("@")[1];
            sftpUsername = sftpServerAd
                    .split("@")[0]
                    .split("//")[1]
                    .split(":")[0];
            sftpPsw = sftpServerAd.split("@")[0]
                    .split("//")[1]
                    .split(":")[1];
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
        String date = sdf.format(new Date());
        String sfileName = PropertyUtil.getProperty("ro.mac").replace(":", "")
                + "_" + date + "log";

        this.remotePath = "/";
        this.remoteFileName = sfileName;
        this.localPath = "/data/data/com.konka.kksdtr069/cache/log/";
        this.localFileName = sfileName;
        this.sftpIP = sftpIP;
        this.sftpPort = "22";
        this.sftpUsername = sftpUsername;
        this.sftpPassword = sftpPsw;
    }

    public int getOutPutType() {
        return outPutType;
    }

    public void setOutPutType(int outPutType) {
        this.outPutType = outPutType;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getServerIP() {
        return serverIP;
    }

    public void setServerIP(String serverIP) {
        this.serverIP = serverIP;
    }

    public String getServerPort() {
        return serverPort;
    }

    public void setServerPort(String serverPort) {
        this.serverPort = serverPort;
    }

    public int getContinueTime() {
        return continueTime;
    }

    public void setContinueTime(int continueTime) {
        this.continueTime = continueTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }


    public String getRemotePath() {
        return remotePath;
    }

    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
    }

    public String getRemoteFileName() {
        return remoteFileName;
    }

    public void setRemoteFileName(String remoteFileName) {
        this.remoteFileName = remoteFileName;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getLocalFileName() {
        return localFileName;
    }

    public void setLocalFileName(String localFileName) {
        this.localFileName = localFileName;
    }

    public String getSftpIP() {
        return sftpIP;
    }

    public void setSftpIP(String sftpIP) {
        this.sftpIP = sftpIP;
    }

    public String getSftpPort() {
        return sftpPort;
    }

    public void setSftpPort(String sftpPort) {
        this.sftpPort = sftpPort;
    }

    public String getSftpUsername() {
        return sftpUsername;
    }

    public void setSftpUsername(String sftpUsername) {
        this.sftpUsername = sftpUsername;
    }

    public String getSftpPassword() {
        return sftpPassword;
    }

    public void setSftpPassword(String sftpPassword) {
        this.sftpPassword = sftpPassword;
    }
}
