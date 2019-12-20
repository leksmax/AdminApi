package com.konka.kksdtr069.model;

public class RequestURL {

    private String account;
    private String cityCode;
    private String bandWidth;
    private String terminalType;
    private String terminalNo;
    private String userIP;

    public RequestURL(String account, String cityCode, String bandWidth,
                      String terminalType, String terminalNo, String userIP) {
        this.account = account;
        this.cityCode = cityCode;
        this.bandWidth = bandWidth;
        this.terminalType = terminalType;
        this.terminalNo = terminalNo;
        this.userIP = userIP;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getBandWidth() {
        return bandWidth;
    }

    public void setBandWidth(String bandWidth) {
        this.bandWidth = bandWidth;
    }

    public String getTerminalType() {
        return terminalType;
    }

    public void setTerminalType(String terminalType) {
        this.terminalType = terminalType;
    }

    public String getTerminalNo() {
        return terminalNo;
    }

    public void setTerminalNo(String terminalNo) {
        this.terminalNo = terminalNo;
    }

    public String getUserIP() {
        return userIP;
    }

    public void setUserIP(String userIP) {
        this.userIP = userIP;
    }
}
