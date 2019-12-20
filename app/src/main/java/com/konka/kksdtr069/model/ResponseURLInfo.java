package com.konka.kksdtr069.model;

public class ResponseURLInfo {

    private String speedTestId;
    private int statusCode;
    private String speedTestUrl;

    public ResponseURLInfo(String speedTestId, int statusCode, String speedTestUrl) {
        this.speedTestId = speedTestId;
        this.statusCode = statusCode;
        this.speedTestUrl = speedTestUrl;
    }

    public String getSpeedTestId() {
        return speedTestId;
    }

    public void setSpeedTestId(String speedTestId) {
        this.speedTestId = speedTestId;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getSpeedTestUrl() {
        return speedTestUrl;
    }

    public void setSpeedTestUrl(String speedTestUrl) {
        this.speedTestUrl = speedTestUrl;
    }
}
