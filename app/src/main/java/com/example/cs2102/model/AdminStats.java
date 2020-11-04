package com.example.cs2102.model;

public class AdminStats {

    private final String totalpets;
    private final String petdays;

    public AdminStats(String totalpets, String petdays) {
        this.totalpets = totalpets;
        this.petdays = petdays;
    }

    public String getTotalpets() {
        return totalpets;
    }

    public String getPetdays() {
        return petdays;
    }
}
