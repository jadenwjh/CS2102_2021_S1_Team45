package com.example.cs2102.view.petOwnerView.viewModel;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cs2102.model.retrofitApi.DataApiService;
import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class PetOwnerListingViewModel extends ViewModel {

    public MutableLiveData<ArrayList<LinkedTreeMap<String,String>>> careTakers = new MutableLiveData<>();
    public MutableLiveData<Boolean> loading = new MutableLiveData<>();
    public MutableLiveData<Boolean> emptyLists = new MutableLiveData<>();
    public MutableLiveData<Boolean> noPets = new MutableLiveData<>();
    public MutableLiveData<String[]> petTypes = new MutableLiveData<>();

    private DataApiService dataApiService = DataApiService.getInstance();
    private CompositeDisposable disposable = new CompositeDisposable();

    public void refreshListings(String category, String start, String end, String username) {
        fetchListings(category, start, end);
        fetchPetTypes(username);
    }

    public void fetchListings(String category, String start, String end) {
        loading.setValue(true);
        emptyLists.setValue(true);
        Log.e("fetchListings", String.format("Type: %s, Date: %s - %s", category, start, end));
        disposable.add(dataApiService.getPOListings(category, start, end)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<ArrayList<LinkedTreeMap<String,String>>>() {
                    @Override
                    public void onSuccess(ArrayList<LinkedTreeMap<String,String>> _careTakers) {
                        careTakers.setValue(_careTakers);
                        loading.setValue(false);
                        Log.e("fetchListings", "Success");
                        if (_careTakers.size() != 0) {
                            emptyLists.setValue(false);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("fetchListings", "Failed");
                        loading.setValue(false);
                        e.printStackTrace();
                    }
                })
        );
    }

    public void fetchPetTypes(String username) {
        noPets.setValue(false);
        loading.setValue(true);
        disposable.add(dataApiService.getPetTypes(username)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<ArrayList<LinkedTreeMap<String,String>>>() {
                    @Override
                    public void onSuccess(ArrayList<LinkedTreeMap<String,String>> _pets) {
                        String[] types = new String[_pets.size()];
                        int i = 0;
                        for (LinkedTreeMap<String,String> pet : _pets) {
                            types[i] = pet.get("category");
                            i++;
                        }
                        if (types.length == 0) {
                            noPets.setValue(true);
                        }
                        petTypes.setValue(types);
                        loading.setValue(false);
                        Log.e("fetchPetTypes", "Success");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("fetchPetTypes", "Failed");
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