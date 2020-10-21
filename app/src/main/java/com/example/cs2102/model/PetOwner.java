package com.example.cs2102.model;

public class PetOwner extends User {

    private String petName;
    private String petType;

    public PetOwner(String userID, String password, String address, String number, String email, String type, String petName, String petType) {
        super(userID, password, address, number, email, type);
        this.petName = petName;
        this.petType = petType;
    }

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public String getPetType() {
        return petType;
    }

    public void setPetType(String petType) {
        this.petType = petType;
    }
}
