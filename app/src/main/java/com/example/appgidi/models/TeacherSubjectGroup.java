package com.example.appgidi.models;

public class TeacherSubjectGroup {
    private int id;
    private User users;
    private Subject subjects;
    private Group groups;
    private Classroom classrooms;
    private Schedule schedules;

    public User getUsers() { return users; }
    public Subject getSubjects() { return subjects; }
    public Group getGroups() { return groups; }
    public Classroom getClassrooms() { return classrooms; }
    public Schedule getSchedules() { return schedules; }
    private int teacher_id;
    private int subject_id;
    private int group_id;
    private int classroom_id;
    private int schedule_id;

    public int getTeacherId() { return teacher_id; }
    public int getSubjectId() { return subject_id; }
    public int getGroupId() { return group_id; }
    public int getClassroomId() { return classroom_id; }
    public int getScheduleId() { return schedule_id; }


}