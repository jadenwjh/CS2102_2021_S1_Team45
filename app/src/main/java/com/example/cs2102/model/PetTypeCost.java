package com.example.cs2102.model;

public class PetTypeCost {

    private String careTakerUsername;
    private String type;
    private double upperBound;
    private double lowerBound;
    private double currentCost;

    public PetTypeCost(String careTakerUsername, String type, double upperBound, double lowerBound, double currentCost) {
        this.careTakerUsername = careTakerUsername;
        this.type = type;
        this.upperBound = upperBound;
        this.lowerBound = lowerBound;
        this.currentCost = currentCost;
    }

    public String getCareTakerUsername() {
        return careTakerUsername;
    }

    public void setCareTakerUsername(String careTakerUsername) {
        this.careTakerUsername = careTakerUsername;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getUpperBound() {
        return upperBound;
    }

    public void setUpperBound(double upperBound) {
        this.upperBound = upperBound;
    }

    public double getLowerBound() {
        return lowerBound;
    }

    public void setLowerBound(double lowerBound) {
        this.lowerBound = lowerBound;
    }

    public double getCurrentCost() {
        return currentCost;
    }

    public void setCurrentCost(double currentCost) {
        this.currentCost = currentCost;
    }
}
