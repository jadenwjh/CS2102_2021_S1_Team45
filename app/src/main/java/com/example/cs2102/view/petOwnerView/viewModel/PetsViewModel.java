package com.example.cs2102.view.petOwnerView.viewModel;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cs2102.model.Pet;
import com.example.cs2102.model.retrofitApi.DataApiService;
import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class PetsViewModel extends ViewModel {

    public MutableLiveData<String[]> allPets = new MutableLiveData<>();
    public MutableLiveData<List<Pet>> ownedPets = new MutableLiveData<>();
    public MutableLiveData<Boolean> loading = new MutableLiveData<>();
    public MutableLiveData<Boolean> showError = new MutableLiveData<>();

    private DataApiService dataApiService = DataApiService.getInstance();
    private CompositeDisposable disposable = new CompositeDisposable();

    public void refreshPage(String username) {
        fetchOwnedPets(username);
        fetchPets();
    }

    public void fetchPets() {
        loading.setValue(true);
        disposable.add(dataApiService.getAllPetTypes()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<ArrayList<LinkedTreeMap<String,String>>>() {
                    @Override
                    public void onSuccess(ArrayList<LinkedTreeMap<String,String>> _pets) {
                        String[] names = new String[_pets.size()];
                        int i = 0;
                        for (LinkedTreeMap<String,String> pet : _pets) {
                            names[i] = pet.get("category");
                            i++;
                        }
                        allPets.setValue(names);
                        loading.setValue(false);
                        Log.e("fetchPets", "Success");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("fetchPets", "Failed");
                        loading.setValue(false);
                        e.printStackTrace();
                    }
                })
        );
    }

    public void fetchOwnedPets(String username) {
        loading.setValue(true);
        disposable.add(dataApiService.getOwnedPets(username)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<ArrayList<LinkedTreeMap<String,String>>>() {
                    @Override
                    public void onSuccess(ArrayList<LinkedTreeMap<String,String>> _pets) {
                        List<Pet> pets = new ArrayList<>();
                        for (LinkedTreeMap<String,String> pet : _pets) {
                            String name = pet.get("petname");
                            String type = pet.get("category");
                            String profile = pet.get("profile");
                            String needs = pet.get("specialreq");
                            Pet currentPet = new Pet(name, type, profile, needs);
                            pets.add(currentPet);
                        }
                        ownedPets.setValue(pets);
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

    public void addPet(String username, String petname, String type, String profile, String requests) {
        loading.setValue(true);
        disposable.add(dataApiService.addNewPetOwnerPet(username, petname, type, profile, requests)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableCompletableObserver() {

                    @Override
                    public void onComplete() {
                        Log.e("addPet", "Success");
                        loading.setValue(false);
                        showError.setValue(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("addPet", "Fail");
                        loading.setValue(false);
                        showError.setValue(true);
                    }
                })
        );
    }

    public void updatePet(String username, String petname, String type, String profile, String requests) {
        loading.setValue(true);
        disposable.add(dataApiService.updatePetOwnerPet(username, petname, type, profile, requests)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableCompletableObserver() {

                    @Override
                    public void onComplete() {
                        Log.e("updatePet", "Success");
                        loading.setValue(false);
                        showError.setValue(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("updatePet", "Fail");
                        loading.setValue(false);
                        showError.setValue(true);
                    }
                })
        );
    }

    public void deletePet(String username, String petname) {
        loading.setValue(true);
        disposable.add(dataApiService.deletePetOwnerPet(username, petname)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableCompletableObserver() {

                    @Override
                    public void onComplete() {
                        Log.e("deletePet", "Success");
                        loading.setValue(false);
                        showError.setValue(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("deletePet", "Fail");
                        loading.setValue(false);
                        showError.setValue(true);
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