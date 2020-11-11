package com.example.cs2102.view.adminView.viewModel;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cs2102.model.AdminStats;
import com.example.cs2102.model.CaretakerInfo;
import com.example.cs2102.model.retrofitApi.DataApiService;
import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class AdminSalaryViewModel extends ViewModel {

    public MutableLiveData<List<CaretakerInfo>> caretakerInfos = new MutableLiveData<>();
    public MutableLiveData<Boolean> loading = new MutableLiveData<>();
    public MutableLiveData<Boolean> nothing = new MutableLiveData<>();

    private DataApiService dataApiService = DataApiService.getInstance();
    private CompositeDisposable disposable = new CompositeDisposable();

    public void fetchCTInfo(String username, String date) {
        loading.setValue(true);
        nothing.setValue(false);
        disposable.add(dataApiService.fetchCTInfo(username, date)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<ArrayList<LinkedTreeMap<String,String>>>() {

                    @Override
                    public void onSuccess(@NonNull ArrayList<LinkedTreeMap<String, String>> linkedTreeMaps) {
                        List<CaretakerInfo> list = new ArrayList<>();
                        for (LinkedTreeMap<String, String> row : linkedTreeMaps) {
                            String name = row.get("caretaker");
                            String contract = row.get("contract");
                            String amount = row.get("salary");
                            String days = row.get("petdaysclocked");
                            String rating = row.get("avgrating") == null ? "0" : row.get("avgrating");
                            String freq = row.get("numratings");
                            CaretakerInfo current = new CaretakerInfo(name, contract, amount, days, rating, freq);
                            list.add(current);
                        }
                        caretakerInfos.setValue(list);
                        loading.setValue(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        loading.setValue(false);
                        nothing.setValue(true);
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