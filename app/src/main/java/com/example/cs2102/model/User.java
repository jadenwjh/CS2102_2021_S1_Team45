package com.example.cs2102.model;

public class User {
    private String userID;
    private String password;
    private String address;
    private String number;
    private String email;
    private String type;

    public User(String userID, String password, String address, String number, String email, String type) {
        this.userID = userID;
        this.password = password;
        this.address = address;
        this.number = number;
        this.email = email;
        this.type = type;
    }

    public String getUserID() {
        return userID;
    }

    public String getPassword() {
        return password;
    }

    public String getAddress() {
        return address;
    }

    public String getNumber() {
        return number;
    }

    public String getEmail() {
        return email;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
