package com.example.appgidi.network;

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
}