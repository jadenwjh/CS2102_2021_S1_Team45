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
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20,TimeUnit.SECONDS).build();

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
     * LOGIN + REGISTER + DELETE
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
    public Completable addBoth(String username, String email, String password, String profile, String address, int phoneNum, int creditCard, int bankAcc, boolean isPT, String admin) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("username", username);
        params.put("email",email);
        params.put("password", password);
        params.put("profile", profile);
        params.put("address", address);
        params.put("phoneNum", String.valueOf(phoneNum));
        params.put("creditCard", String.valueOf(creditCard));
        params.put("bankAcc", String.valueOf(bankAcc));
        params.put("acctype", "both");
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
    public Completable deleteUser(String username, String password) {
        HashMap<String,String> params = new HashMap<>();
        params.put("username", username);
        params.put("password", password);
        return dataApi.deleteUser(params);
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

    // Salary page
    public Single<LinkedTreeMap<String,String>> fetchCTSalary(String username, String date) {
        HashMap<String,String> params = new HashMap<>();
        params.put("caretaker", username);
        params.put("date", date);
        return dataApi.getCTSalary(params);
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
    public Completable submitPObid(String username, String petName, String careTaker, String sdate, String edate, String payment, float price, String transfer) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("petowner", username);
        params.put("petname", petName);
        params.put("caretaker", careTaker);
        params.put("sdate", sdate);
        params.put("edate", edate);
        params.put("transferType", transfer);
        params.put("paymentType", payment);
        params.put("price", price);
        return dataApi.sendBidRequest(params);
    }
    public Single<ArrayList<LinkedTreeMap<String,String>>> getPetNamesOfType(String username, String petType) {
        return dataApi.getPetNamesOfType(username, petType);
    }
    public Single<ArrayList<LinkedTreeMap<String,String>>> getCareTakerReviews(String username) {
        return dataApi.getCTReview(username);
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
        params.put("specialreq", requests);
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

    // Bids page
    public Single<ArrayList<LinkedTreeMap<String,String>>> getOngoingBids(String username) {
        return dataApi.getOngoingBids(username);
    }
    public Single<ArrayList<LinkedTreeMap<String,String>>> getExpiredBids(String username) {
        return dataApi.getExpiredBids(username);
    }
    public Completable leaveReview(String petOwner, String petName, String careTaker, String avail, int rating, String review, boolean isPaid) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("petowner", petOwner);
        params.put("petname", petName);
        params.put("caretaker", careTaker);
        params.put("avail", avail);
        params.put("rating", rating);
        params.put("review", review);
        params.put("isPaid", isPaid);
        return dataApi.leaveReview(params);
    }

    /**
     * ADMIN HOMEPAGE
     * */

    //Pet Config
    //use getAllPetTypes from Pets Page
    public Completable updatePetBasePrice(String type, float price) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("basePrice", price);
        params.put("category", type);
        return dataApi.updateBasePrice(params);
    }
    public Single<ArrayList<LinkedTreeMap<String,String>>> fetchCTInfo(String username, String date) {
        return dataApi.getCTInfo(username, date);
    }
    public Single<ArrayList<LinkedTreeMap<String,String>>> fetchFinances(String sdate, String edate) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("sdate", sdate);
        params.put("edate", edate);
        return dataApi.getFinances(params);
    }
}
