package com.example.appgidi.models;

public class ResendCodeRequest {
    private String email;

    public ResendCodeRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
