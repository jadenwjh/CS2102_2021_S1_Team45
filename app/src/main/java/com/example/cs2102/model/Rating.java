package com.example.cs2102.model;

public class Rating {

    private final String careTaker;
    private final String rating;

    public Rating(String careTaker, String rating) {
        this.careTaker = careTaker;
        this.rating = rating.substring(0,3);
    }

    public String getCareTaker() {
        return careTaker;
    }

    public String getRating() {
        return rating;
    }
}
