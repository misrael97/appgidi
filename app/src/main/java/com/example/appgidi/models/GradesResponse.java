package com.example.appgidi.models;

import java.util.List;

public class GradesResponse {
    private String status;
    private GradesData data;
    private List<String> msg;

    public GradesData getData() {
        return data;
    }

    public static class GradeData {
        private User student;
        private List<Grade> grades;

        public List<Grade> getGrades() {
            return grades;
        }

}}
