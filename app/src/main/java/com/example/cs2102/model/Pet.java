package com.example.cs2102.model;

public class Pet {
    private final String name;
    private final String type;
    private final String profile;
    private final String needs;

    public Pet(String name, String type, String profile, String needs) {
        this.name = name;
        this.type = type;
        this.profile = profile;
        this.needs = needs;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getProfile() {
        return profile;
    }

    public String getNeeds() {
        return needs;
    }
}
