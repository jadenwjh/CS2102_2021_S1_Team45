package com.example.cs2102.view.careTakerView.viewModel;

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

public class CareTakerBidsViewModel extends ViewModel {

    public MutableLiveData<ArrayList<LinkedTreeMap<String,String>>> petOwners = new MutableLiveData<>();
    public MutableLiveData<ArrayList<LinkedTreeMap<String,String>>> acceptedBidsForCT = new MutableLiveData<>();
    public MutableLiveData<Boolean> loading = new MutableLiveData<>();

    private DataApiService dataApiService = DataApiService.getInstance();
    private CompositeDisposable disposable = new CompositeDisposable();

    public void refreshBids(String careTakerName) {
        fetchPetOwnerBids(careTakerName);
        fetchAcceptedBids(careTakerName);
    }

    public void fetchPetOwnerBids(String careTakerName) {
        loading.setValue(true);
        disposable.add(dataApiService.getBids(careTakerName)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<ArrayList<LinkedTreeMap<String,String>>>() {
                    @Override
                    public void onSuccess(ArrayList<LinkedTreeMap<String,String>> _petOwners) {
                        petOwners.setValue(_petOwners);
                        loading.setValue(false);
                        Log.e("fetchBids", "Success");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("fetchBids", "Failed");
                        loading.setValue(false);
                        e.printStackTrace();
                    }
                })
        );
    }

    public void fetchAcceptedBids(String careTakerName) {
        loading.setValue(true);
        disposable.add(dataApiService.fetchedBidsAccepted(careTakerName)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<ArrayList<LinkedTreeMap<String,String>>>() {
                    @Override
                    public void onSuccess(ArrayList<LinkedTreeMap<String,String>> _petOwners) {
                        acceptedBidsForCT.setValue(_petOwners);
                        loading.setValue(false);
                        Log.e("fetchBids", "Success");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("fetchBids", "Failed");
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