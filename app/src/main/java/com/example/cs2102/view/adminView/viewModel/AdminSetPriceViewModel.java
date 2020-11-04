package com.example.cs2102.view.adminView.viewModel;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cs2102.model.PetType;
import com.example.cs2102.model.retrofitApi.DataApiService;
import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class AdminSetPriceViewModel extends ViewModel {

    public MutableLiveData<List<PetType>> petBasePrices = new MutableLiveData<>();
    public MutableLiveData<Boolean> loading = new MutableLiveData<>();
    public MutableLiveData<Boolean> updatedPrice = new MutableLiveData<>();


    private DataApiService dataApiService = DataApiService.getInstance();
    private CompositeDisposable disposable = new CompositeDisposable();

    public void fetchBasePrices() {
        loading.setValue(true);
        disposable.add(dataApiService.getAllPetTypes()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<ArrayList<LinkedTreeMap<String,String>>>() {
                    @Override
                    public void onSuccess(ArrayList<LinkedTreeMap<String,String>> _pets) {
                        List<PetType> list = new ArrayList<>();
                        for (LinkedTreeMap<String,String> pet : _pets) {
                            String type = pet.get("category");
                            String price = pet.get("baseprice");
                            PetType petType = new PetType(type, price);
                            list.add(petType);
                        }
                        petBasePrices.setValue(list);
                        loading.setValue(false);
                        Log.e("fetchBasePrices", "Success");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("fetchBasePrices", "Failed");
                        loading.setValue(false);
                    }
                })
        );
    }

    //add update same
    public void updateBasePrice(String type, float price, Context context) {
        loading.setValue(true);
        updatedPrice.setValue(false);
        disposable.add(dataApiService.updatePetBasePrice(type, price)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        loading.setValue(false);
                        Toast.makeText(context, "Successfully updated base price", Toast.LENGTH_SHORT).show();
                        Log.e("updateBasePrice", "Success");
                        updatedPrice.setValue(true);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("updateBasePrice", "Failed");
                        Toast.makeText(context, "Failed to update base price", Toast.LENGTH_SHORT).show();
                        loading.setValue(false);
                        updatedPrice.setValue(false);
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