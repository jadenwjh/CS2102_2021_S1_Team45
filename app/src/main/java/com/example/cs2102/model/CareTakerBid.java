package com.example.cs2102.model;

public class CareTakerBid {

    private final String petOwner;
    private final String petName;
    private final String careTaker;
    private final String startDate;
    private final String endDate;
    private final String transfer;
    private final String payment;
    private final String price;
    private final String isPaid;
    private final String status;
    private final String review;
    private final String rating;

    public CareTakerBid(String petOwner, String petName, String careTaker, String startDate, String endDate, String transfer, String payment, String price, String isPaid, String status, String review, String rating) {
        this.petOwner = petOwner;
        this.petName = petName;
        this.careTaker = careTaker;
        this.startDate = startDate;
        this.endDate = endDate;
        this.transfer = transfer;
        this.payment = payment;
        this.price = price;
        this.isPaid = isPaid;
        this.status = status;
        this.review = review;
        this.rating = rating;
    }

    public String getPetOwner() {
        return petOwner;
    }

    public String getPetName() {
        return petName;
    }

    public String getCareTaker() {
        return careTaker;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getTransfer() {
        return transfer;
    }

    public String getPayment() {
        return payment;
    }

    public String getPrice() {
        return price;
    }

    public String getIsPaid() {
        return isPaid;
    }

    public String getStatus() {
        return status;
    }

    public String getReview() {
        return review;
    }

    public String getRating() {
        return rating;
    }
}
