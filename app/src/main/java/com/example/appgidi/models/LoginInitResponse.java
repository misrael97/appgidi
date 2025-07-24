package com.example.appgidi.models;

public class LoginInitResponse {
    private String status;
    private String msg;
    private LoginInitData data;

    public String getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }

    public LoginInitData getData() {
        return data;
    }

    public static class LoginInitData {
        private int id;
        private String email;

        public int getId() { return id; }
        public String getEmail() { return email; }
    }
}
