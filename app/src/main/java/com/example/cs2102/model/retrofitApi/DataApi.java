package com.example.cs2102.model.retrofitApi;

import com.example.cs2102.model.CareTaker;
import com.example.cs2102.model.PetOwner;
import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface DataApi {

    /**
     * LOGIN + REGISTER
     * */

    @POST(Strings.LOGIN)
    Single<ArrayList<LinkedTreeMap<String,String>>> verifyUser(@Body HashMap<String, String> params);

    @POST(Strings.REGISTER)
    Completable addPetOwner(@Body HashMap<String, String> params);

    @POST(Strings.REGISTER)
    Completable addCareTaker(@Body HashMap<String, Object> params);

    /**
     * CARE TAKER
     * */

    @GET(Strings.CARE_TAKERS_CONTRACT + "/{username}")
    Single<LinkedTreeMap<String,String>> getCareTakerContract(@Path("username") String username);

    @GET(Strings.CT_BIDS + "/{username}")
    Single<ArrayList<LinkedTreeMap<String,String>>> getBidsReceived(@Path("username") String username);

    @GET(Strings.UPDATE_PRICE_CT + "/{username}")
    Single<ArrayList<LinkedTreeMap<String,String>>> getPetsForCare(@Path("username") String username);

    @GET(Strings.PETS_THE_CARE_TAKER_CAN_TAKE_CARE + "/{caretaker}")
    Single<ArrayList<LinkedTreeMap<String,String>>> getPetTypesForCT(@Path("caretaker") String username);

    @POST(Strings.PETS_THE_CARE_TAKER_CAN_TAKE_CARE)
    Completable addPetTypeToCare(@Body HashMap<String, Object> params);

    @HTTP(method = "DELETE", path = Strings.PETS_THE_CARE_TAKER_CAN_TAKE_CARE, hasBody = true)
    Completable deletePetTypeToCare(@Body HashMap<String, String> params);

    @PUT(Strings.UPDATE_PRICE_CT)
    Completable updateCost(@Body HashMap<String,Object> params);

    @POST(Strings.CT_FULL_TIME_LEAVE)
    Completable applyLeave(@Body HashMap<String, String> params);

    @PUT(Strings.CT_BIDS)
    Completable acceptBid(@Body HashMap<String, String> params);

    @POST(Strings.CT_PART_TIME_FREE)
    Completable setPartTimeFree(@Body HashMap<String, String> params);

    /**
     * PET OWNER
     * */


}
