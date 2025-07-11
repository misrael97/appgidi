package com.example.appgidi.models;

public class User {
    private String email;
    private String password;
    private String client;

    public User(String email, String password, String client) {
        this.email = email;
        this.password = password;
        this.client = client;
    }


    private String first_name;
    private String last_name;

    public String getFullName() {
        return first_name + " " + last_name;
    }

    // Getters (si usas Gson, setters no son necesarios)
}