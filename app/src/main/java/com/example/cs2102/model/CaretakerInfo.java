package com.example.cs2102.model;

public class CaretakerInfo {

    private final String caretaker;
    private final String contract;
    private final String salary;
    private final String days;
    private final String rating;
    private final String frequency;

    public CaretakerInfo(String caretaker, String contract, String salary, String days, String rating, String frequency) {
        this.caretaker = caretaker;
        this.contract = contract;
        this.salary = salary;
        this.days = days;
        this.rating = rating;
        this.frequency = frequency;
    }

    public String getCaretaker() {
        return caretaker;
    }

    public String getContract() {
        return contract;
    }

    public String getSalary() {
        return salary;
    }

    public String getDays() {
        return days;
    }

    public String getRating() {
        return rating;
    }

    public String getFrequency() {
        return frequency;
    }
}
