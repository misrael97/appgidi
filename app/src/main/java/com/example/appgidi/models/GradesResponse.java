package com.example.appgidi.models;

import java.util.List;

public class GradesResponse {
    private String status;
    private GradesData data;
    private List<String> msg;

    public String getStatus() {
        return status;
    }

    public GradesData getData() {
        return data;
    }

    public List<String> getMsg() {
        return msg;
    }
}
