package com.example.cs2102.model.retrofitApi;

import com.example.cs2102.widgets.Strings;
import com.example.cs2102.model.CareTaker;
import com.example.cs2102.model.PetOwner;
import com.example.cs2102.model.PetTypeCost;
import com.example.cs2102.model.User;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class DataApiService {

    //singleton class

    private static DataApiService dataApiServiceInstance;

    private DataApiService() {}

    public DataApi dataApi =  new Retrofit.Builder()
            .baseUrl(Strings.BASE_URL)
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

    // all possible queries

    public Single<List<CareTaker>> getCareTakers() {
        return dataApi.getCareTakers();
    }

    public Single<List<PetOwner>> getBids(String careTakerName) {
        return dataApi.getBidsReceived(careTakerName);
    }

    public Completable verifyLogin(String uName, String pw, String type) {
        return dataApi.verifyUser(uName, pw, type);
    }

    public Single<User> getUsername(String uName) {
        return dataApi.getUsername(uName);
    }

    public Completable addPetOwner(String username, String email, String password, String profile, String address, int phoneNum, int creditCard, int bankAcc, String acctype) {
        return dataApi.addPetOwner(username, email, password, profile, address, phoneNum, creditCard, bankAcc, acctype);
    }

    public Completable addCareTaker(String username, String email, String password, String profile, String address, int phoneNum, int creditCard, int bankAcc, String acctype, boolean isPT, String admin) {
        return dataApi.addCareTaker(username, email, password, profile, address, phoneNum, creditCard, bankAcc, acctype, isPT, admin);
    }

    public Single<List<PetTypeCost>> getPetsForCare(String careTakerUsername) {
        return dataApi.getPetsForCare(careTakerUsername);
    }

    public Completable updateCostOfPetType(String careTakerUsername, String type, double price) {
        return dataApi.updateCost(careTakerUsername, type, price);
    }

    public Completable applyLeave(String username, String date) {
        return dataApi.applyLeave(username, date);
    }

    public Completable acceptBid(String username, String petName) {
        return dataApi.acceptBid(username, petName);
    }

    public Single<List<String>> careTakerFullTimeFree(String username) {
        return dataApi.careTakerFullTimeFree(username);
    }

    public Completable setPartTimerFree(String username, String date) {
        return dataApi.setPartTimeFree(username, date);
    }

    public Single<String> getCTContract(String username) {
        return dataApi.getCareTakerContract(username);
    }

    public Single<List<PetOwner>> getPetOwners() {
        return dataApi.getPetOwners();
    }
}
