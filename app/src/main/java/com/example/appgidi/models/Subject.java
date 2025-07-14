package com.example.appgidi.models;

public class Subject {
    private int id;
    private String name;
    private String code;

    public String getName() { return name; }
    public int getId() { return id; }

    @Override
    public String toString() {
        return name; // Para mostrar en el Spinner
    }
    public String getCode() { return code; }
}
