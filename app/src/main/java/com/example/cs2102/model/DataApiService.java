package com.example.cs2102.model;

import com.example.cs2102.constants.Strings;

import java.util.List;

import io.reactivex.Single;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class DataApiService {

    //singleton class

    private static DataApiService dataApiServiceInstance;

    private DataApiService() {};

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

    public Single<List<User>> getUsers() {
        return dataApi.getUsers();
    }

    public Single<List<CareTaker>> getCareTakers() {
        return dataApi.getCareTakers();
    }

    public Single<List<PetOwner>> getPetOwners() {
        return dataApi.getPetOwners();
    }

    public Single<List<Pet>> getPets() {
        return dataApi.getPets();
    }

    public Call<User> verifyLogin(String uName, String pw, String type) {
        return dataApi.verifyUser(uName, pw, type);
    }
}