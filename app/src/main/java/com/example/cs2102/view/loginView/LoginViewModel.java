package com.example.cs2102.view.loginView;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cs2102.model.retrofitApi.DataApiService;
import com.example.cs2102.model.User;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class LoginViewModel extends ViewModel {

    public MutableLiveData<Boolean> loadError = new MutableLiveData<Boolean>();
    public MutableLiveData<Boolean> loading = new MutableLiveData<Boolean>();
    public MutableLiveData<String> userName = new MutableLiveData<>();
    public MutableLiveData<String> userType = new MutableLiveData<>();
    public MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();

    private DataApiService dataApiService = DataApiService.getInstance();
    private CompositeDisposable disposable = new CompositeDisposable();

    public void loginAttempt(String username, String password, String type) {
        Log.e("Login", "Attempt");
        loginSuccess.setValue(false);
        loading.setValue(true);
        userName.setValue(username);
        userType.setValue(type);
        disposable.add(dataApiService.verifyLogin(username, password, type)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        Log.e("Login", "Success");
                        loadError.setValue(false);
                        loading.setValue(false);
                        loginSuccess.setValue(true);
                    }

                    @Override
                    public void onError(Throwable e) {
                        loadError.setValue(true);
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
