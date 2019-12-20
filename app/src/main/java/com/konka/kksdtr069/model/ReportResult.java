package com.konka.kksdtr069.model;

public class ReportResult {

    private String speedTestId;
    private String account;
    private String speedMax;
    private String speedAvg;

    public ReportResult(String speedTestId, String account, String speedMax,
                            String speedAvg) {
        this.speedTestId = speedTestId;
        this.account = account;
        this.speedMax = speedMax;
        this.speedAvg = speedAvg;
    }

    public String getSpeedTestId() {
        return speedTestId;
    }

    public void setSpeedTestId(String speedTestId) {
        this.speedTestId = speedTestId;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getSpeedMax() {
        return speedMax;
    }

    public void setSpeedMax(String speedMax) {
        this.speedMax = speedMax;
    }

    public String getSpeedAvg() {
        return speedAvg;
    }

    public void setSpeedAvg(String speedAvg) {
        this.speedAvg = speedAvg;
    }
}
