package com.konka.kksdtr069.model;

import java.io.Serializable;

public class ResponseURL implements Serializable {

    private int result;
    private String desc;
    private ResponseURLInfo data;

    public ResponseURL(int result, String desc, ResponseURLInfo data) {
        this.result = result;
        this.desc = desc;
        this.data = data;
    }

    public int getResult() {
        return result;
    }

    public String getDesc() {
        return desc;
    }

    public ResponseURLInfo getResponseURLInfo() {
        return data;
    }

}
