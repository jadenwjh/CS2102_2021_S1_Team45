package com.example.cs2102.model.retrofitApi;

import com.example.cs2102.widgets.Strings;
import com.example.cs2102.model.CareTaker;
import com.example.cs2102.model.PetOwner;
import com.example.cs2102.model.PetTypeCost;
import com.example.cs2102.model.User;

import java.util.List;

import io.reactivex.Single;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface DataApi {

    @POST(Strings.LOGIN)
    Single<User> verifyUser(@Query("username") String username,
                            @Query("password") String password,
                            @Query("type") String type);

    // missing
    @GET(Strings.LOGIN)
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

    @GET(Strings.CT_BIDS)
    Single<List<PetOwner>> getBidsReceived(@Query("username") String username);

    @GET(Strings.PETS_THE_CARE_TAKER_CAN_TAKE_CARE)
    Single<List<PetTypeCost>> getPetsForCare(@Query("username") String username);

    @POST(Strings.PETS_THE_CARE_TAKER_CAN_TAKE_CARE)
    Single<PetTypeCost> updateCost(@Field("username") String username,
                                   @Field("petType")String type,
                                   @Field("price") double price);

    @GET(Strings.CARE_TAKERS_AVAILABLE)
    Single<List<CareTaker>> getCareTakers();

    @POST(Strings.CT_FULL_TIME_LEAVE)
    Single<String> applyLeave(@Field("username") String username,
                            @Field("date") String leaveDate);

    @POST(Strings.CT_BIDS)
    Single<String> acceptBid(@Field("username") String username,
                             @Field("petName") String petName);

    @GET(Strings.CT_FULL_TIME_FREE)
    Single<List<String>> careTakerFullTimeFree(@Query("username") String username);

    @GET(Strings.PET_OWNERS_REQUESTS)
    Single<List<PetOwner>> getPetOwners();

    @POST(Strings.CT_PART_TIME_FREE)
    Single<String> setPartTimeFree(@Field("username") String username,
                                   @Field("date") String leaveDate);

    @GET(Strings.CARE_TAKERS)
    Single<String> getCareTakerContract(@Query("username") String username);
}
