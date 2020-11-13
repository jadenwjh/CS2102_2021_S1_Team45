package com.example.cs2102.model;

public class PetOwnerBid {
    private final String username;
    private final String petName;
    private final String petType;
    private final String availability;
    private final String endDate;
    private final String price;
    private final String payment;
    private final String status;

    public PetOwnerBid(String username, String petName, String petType, String availability, String endDate, String price, String payment, String status) {
        this.username = username;
        this.petName = petName;
        this.petType = petType;
        this.availability = availability;
        this.price = price;
        this.payment = payment;
        this.endDate = endDate;
        this.status = status;
    }

    public String getStatus() {
        return status;
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

    public String getPrice() {
        return price;
    }

    public String getPayment() {
        return payment;
    }

    public String getEndDate() {
        return endDate;
    }
}
