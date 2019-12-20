package com.konka.kksdtr069.model;

public class SysLogRequest {

    String server = "";// Syslog上报服务器
    String level = "";// 输出日志的级别 0:不过滤 3:Error 6:Info 7:Debug
    String type = "";// 输出日志的类型 0:不过滤 16:操作日志 17:运行日志 19:用户日志 20:用户日志
    String outputType = "";/*日志输出方式 0:关闭日志功能 1:仅发送SFTP日志 2:仅发送实时日志
                                       3:SFTP和即时日志都发送*/
    String startTime = "";// 开始时间
    String continueTime = "";// 日志输出持续时间，单位：分钟
    String timer = "";// SFTP日志自动上传定时器值。单位：秒
    String ftpServer = "";// FTP格式的URL，sftp://ftpuser:111111@192.168.0.8

    public SysLogRequest(String server, String logLevel, String logType, String logOutputType
            , String startTime, String continueTime, String timer, String ftpServer) {
        this.server = server;
        this.level = logLevel;
        this.type = logType;
        this.outputType = logOutputType;
        this.startTime = startTime;
        this.continueTime = continueTime;
        this.timer = timer;
        this.ftpServer = ftpServer;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOutputType() {
        return outputType;
    }

    public void setOutputType(String outputType) {
        this.outputType = outputType;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getContinueTime() {
        return continueTime;
    }

    public void setContinueTime(String continueTime) {
        this.continueTime = continueTime;
    }

    public String getTimer() {
        return timer;
    }

    public void setTimer(String timer) {
        this.timer = timer;
    }

    public String getFtpServer() {
        return ftpServer;
    }

    public void setFtpServer(String ftpServer) {
        this.ftpServer = ftpServer;
    }
}
