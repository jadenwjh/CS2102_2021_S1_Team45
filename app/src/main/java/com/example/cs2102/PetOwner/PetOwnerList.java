package com.example.cs2102.PetOwner;

import com.example.cs2102.UrlStrings;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class PetOwnerList {

    @SerializedName(UrlStrings.PET_OWNER_HEADER)
    private ArrayList<PetOwner> petOwnerArrayList;

    public void setPetOwnerArrayList(ArrayList<PetOwner> petOwnerArrayList) {
        this.petOwnerArrayList = petOwnerArrayList;
    }

    public ArrayList<PetOwner> getPetOwnerArrayList() {
        return petOwnerArrayList;
    }
}
