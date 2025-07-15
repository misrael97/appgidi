package com.example.appgidi.models;

import java.util.List;

public class AttendanceResponse {
    private String status;
    private List<Attendance> data;
    private List<String> msg;

    public String getStatus() { return status; }
    public List<Attendance> getData() { return data; }
    public List<String> getMsg() { return msg; }
}
