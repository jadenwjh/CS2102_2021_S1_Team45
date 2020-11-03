package com.example.cs2102.view.deleteView;

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

public class DeleteViewModel extends ViewModel {

    public MutableLiveData<Boolean> loadError = new MutableLiveData<>();
    public MutableLiveData<Boolean> loading = new MutableLiveData<>();
    public MutableLiveData<Boolean> deleteSuccess = new MutableLiveData<>();

    private DataApiService dataApiService = DataApiService.getInstance();
    private CompositeDisposable disposable = new CompositeDisposable();

    public void deleteUser(String username, String password) {
        deleteSuccess.setValue(false);
        loading.setValue(true);
        disposable.add(dataApiService.deleteUser(username, password)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableCompletableObserver() {

                    @Override
                    public void onComplete() {
                        loadError.setValue(false);
                        loading.setValue(false);
                        deleteSuccess.setValue(true);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("Delete", "Failed");
                        Log.e("Delete", String.valueOf(e));
                        loadError.setValue(true);
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
