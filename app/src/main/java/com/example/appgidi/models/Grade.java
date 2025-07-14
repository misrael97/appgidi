package com.example.appgidi.models;

import java.util.Map;

public class Grade {
    private int id;
    private Map<String, Object> teacher;
    private Subject subject;
    private int unit_number;
    private double grade;
    private String notes;

    public Map<String, Object> getTeacher() {
        return teacher;
    }
    public int getUnitNumber() { return unit_number; }
    public double getGrade() { return grade; }
    public String getNotes() { return notes; }
    public Subject getSubject() { return subject; }


}
