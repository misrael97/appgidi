package com.example.appgidi.network;

import com.example.appgidi.models.Attendance;
import com.example.appgidi.models.AttendanceResponse;
import com.example.appgidi.models.GradesResponse;
import com.example.appgidi.models.GroupDataResponse;
import com.example.appgidi.models.LoginInitResponse;
import com.example.appgidi.models.LoginResponse;
import com.example.appgidi.models.ResendCodeRequest;
import com.example.appgidi.models.User;
import com.example.appgidi.models.VerifyCodeRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("/api/academic/login")
    Call<LoginInitResponse> loginUser(@Body User user);

    // Horarios - Obtener materias del estudiante
    @GET("api/academic/teacher-subject-groups/student/{student_id}")
    Call<GroupDataResponse> getTeacherGroups(
            @Path("student_id") int studentId,
            @Header("Authorization") String authHeader
    );

    // Calificaciones - Obtener todas las calificaciones del estudiante
    @GET("api/academic/grades/student/{student_id}")
    Call<GradesResponse> getAllStudentGrades(
            @Path("student_id") int studentId,
            @Header("Authorization") String token
    );

    // Calificaciones - Obtener calificaciones por materia
    @GET("api/academic/grades/student/{student_id}/{subject_id}")
    Call<GradesResponse> getStudentGradesBySubject(
            @Path("student_id") int studentId,
            @Path("subject_id") int subjectId,
            @Header("Authorization") String token
    );

    // Asistencia - Obtener asistencia filtrada
    @GET("api/academic/attendance")
    Call<AttendanceResponse> getAttendanceFiltered(
            @Query("user_id") int userId,
            @Query("month") int month,
            @Query("year") int year,
            @Query("subject_id") int subjectId,
            @Header("Authorization") String authHeader
    );

    @POST("/api/academic/users/verify")
    Call<LoginResponse> verifyCode(@Body VerifyCodeRequest request);

    @POST("/api/academic/resend")
    Call<Void> resendCode(@Body ResendCodeRequest request);

}
