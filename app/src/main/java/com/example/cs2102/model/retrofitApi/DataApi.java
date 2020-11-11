package com.example.cs2102.model.retrofitApi;

import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.Completable;
import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface DataApi {

    /**
     * LOGIN + REGISTER + DELETE
     * */

    @POST(Strings.LOGIN)
    Single<ArrayList<LinkedTreeMap<String,String>>> verifyUser(@Body HashMap<String, String> params);

    @POST(Strings.REGISTER)
    Completable addPetOwner(@Body HashMap<String, String> params);

    @POST(Strings.REGISTER)
    Completable addCareTaker(@Body HashMap<String, Object> params);

    @HTTP(method = "DELETE", path = Strings.DELETE_USER, hasBody = true)
    Completable deleteUser(@Body HashMap<String, String> params);

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

    @POST(Strings.CT_SALARY)
    Single<LinkedTreeMap<String,String>> getCTSalary(@Body HashMap<String, String> params);

    @GET(Strings.CT_REVIEWS + "/{username}")
    Single<ArrayList<LinkedTreeMap<String,String>>> getCTReview(@Path("username") String username);

    /**
     * PET OWNER
     * */

    @POST(Strings.PO_GET_LISTINGS)
    Single<ArrayList<LinkedTreeMap<String,String>>> getListings(@Body HashMap<String, String> params);

    @GET(Strings.PETS_PETOWNER + "/{username}")
    Single<ArrayList<LinkedTreeMap<String,String>>> getPetTypes(@Path("username") String username);

    @POST(Strings.SEND_BID_REQUEST)
    Completable sendBidRequest(@Body HashMap<String, Object> params);

    @GET(Strings.PETS_PETOWNER + "/{username}/{category}")
    Single<ArrayList<LinkedTreeMap<String,String>>> getPetNamesOfType(@Path("username") String username,@Path("category") String petType);

    @GET(Strings.PETS_PETOWNER + "/{username}")
    Single<ArrayList<LinkedTreeMap<String,String>>> currentlyOwnedPets(@Path("username") String username);

    @GET(Strings.ALL_PET_TYPES)
    Single<ArrayList<LinkedTreeMap<String,String>>> getAllPetTypes();

    @POST(Strings.ADD_PETOWNER_PET)
    Completable addNewPetOwnerPet(@Body HashMap<String, String> params);

    @PUT(Strings.PETS_PETOWNER + "/{username}/{petName}")
    Completable updatePetOwnerPet(@Path("username") String username, @Path("petName") String petName, @Body HashMap<String, String> params);

    @HTTP(method = "DELETE", path = Strings.PETS_PETOWNER + "/{username}/{petName}", hasBody = true)
    Completable deletePetOwnerPet(@Path("username") String username, @Path("petName") String petName, @Body HashMap<String, String> params);

    @GET(Strings.ONGOING_BIDS + "/{username}")
    Single<ArrayList<LinkedTreeMap<String,String>>> getOngoingBids(@Path("username") String username);

    @GET(Strings.EXPIRED_BIDS + "/{username}/history")
    Single<ArrayList<LinkedTreeMap<String,String>>> getExpiredBids(@Path("username") String username);

    @POST(Strings.LEAVE_RATING)
    Completable leaveReview(@Body HashMap<String, Object> params);

    /**
     * ADMIN
     * */

    @POST(Strings.ALL_PET_TYPES)
    Completable updateBasePrice(@Body HashMap<String,Object> params);

    @GET(Strings.GET_RATINGS + "/{username}")
    Single<ArrayList<LinkedTreeMap<String,String>>> getRatings(@Path("username") String username);

    @GET(Strings.GET_CTINFO + "/{username}/{date}")
    Single<ArrayList<LinkedTreeMap<String,String>>> getCTInfo(@Path("username") String username, @Path("date") String date);

    @GET(Strings.STATS + "/{username}/{date}")
    Single<LinkedTreeMap<String,String>> getStats(@Path("username") String username, @Path("date") String date);

    @POST(Strings.FINANCES)
    Single<ArrayList<LinkedTreeMap<String,String>>> getFinances(@Body HashMap<String,Object> params);
}