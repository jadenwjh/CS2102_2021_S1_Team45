package com.example.cs2102.view.registerView;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cs2102.model.CareTaker;
import com.example.cs2102.model.PetOwner;
import com.example.cs2102.model.retrofitApi.DataApiService;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class RegisterViewModel extends ViewModel {

    public MutableLiveData<Boolean> registered = new MutableLiveData<Boolean>();
    public MutableLiveData<Boolean> loadError = new MutableLiveData<Boolean>();
    public MutableLiveData<Boolean> loading = new MutableLiveData<Boolean>();

    private DataApiService dataApiService = DataApiService.getInstance();
    private CompositeDisposable disposable = new CompositeDisposable();

    public void registerPetOwner(String username, String email, String password, String profile, String address, int phoneNum, int creditCard, int bankAcc, String acctype) {
        Log.e(this.toString(), String.format("Username:%s, Email:%s, Password:%s, Profile:%s, Address:%s, phoneNum:%s, credit:%d, bank:%d, acctype:%s", username,email,password,profile,address,phoneNum,creditCard,bankAcc,acctype));
        loading.setValue(true);
        registered.setValue(false);
        Log.e(this.toString(), "attempting to register PO");
        disposable.add(dataApiService.addPetOwner(username, email, password, profile, address, phoneNum, creditCard, bankAcc, acctype)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        Log.e(this.toString(), "success");
                        registered.setValue(true);
                        loadError.setValue(false);
                        loading.setValue(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("RequestError", String.valueOf(e));
                        Log.e(this.toString(), "failed");
                        registered.setValue(false);
                        loadError.setValue(true);
                        loading.setValue(false);
                    }
                })
        );
    }

    public void registerCareTaker(String username, String email, String password, String profile, String address, int phoneNum, int creditCard, int bankAcc, String acctype, boolean isPT, String admin) {
        Log.e(this.toString(), String.format("Username:%s, Email:%s, Password:%s, Profile:%s, Address:%s, phoneNum:%s, credit:%d, bank:%d, acctype:%s, FullTime:%s, Admin:%s", username,email,password,profile,address,phoneNum,creditCard,bankAcc,acctype,isPT,admin));
        loading.setValue(true);
        registered.setValue(false);
        Log.e(this.toString(), "attempting to register CT");
        disposable.add(dataApiService.addCareTaker(username, email, password, profile, address, phoneNum, creditCard, bankAcc, acctype, isPT, admin)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        Log.e(this.toString(), "success");
                        registered.setValue(true);
                        loadError.setValue(false);
                        loading.setValue(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("RequestError", String.valueOf(e));
                        Log.e(this.toString(), "failed");
                        registered.setValue(false);
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
