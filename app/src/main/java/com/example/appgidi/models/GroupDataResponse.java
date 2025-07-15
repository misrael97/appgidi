package com.example.appgidi.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class GroupDataResponse {
    private String status;
    private String msg;
    private Data data;

    public Data getData() {
        return data;
    }

    public class Data {
        @SerializedName("teacherSubjectGroups")
        private List<TeacherSubjectGroup> teacherSubjectGroups;

        public List<TeacherSubjectGroup> getTeacherSubjectGroups() {
            return teacherSubjectGroups;
        }
    }

    public List<TeacherSubjectGroup> getTeacherSubjectGroups() {
        return data != null ? data.getTeacherSubjectGroups() : new ArrayList<>();
    }
}
