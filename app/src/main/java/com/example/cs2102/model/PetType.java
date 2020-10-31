package com.example.cs2102.model;

public class PetType {
    private final String type;
    private final String basePrice;

    public PetType(String type, String basePrice) {
        this.type = type;
        this.basePrice = basePrice;
    }

    public String getType() {
        return type;
    }

    public String getBasePrice() {
        return basePrice;
    }
}
