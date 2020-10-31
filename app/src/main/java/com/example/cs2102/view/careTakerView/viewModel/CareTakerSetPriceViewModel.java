package com.example.cs2102.view.careTakerView.viewModel;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cs2102.model.PetTypeCost;
import com.example.cs2102.model.retrofitApi.DataApiService;
import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class CareTakerSetPriceViewModel extends ViewModel {

    public MutableLiveData<Boolean> loadError = new MutableLiveData<Boolean>();
    public MutableLiveData<Boolean> loading = new MutableLiveData<Boolean>();
    public MutableLiveData<List<PetTypeCost>> petTypeCosts = new MutableLiveData<>();

    public MutableLiveData<PetTypeCost> selectedPetTypeCost = new MutableLiveData<>();

    private DataApiService dataApiService = DataApiService.getInstance();
    private CompositeDisposable disposable = new CompositeDisposable();

    public void refreshPrices(String careTakerUsername) {
        fetchPetTypeCosts(careTakerUsername);
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
                        for (LinkedTreeMap<String,String> type : _petTypeCosts) {
                            PetTypeCost item = new PetTypeCost(careTakerUsername, type.get("category"), type.get("feeperday"));
                            list.add(item);
                        }
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

    //method setPrice

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }
}
