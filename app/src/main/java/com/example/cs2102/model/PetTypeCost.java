package com.example.cs2102.model;

public class PetTypeCost {

    private String careTakerUsername;
    private String type;
    private String fee;

    //yet to impl
    private String max;
    private String min;

    public PetTypeCost(String careTakerUsername, String type, String fee, String base, String upper) {
        this.careTakerUsername = careTakerUsername;
        this.type = type;
        this.fee = fee;
        this.max = upper;
        this.min = base;
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

    public String getMax() {
        return max;
    }

    public String getMin() {
        return min;
    }
}
