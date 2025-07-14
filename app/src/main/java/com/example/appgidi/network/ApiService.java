package com.example.appgidi.network;

import com.example.appgidi.models.GradesResponse;
import com.example.appgidi.models.GroupDataResponse;
import com.example.appgidi.models.LoginResponse;
import com.example.appgidi.models.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    @POST("/api/login")
    Call<LoginResponse> loginUser(@Body User user);

    @GET("api/teacher-subject-groups/group/{groupId}")
    Call<GroupDataResponse> getTeacherGroups(
            @Path("groupId") int groupId,
            @Header("Authorization") String authHeader
    );

    @GET("api/grades/student/{studentId}")
    Call<GradesResponse> getAllStudentGrades(
            @Path("studentId") int studentId,
            @Header("Authorization") String token
    );

    @GET("api/grades/student/{studentId}/{subjectId}")
    Call<GradesResponse> getStudentGradesBySubject(
            @Path("studentId") int studentId,
            @Path("subjectId") int subjectId,
            @Header("Authorization") String token
    );

}