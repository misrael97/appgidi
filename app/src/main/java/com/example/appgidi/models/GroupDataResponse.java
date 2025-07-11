package com.example.appgidi.models;

import java.util.List;

public class GroupDataResponse {
    private String status;
    private Data data;

    public String getStatus() {
        return status;
    }

    public List<TeacherSubjectGroup> getTeacherSubjectGroups() {
        return data != null ? data.getTeacherSubjectGroups() : null;
    }

    // ✅ Clase interna ahora es pública y con getters
    public static class Data {
        private List<TeacherSubjectGroup> teacherSubjectGroups;

        public List<TeacherSubjectGroup> getTeacherSubjectGroups() {
            return teacherSubjectGroups;
        }
    }
}
