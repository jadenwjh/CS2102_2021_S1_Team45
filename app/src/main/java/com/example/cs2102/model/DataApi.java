package com.example.cs2102.model;

import com.example.cs2102.constants.Url;

import java.util.List;

import io.reactivex.Single;
import retrofit2.http.GET;

public interface DataApi {

    @GET(Url.USERS)
    Single<List<User>> getUsers();

    @GET(Url.PET_OWNERS)
    Single<List<PetOwner>> getPetOwners();

    @GET(Url.PETS)
    Single<List<Pet>> getPets();
}
