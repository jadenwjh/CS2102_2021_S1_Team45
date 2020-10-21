package com.example.cs2102.model;

public class Pet {
    private String petID;
    private String type;

    public Pet(String petID, String type) {
        this.petID = petID;
        this.type = type;
    }

    public String getPetID() {
        return petID;
    }

    public void setPetID(String petID) {
        this.petID = petID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
