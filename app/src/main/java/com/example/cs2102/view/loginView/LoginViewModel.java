package com.example.cs2102.view.loginView;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cs2102.model.retrofitApi.DataApiService;
import com.google.gson.JsonElement;
import com.google.gson.internal.LinkedTreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class LoginViewModel extends ViewModel {

    public MutableLiveData<Boolean> loadError = new MutableLiveData<>();
    public MutableLiveData<Boolean> loading = new MutableLiveData<>();
    public MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();
    public MutableLiveData<LinkedTreeMap<String,String>> userProfile = new MutableLiveData<>();

    private DataApiService dataApiService = DataApiService.getInstance();
    private CompositeDisposable disposable = new CompositeDisposable();

    public void loginAttempt(String username, String password, String type) {
        Log.e("Login", String.format("Username:%s, Password:%s, Type:%s", username,password,type));
        loginSuccess.setValue(false);
        loading.setValue(true);
        disposable.add(dataApiService.verifyLogin(username, password, type)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<ArrayList<LinkedTreeMap<String,String>>>() {

                    @Override
                    public void onSuccess(ArrayList<LinkedTreeMap<String,String>> details) {
                        Log.e("Login", "Success");
                        userProfile.setValue(details.get(0));
                        Log.e("User Details", details.get(0).toString());
                        loadError.setValue(false);
                        loading.setValue(false);
                        loginSuccess.setValue(true);

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("RequestError", String.valueOf(e));
                        Log.e("Login", "Failed");
                        loadError.setValue(true);
                        loading.setValue(false);
                        e.printStackTrace();
                    }
                })
        );
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }
}
