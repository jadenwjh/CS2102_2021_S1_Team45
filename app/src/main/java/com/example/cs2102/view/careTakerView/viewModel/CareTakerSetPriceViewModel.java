package com.example.cs2102.view.careTakerView.viewModel;

import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cs2102.model.Pet;
import com.example.cs2102.model.PetType;
import com.example.cs2102.model.PetTypeCost;
import com.example.cs2102.model.retrofitApi.DataApiService;
import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class CareTakerSetPriceViewModel extends ViewModel {

    public MutableLiveData<Boolean> loadError = new MutableLiveData<Boolean>();
    public MutableLiveData<Boolean> loading = new MutableLiveData<Boolean>();
    public MutableLiveData<Boolean> setPriceError = new MutableLiveData<Boolean>();
    public MutableLiveData<List<PetTypeCost>> petTypeCosts = new MutableLiveData<>();
    public MutableLiveData<PetTypeCost> selectedPetTypeCost = new MutableLiveData<>();
    public MutableLiveData<String[]> petTypeBasePrices = new MutableLiveData<>();
    public MutableLiveData<String[]> petTypeAdapter = new MutableLiveData<>();
    public MutableLiveData<String[]> removePetTypeAdapter = new MutableLiveData<>();


    private DataApiService dataApiService = DataApiService.getInstance();
    private CompositeDisposable disposable = new CompositeDisposable();

    public void refreshPage(String careTakerUsername) {
        fetchPetTypeCosts(careTakerUsername);
        fetchPetTypes(careTakerUsername);
    }

    public String[] getPetTypesToShow() {
        if (petTypeAdapter.getValue() != null) {
            return petTypeAdapter.getValue();
        } else {
            return new String[0];
        }
    }

    public String[] getDeletePetTypesToShow() {
        if (removePetTypeAdapter.getValue() != null) {
            return removePetTypeAdapter.getValue();
        } else {
            return new String[0];
        }
    }

    public void fetchPetTypeCosts(String careTakerUsername) {
        loading.setValue(true);
        disposable.add(dataApiService.getPetsForCare(careTakerUsername)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<ArrayList<LinkedTreeMap<String,String>>>() {

                    @Override
                    public void onSuccess(ArrayList<LinkedTreeMap<String,String>> _petTypeCosts) {
                        Log.e("fetchPetTypeCosts", "Success");
                        List<PetTypeCost> list = new ArrayList<>();
                        String[] types = new String[_petTypeCosts.size()];
                        int i = 0;
                        for (LinkedTreeMap<String,String> type : _petTypeCosts) {
                            PetTypeCost item = new PetTypeCost(careTakerUsername, type.get("category"), type.get("feeperday"), type.get("baseprice"), type.get("upperlimit"));
                            list.add(item);
                            types[i] = type.get("category");
                            i++;
                        }
                        removePetTypeAdapter.setValue(types);
                        petTypeCosts.setValue(list);
                        loadError.setValue(false);
                        loading.setValue(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("fetchPetTypeCosts", "Fail");
                        loadError.setValue(true);
                        loading.setValue(false);
                        e.printStackTrace();
                    }
                })
        );
    }

    public void updatePetTypeCost(String careTakerUsername, String petType, int price) {
        loading.setValue(true);
        disposable.add(dataApiService.updatePricePetType(careTakerUsername, petType, price)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableCompletableObserver() {

                    @Override
                    public void onComplete() {
                        Log.e("updatePetTypeCost", "Success");
                        loading.setValue(false);
                        setPriceError.setValue(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("updatePetTypeCost", "Fail");
                        loading.setValue(false);
                        setPriceError.setValue(true);
                    }
                })
        );
    }

    public void fetchPetTypes(String username) {
        loading.setValue(true);
        disposable.add(dataApiService.getCTPetTypes(username)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<ArrayList<LinkedTreeMap<String,String>>>() {

                    @Override
                    public void onSuccess(ArrayList<LinkedTreeMap<String, String>> _petTypes) {
                        String[] stringPetTypes = new String[_petTypes.size()];
                        String[] stringPetTypesPrices = new String[_petTypes.size()];
                        int i = 0;
                        for (LinkedTreeMap<String,String> type : _petTypes) {
                            stringPetTypesPrices[i] = type.get("baseprice");
                            stringPetTypes[i] = type.get("category");
                            i++;
                        }
                        petTypeAdapter.setValue(stringPetTypes);
                        petTypeBasePrices.setValue(stringPetTypesPrices);
                        loading.setValue(false);
                        Log.e("fetchPetTypes", "Success");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("fetchPetTypes", "Fail");
                        loading.setValue(false);
                    }
                })
        );
    }

    public void addPetType(String careTakerUsername, String petType, int price) {
        Log.e("addPetType", String.format("username: %s, pettype: %s, price: %d", careTakerUsername, petType, price));
        loading.setValue(true);
        disposable.add(dataApiService.addPetTypeForCT(careTakerUsername, petType, price)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableCompletableObserver() {

                    @Override
                    public void onComplete() {
                        Log.e("addPetType", "Success");
                        loading.setValue(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("addPetType", "Fail");
                        Log.e("addPetType", e.getMessage());
                        loading.setValue(false);
                    }
                })
        );
    }

    public void deletePetType(String careTakerUsername, String petType) {
        Log.e("deletePetType", String.format("username: %s, pettype: %s", careTakerUsername, petType));
        loading.setValue(true);
        disposable.add(dataApiService.deletePetTypeForCT(careTakerUsername, petType)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableCompletableObserver() {

                    @Override
                    public void onComplete() {
                        Log.e("deletePetType", "Success");
                        loading.setValue(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("deletePetType", "Fail");
                        Log.e("deletePetType", e.getMessage());
                        loading.setValue(false);
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
