package com.example.cs2102.view.careTakerView.viewModel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cs2102.model.retrofitApi.DataApiService;
import com.google.gson.internal.LinkedTreeMap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class CareTakerSalaryViewModel extends ViewModel {

    public MutableLiveData<Boolean> loading = new MutableLiveData<>();
    public MutableLiveData<String> salary = new MutableLiveData<>();
    public MutableLiveData<Boolean> nothing = new MutableLiveData<>();

    private DataApiService dataApiService = DataApiService.getInstance();
    private CompositeDisposable disposable = new CompositeDisposable();

    public void fetchSalary(String username, String date) {
        loading.setValue(true);
        nothing.setValue(false);
        disposable.add(dataApiService.fetchCTSalary(username, date)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<LinkedTreeMap<String,String>>() {

                    @Override
                    public void onSuccess(@NonNull LinkedTreeMap<String, String> map) {
                        String value = map.get("salary");
                        salary.setValue(value);
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