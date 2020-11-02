package com.example.cs2102.model.retrofitApi;

import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Single;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class DataApiService {

    private static DataApiService dataApiServiceInstance;

    private DataApiService() {}

    OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(3,TimeUnit.SECONDS).build();

    public DataApi dataApi =  new Retrofit.Builder()
            .baseUrl(Strings.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(DataApi.class);

    public static DataApiService getInstance() {
        if (dataApiServiceInstance == null) {
            dataApiServiceInstance = new DataApiService();
        }
        return dataApiServiceInstance;
    }

    /**
     * LOGIN + REGISTER
     * */

    public Completable addPetOwner(String username, String email, String password, String profile, String address, int phoneNum, int creditCard, int bankAcc, String acctype) {
        HashMap<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("email",email);
        params.put("password", password);
        params.put("profile", profile);
        params.put("address", address);
        params.put("phoneNum", String.valueOf(phoneNum));
        params.put("creditCard", String.valueOf(creditCard));
        params.put("bankAcc", String.valueOf(bankAcc));
        params.put("acctype", acctype);
        return dataApi.addPetOwner(params);
    }
    public Completable addCareTaker(String username, String email, String password, String profile, String address, int phoneNum, int creditCard, int bankAcc, String acctype, boolean isPT, String admin) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("username", username);
        params.put("email",email);
        params.put("password", password);
        params.put("profile", profile);
        params.put("address", address);
        params.put("phoneNum", String.valueOf(phoneNum));
        params.put("creditCard", String.valueOf(creditCard));
        params.put("bankAcc", String.valueOf(bankAcc));
        params.put("acctype", acctype);
        params.put("isPartTime", isPT);
        params.put("admin", admin);
        return dataApi.addCareTaker(params);
    }
    public Single<ArrayList<LinkedTreeMap<String,String>>> verifyLogin(String uName, String pw, String type) {
        HashMap<String, String> params = new HashMap<>();
        params.put("username", uName);
        params.put("password", pw);
        params.put("acctype",type);
        return dataApi.verifyUser(params);
    }

    /**
     * CARE TAKER HOMEPAGE
     * */

    public Single<LinkedTreeMap<String,String>> getCTContract(String username) {
        return dataApi.getCareTakerContract(username);
    }

    // Bids Page
    public Completable acceptRejectBid(String petOwner, String petName, String careTaker, String avail, String approveReject) {
        HashMap<String, String> params = new HashMap<>();
        params.put("petowner", petOwner);
        params.put("petname", petName);
        params.put("caretaker", careTaker);
        params.put("avail", avail);
        params.put("approveReject", approveReject);
        return dataApi.acceptBid(params);
    }
    public Single<ArrayList<LinkedTreeMap<String,String>>> getBids(String careTakerName) {
        return dataApi.getBidsReceived(careTakerName);
    }

    // Prices Page
    public Completable updatePricePetType(String username, String petType, int price) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("caretaker", username);
        params.put("petType", petType);
        params.put("price", price);
        return dataApi.updateCost(params);
    }
    public Completable addPetTypeForCT(String username, String petType, int price) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("caretaker", username);
        params.put("category", petType);
        params.put("feeperday", price);
        return dataApi.addPetTypeToCare(params);
    }
    public Completable deletePetTypeForCT(String username, String petType) {
        HashMap<String, String> params = new HashMap<>();
        params.put("caretaker", username);
        params.put("category", petType);
        return dataApi.deletePetTypeToCare(params);
    }
    public Single<ArrayList<LinkedTreeMap<String,String>>> getCTPetTypes(String username) {
        return dataApi.getPetTypesForCT(username);
    }
    public Single<ArrayList<LinkedTreeMap<String,String>>> getPetsForCare(String careTakerUsername) {
        return dataApi.getPetsForCare(careTakerUsername);
    }

    // Availability page
    public Completable applyLeave(String username, String date) {
        HashMap<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("sdate", date);
        params.put("edate", date);
        return dataApi.applyLeave(params);
    }
    public Completable setPartTimerFree(String username, String date) {
        HashMap<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("sdate", date);
        params.put("edate", date);
        return dataApi.setPartTimeFree(params);
    }

    /**
     * PET OWNER HOMEPAGE
     * */

    // Listings Page
    public Single<ArrayList<LinkedTreeMap<String,String>>> getPetTypes(String username) {
        return dataApi.getPetTypes(username);
    }
    public Single<ArrayList<LinkedTreeMap<String,String>>> getPOListings(String type, String sdate, String edate) {
        HashMap<String, String> params = new HashMap<>();
        params.put("category", type);
        params.put("sdate", sdate);
        params.put("edate", edate);
        return dataApi.getListings(params);
    }
    public Completable submitPObid(String username, String petName, String careTaker, String sdate, String edate, String payment, float price) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("petowner", username);
        params.put("petname", petName);
        params.put("caretaker", careTaker);
        params.put("sdate", sdate);
        params.put("edate", edate);
        params.put("transferType", payment);
        params.put("paymentType", payment);
        params.put("price", price);
        return dataApi.sendBidRequest(params);
    }
    public Single<ArrayList<LinkedTreeMap<String,String>>> getPetNamesOfType(String username, String petType) {
        return dataApi.getPetNamesOfType(username, petType);
    }

    // Pets Page
    public Single<ArrayList<LinkedTreeMap<String,String>>> getOwnedPets(String username) {
        return dataApi.currentlyOwnedPets(username);
    }
    public Single<ArrayList<LinkedTreeMap<String,String>>> getAllPetTypes() {
        return dataApi.getAllPetTypes();
    }
    public Completable addNewPetOwnerPet(String username, String petname, String type, String profile, String requests) {
        HashMap<String,String> params = new HashMap<>();
        params.put("petowner", username);
        params.put("petname", petname);
        params.put("profile", profile);
        params.put("specialReq", requests);
        params.put("category", type);
        return dataApi.addNewPetOwnerPet(params);
    }
    public Completable updatePetOwnerPet(String username, String petname, String type, String profile, String requests) {
        HashMap<String,String> params = new HashMap<>();
        params.put("petowner", username);
        params.put("petname", petname);
        params.put("profile", profile);
        params.put("specialReq", requests);
        params.put("category", type);
        return dataApi.updatePetOwnerPet(username, petname, params);
    }
    public Completable deletePetOwnerPet(String username, String petName) {
        HashMap<String,String> params = new HashMap<>();
        params.put("petowner", username);
        params.put("petname", petName);
        return dataApi.deletePetOwnerPet(username, petName, params);
    }
}
