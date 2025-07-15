package com.example.appgidi.models;

import com.google.gson.annotations.SerializedName;

public class Attendance {
    private int id;
    private int user_id;
    private int subject_id;
    private String date;
    private String check_in_time;
    private String status;
    private Integer sensor_id;
    private String notes;

    public int getId() { return id; }
    public int getUser_id() { return user_id; }
    public int getSubject_id() { return subject_id; }
    public String getDate() { return date; }
    public String getCheck_in_time() { return check_in_time; }
    public String getStatus() { return status; }
    public Integer getSensor_id() { return sensor_id; }
    public String getNotes() { return notes; }
}

