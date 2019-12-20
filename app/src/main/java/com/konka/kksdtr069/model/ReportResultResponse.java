package com.konka.kksdtr069.model;

public class ReportResultResponse {

    private int result;
    private String desc;

    public ReportResultResponse(int result, String desc) {
        this.result = result;
        this.desc = desc;
    }

    public int getResult() {
        return result;
    }

    public String getDesc() {
        return desc;
    }

}
