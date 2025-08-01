package com.example.appgidi.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class GroupDataResponse {
    private String status;

    // Cambiado a List por si viene como array en el JSON del estudiante
    private List<String> msg;

    private Data data;

    public Data getData() {
        return data;
    }

    public List<TeacherSubjectGroup> getTeacherSubjectGroups() {
        return data != null ? data.getTeacherSubjectGroups() : new ArrayList<>();
    }

    public List<TeacherSubjectGroup> getSchedule() {
        return data != null ? data.getSchedule() : new ArrayList<>();
    }

    public Group getStudentGroup() {
        return data != null ? data.getStudentGroup() : null;
    }

    public class Data {
        // Para maestros
        @SerializedName("teacherSubjectGroups")
        private List<TeacherSubjectGroup> teacherSubjectGroups;

        // Para estudiantes
        @SerializedName("schedule")
        private List<TeacherSubjectGroup> schedule;

        @SerializedName("student_group")
        private Group studentGroup;

        public List<TeacherSubjectGroup> getTeacherSubjectGroups() {
            return teacherSubjectGroups != null ? teacherSubjectGroups : new ArrayList<>();
        }

        public List<TeacherSubjectGroup> getSchedule() {
            return schedule != null ? schedule : new ArrayList<>();
        }

        public Group getStudentGroup() {
            return studentGroup;
        }
    }
}
