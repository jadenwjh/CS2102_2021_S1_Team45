package com.example.cs2102.model;

public class CareTaker extends User{
    private String contract;

    public CareTaker(String userID, String password, String address, String number, String email, String type, String contract) {
        super(userID, password, address, number, email, type);
        this.contract = contract;
    }

    public String getContract() {
        return contract;
    }

    public void setContract(String contract) {
        this.contract = contract;
    }
}
