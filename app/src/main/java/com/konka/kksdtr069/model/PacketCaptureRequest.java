package com.konka.kksdtr069.model;

public class PacketCaptureRequest {

    private int state;// 抓包请求2
    private int duration;// 抓包持续时长，单位秒
    private String ip;// 抓包过滤条件，IP
    private int port;// 抓包过滤条件，Port
    private String uploadURL;// 上传抓包服务器地址,格式：sftp://192.168.0.8
    private String username;// 服务器账号
    private String password;// 服务器密码
    private String fileName;
    private String localPath;

    public PacketCaptureRequest() {

    }

    public PacketCaptureRequest(String state, String duration, String ip, String port,
                                String uploadURL, String username, String password) {
        if (!"".equals(state)) {
            this.state = Integer.valueOf(state);
        }
        if (!"".equals(duration)) {
            this.duration = Integer.valueOf(duration);
        }
        if (!"".equals(port)) {
            this.port = Integer.valueOf(port);
        }
        this.ip = ip;
        this.uploadURL = uploadURL;
        this.username = username;
        this.password = password;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public int getState() {
        return state;
    }

    public void setState(String state) {
        if (!"".equals(state)) {
            this.state = Integer.valueOf(state);
        }
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        if (!"".equals(duration)) {
            this.duration = Integer.valueOf(duration);
        }
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(String port) {
        if (!"".equals(port)) {
            this.port = Integer.valueOf(port);
        }
    }

    public String getUploadURL() {
        return uploadURL;
    }

    public void setUploadURL(String uploadURL) {
        this.uploadURL = uploadURL;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
