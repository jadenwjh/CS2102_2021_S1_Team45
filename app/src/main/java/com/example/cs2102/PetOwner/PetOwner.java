package com.example.cs2102.PetOwner;

import com.example.cs2102.UrlStrings;
import com.google.gson.annotations.SerializedName;

public class PetOwner {

    @SerializedName(UrlStrings.ID)
    private String uid;
    @SerializedName(UrlStrings.PET_OWNER_PET)
    private String pet;

    public PetOwner(String uid, String pet) {
        this.uid = uid;
        this.pet = pet;
    }

    public String getUid() {
        return uid;
    }

    public String getPet() {
        return pet;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setPet(String pet) {
        this.pet = pet;
    }
}
