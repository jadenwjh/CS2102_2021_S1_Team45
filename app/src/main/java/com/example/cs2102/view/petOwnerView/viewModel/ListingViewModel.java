package com.example.cs2102.view.petOwnerView.viewModel;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cs2102.model.retrofitApi.DataApiService;
import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class ListingViewModel extends ViewModel {

    public MutableLiveData<String[]> ownedPets = new MutableLiveData<>();
    public MutableLiveData<Boolean> loading = new MutableLiveData<>();
    public MutableLiveData<Boolean> bidSubmitted = new MutableLiveData<>();

    private DataApiService dataApiService = DataApiService.getInstance();
    private CompositeDisposable disposable = new CompositeDisposable();

    public void submitBid(String username, String petName, String careTaker, String sdate, String edate, String payment, float price) {
        bidSubmitted.setValue(false);
        loading.setValue(true);
        disposable.add(dataApiService.submitPObid(username, petName, careTaker, sdate, edate, payment, price)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        Log.e("submitBid", "Success");
                        loading.setValue(false);
                        bidSubmitted.setValue(true);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("submitBid", "Failed");
                        loading.setValue(false);
                        e.printStackTrace();
                    }
                })
        );
    }

    public void fetchOwnedPets(String username, String petType) {
        loading.setValue(true);
        disposable.add(dataApiService.getPetNamesOfType(username, petType)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<ArrayList<LinkedTreeMap<String,String>>>() {
                    @Override
                    public void onSuccess(ArrayList<LinkedTreeMap<String,String>> _pets) {
                        String[] names = new String[_pets.size()];
                        int i = 0;
                        for (LinkedTreeMap<String,String> pet : _pets) {
                            names[i] = pet.get("petname");
                            i++;
                        }
                        ownedPets.setValue(names);
                        loading.setValue(false);
                        Log.e("fetchOwnedPets", "Success");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("fetchOwnedPets", "Failed");
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