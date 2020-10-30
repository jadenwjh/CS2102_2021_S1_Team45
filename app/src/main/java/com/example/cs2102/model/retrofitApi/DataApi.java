package com.example.cs2102.model.retrofitApi;

import com.example.cs2102.model.CareTaker;
import com.example.cs2102.model.PetOwner;
import com.example.cs2102.model.PetTypeCost;
import com.example.cs2102.widgets.Strings;
import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface DataApi {

    @GET(Strings.CARE_TAKERS)
    Single<ArrayList<LinkedTreeMap<String,String>>> getCareTakerContract(@Query("careTaker") String username);

    @POST(Strings.LOGIN)
    Single<ArrayList<LinkedTreeMap<String,String>>> verifyUser(@Body HashMap<String, String> params);

    @POST(Strings.REGISTER)
    Completable addPetOwner(@Body HashMap<String, String> params);

    @POST(Strings.REGISTER)
    Completable addCareTaker(@Body HashMap<String, Object> params);

    @GET(Strings.CT_BIDS)
    Single<List<PetOwner>> getBidsReceived(@Query("username") String username);

    @GET(Strings.PETS_THE_CARE_TAKER_CAN_TAKE_CARE)
    Single<List<PetTypeCost>> getPetsForCare(@Query("username") String username);

    @POST(Strings.PETS_THE_CARE_TAKER_CAN_TAKE_CARE)
    Completable updateCost(@Field("username") String username,
                                   @Field("petType")String type,
                                   @Field("price") double price);

    @GET(Strings.CARE_TAKERS_AVAILABLE)
    Single<List<CareTaker>> getCareTakers();

    @POST(Strings.CT_FULL_TIME_LEAVE)
    Completable applyLeave(@Field("username") String username,
                            @Field("date") String leaveDate);

    @POST(Strings.CT_BIDS)
    Completable acceptBid(@Field("username") String username,
                             @Field("petName") String petName);

    @GET(Strings.CT_FULL_TIME_FREE)
    Single<List<String>> careTakerFullTimeFree(@Query("username") String username);

    @GET(Strings.PET_OWNERS_REQUESTS)
    Single<List<PetOwner>> getPetOwners();

    @POST(Strings.CT_PART_TIME_FREE)
    Completable setPartTimeFree(@Field("username") String username,
                                   @Field("date") String leaveDate);

}
