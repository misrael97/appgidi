package com.example.appgidi.models;

public class LoginResponse {
    private String status;
    private String msg;
    private UserData data;

    public String getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }

    public UserData getData() {
        return data;
    }

    public static class UserData {
        private int id;
        private String email;
        private String token;

        public int getId() {
            return id;
        }

        public String getEmail() {
            return email;
        }

        public String getToken() {
            return token;
        }
    }
}