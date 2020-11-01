package com.example.cs2102.model;

public class Listing {

    private final String careTaker;
    private final String petType;
    private final String price;
    private final String startDate;
    private final String endDate;

    public Listing(String ct, String petType, String fee, String startDate, String endDate) {
        this.petType = petType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.careTaker = ct;
        this.price = fee;
    }

    public String getPetType() {
        return petType;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getCareTaker() {
        return careTaker;
    }

    public String getPrice() {
        return price;
    }
}
