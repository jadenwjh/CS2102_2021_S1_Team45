package com.example.cs2102.model;

import com.example.cs2102.constants.Strings;

import java.util.List;

import io.reactivex.Single;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface DataApi {

    @GET(Strings.USERS)
    Single<List<User>> getUsers();

    @GET(Strings.USERS)
    Call<User> verifyUser(@Query("username") String username,
                           @Query("password") String password,
                           @Query("type") String type);

    @POST(Strings.PET_OWNERS)
    Call<PetOwner> addPetOwner(@Field("username") String username,
                       @Field("password") String password,
                       @Field("type") String type,
                       @Field("email") String email,
                       @Field("number") String number,
                       @Field("address") String address,
                       @Field("petName") String petName,
                       @Field("petType") String petType);

    @POST(Strings.CARE_TAKERS)
    Call<CareTaker> addCareTaker(@Field("username") String username,
                               @Field("password") String password,
                               @Field("type") String type,
                               @Field("email") String email,
                               @Field("number") String number,
                               @Field("address") String address,
                               @Field("contract") String contract);

    @GET(Strings.PET_OWNERS_REQUESTS)
    Single<List<PetOwner>> getPetOwners();

    @GET(Strings.PETS)
    Single<List<Pet>> getPets();

    @GET(Strings.CARE_TAKERS_AVAILABLE)
    Single<List<CareTaker>> getCareTakers();
}
