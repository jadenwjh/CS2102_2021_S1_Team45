package com.example.cs2102.model;

public class Review {

    private final String username;
    private final String rating;
    private final String review;
    private final String date;
    private final String petName;

    public Review(String username, String rating, String review, String edate, String p) {
        this.username = username;
        this.rating = rating;
        this.review = review;
        this.date = edate;
        this.petName = p;
    }

    public String getUsername() {
        return username;
    }

    public String getRating() {
        return rating;
    }

    public String getReview() {
        return review;
    }

    public String getDate() {
        return date;
    }

    public String getPetName() {
        return petName;
    }
}
