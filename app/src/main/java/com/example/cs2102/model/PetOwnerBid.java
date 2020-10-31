package com.example.cs2102.model;

public class PetOwnerBid {
    private final String username;
    private final String petName;
    private final String petType;
    private final String availability;

    public PetOwnerBid(String username, String petName, String petType, String availability) {
        this.username = username;
        this.petName = petName;
        this.petType = petType;
        this.availability = availability;
    }

    public String getPetOwner() {
        return username;
    }

    public String getPetName() {
        return petName;
    }

    public String getPetType() {
        return petType;
    }

    public String getAvailability() {
        return availability;
    }
}
