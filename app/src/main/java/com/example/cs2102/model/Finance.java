package com.example.cs2102.model;

public class Finance {
    private final String year;
    private final String month;
    private final String profit;
    private final String revenue;
    private final String salary;
    private final String pets;

    public Finance(String year, String month, String profit, String revenue, String salary, String pets) {
        this.year = year;
        this.month = month;
        this.profit = profit;
        this.revenue = revenue;
        this.salary = salary;
        this.pets = pets;
    }

    public String getYear() {
        return year;
    }

    public String getMonth() {
        return month;
    }

    public String getProfit() {
        return profit;
    }

    public String getRevenue() {
        return revenue;
    }

    public String getSalary() {
        return salary;
    }

    public String getPets() {
        return pets;
    }
}
