package org.example.model;

public class country {
    private String name;
    private String code;

    public String getName() {return name;}
    public String getCode() {return code;}

    @Override
    public String toString() {
        return name;
    }
}
