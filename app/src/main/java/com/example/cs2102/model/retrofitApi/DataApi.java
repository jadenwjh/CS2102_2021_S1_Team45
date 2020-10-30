package com.example.cs2102.model.retrofitApi;

import com.example.cs2102.widgets.Strings;
import com.example.cs2102.model.CareTaker;
import com.example.cs2102.model.PetOwner;
import com.example.cs2102.model.PetTypeCost;
import com.example.cs2102.model.User;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface DataApi {

    @FormUrlEncoded
    @POST(Strings.LOGIN)
    Completable verifyUser(@Query("username") String username,
                            @Query("password") String password,
                            @Query("type") String type);

    // missing
    @GET(Strings.LOGIN)
    Single<User> getUsername(@Query("username") String username);

    @FormUrlEncoded
    @POST(Strings.REGISTER) //"Users/register"
    Completable addPetOwner(@Field("username") String username,
                            @Field("email") String email,
                            @Field("password") String password,
                            @Field("profile") String profile,
                            @Field("address") String address,
                            @Field("phoneNum") int phoneNum,
                            @Field("creditCard") int creditCard,
                            @Field("bankAcc") int bankAcc,
                            @Field("acctype") String acctype);

    @FormUrlEncoded
    @POST(Strings.REGISTER)
    Completable addCareTaker(@Field("username") String username,
                             @Field("email") String email,
                             @Field("password") String password,
                             @Field("profile") String profile,
                             @Field("address") String address,
                             @Field("phoneNum") int phoneNum,
                             @Field("creditCard") int creditCard,
                             @Field("bankAcc") int bankAcc,
                             @Field("acctype") String acctype,
                             // new fields
                             @Field("isPartTime") boolean isPartTime,
                             @Field("admin") String admin);
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

    @GET(Strings.CARE_TAKERS)
    Single<String> getCareTakerContract(@Query("username") String username);
}
