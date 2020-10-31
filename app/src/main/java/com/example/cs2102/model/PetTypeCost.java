package com.example.cs2102.model;

public class PetTypeCost {

    private String careTakerUsername;
    private String type;
    private String fee;

    //yet to impl
    private String upper;
    private String base;

    public PetTypeCost(String careTakerUsername, String type, String fee) {
        this.careTakerUsername = careTakerUsername;
        this.type = type;
        this.fee = fee;
    }

    public String getCareTakerUsername() {
        return careTakerUsername;
    }

    public String getType() {
        return type;
    }

    public String getFee() {
        return fee;
    }
}
