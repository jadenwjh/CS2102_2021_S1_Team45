package com.example.cs2102.model;

public class Salary {

    private final String caretaker;
    private final String salary;

    public Salary(String caretaker, String salary) {
        this.caretaker = caretaker;
        this.salary = salary;
    }

    public String getCaretaker() {
        return caretaker;
    }

    public String getSalary() {
        return salary;
    }
}
