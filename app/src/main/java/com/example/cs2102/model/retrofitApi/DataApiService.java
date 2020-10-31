package com.example.cs2102.model.retrofitApi;

import com.example.cs2102.widgets.Strings;
import com.example.cs2102.model.CareTaker;
import com.example.cs2102.model.PetOwner;
import com.example.cs2102.model.PetTypeCost;
import com.example.cs2102.model.User;
import com.google.gson.JsonElement;
import com.google.gson.internal.LinkedTreeMap;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Single;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class DataApiService {

    //singleton class

    private static DataApiService dataApiServiceInstance;

    private DataApiService() {}

    OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5,TimeUnit.SECONDS).build();

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

    // all possible queries

    public Single<List<CareTaker>> getCareTakers() {
        return dataApi.getCareTakers();
    }

    public Single<ArrayList<LinkedTreeMap<String,String>>> getBids(String careTakerName) {
        return dataApi.getBidsReceived(careTakerName);
    }

    public Single<ArrayList<LinkedTreeMap<String,String>>> verifyLogin(String uName, String pw, String type) {
        HashMap<String, String> params = new HashMap<>();
        params.put("username", uName);
        params.put("password", pw);
        params.put("acctype",type);
        return dataApi.verifyUser(params);
    }

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

    public Single<ArrayList<LinkedTreeMap<String,String>>> getPetsForCare(String careTakerUsername) {
        return dataApi.getPetsForCare(careTakerUsername);
    }



    public Completable applyLeave(String username, String date) {
        return dataApi.applyLeave(username, date);
    }

    public Completable acceptBid(String petOwner, String petName, String careTaker, String avail, String approveReject) {
        HashMap<String, String> params = new HashMap<>();
        params.put("petowner", petOwner);
        params.put("petname", petName);
        params.put("caretaker", careTaker);
        params.put("avail", avail);
        params.put("approveReject", approveReject);
        return dataApi.acceptBid(params);
    }

    public Single<List<String>> careTakerFullTimeFree(String username) {
        return dataApi.careTakerFullTimeFree(username);
    }

    public Completable setPartTimerFree(String username, String date) {
        return dataApi.setPartTimeFree(username, date);
    }

    public Single<LinkedTreeMap<String,String>> getCTContract(String username) {
        return dataApi.getCareTakerContract(username);
    }

    public Single<List<PetOwner>> getPetOwners() {
        return dataApi.getPetOwners();
    }
}
