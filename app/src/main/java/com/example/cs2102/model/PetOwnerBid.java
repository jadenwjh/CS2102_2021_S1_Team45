package com.example.cs2102.model;

public class PetOwnerBid {
    private final String username;
    private final String petName;
    private final String petType;
    private final String availability;
    private final String endDate;
    private final String price;
    private final String transfer;
    private final String payment;

    public PetOwnerBid(String username, String petName, String petType, String availability, String endDate, String price, String transfer, String payment) {
        this.username = username;
        this.petName = petName;
        this.petType = petType;
        this.availability = availability;
        this.price = price;
        this.transfer = transfer;
        this.payment = payment;
        this.endDate = endDate;
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

    public String getTransfer() {
        return transfer;
    }

    public String getPayment() {
        return payment;
    }

    public String getEndDate() {
        return endDate;
    }
}
