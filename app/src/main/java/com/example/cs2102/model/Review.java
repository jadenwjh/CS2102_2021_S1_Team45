package com.example.cs2102.model;

public class Review {

    private final String username;
    private final String rating;
    private final String review;

    public Review(String username, String rating, String review) {
        this.username = username;
        this.rating = rating;
        this.review = review;
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
}
