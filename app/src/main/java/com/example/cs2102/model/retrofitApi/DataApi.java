package com.example.cs2102.model.retrofitApi;

import com.example.cs2102.constants.Strings;
import com.example.cs2102.model.CareTaker;
import com.example.cs2102.model.Pet;
import com.example.cs2102.model.PetOwner;
import com.example.cs2102.model.User;

import java.util.List;

import io.reactivex.Single;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface DataApi {

    @GET(Strings.USERS)
    Single<User> verifyUser(@Query("username") String username,
                            @Query("password") String password,
                            @Query("type") String type);

    @GET(Strings.USERS)
    Single<User> getUsername(@Query("username") String username);

    @POST(Strings.PET_OWNERS)
    Single<PetOwner> addPetOwner(@Field("username") String username,
                                 @Field("password") String password,
                                 @Field("type") String type,
                                 @Field("email") String email,
                                 @Field("number") String number,
                                 @Field("address") String address,
                                 @Field("petName") String petName,
                                 @Field("petType") String petType);

    @POST(Strings.CARE_TAKERS)
    Single<CareTaker> addCareTaker(@Field("username") String username,
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
