package com.example.cs2102.view.registerView;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cs2102.model.CareTaker;
import com.example.cs2102.model.PetOwner;
import com.example.cs2102.model.retrofitApi.DataApiService;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class RegisterViewModel extends ViewModel {

    public MutableLiveData<Boolean> registered = new MutableLiveData<Boolean>();
    public MutableLiveData<Boolean> loadError = new MutableLiveData<Boolean>();
    public MutableLiveData<Boolean> loading = new MutableLiveData<Boolean>();

    private DataApiService dataApiService = DataApiService.getInstance();
    private CompositeDisposable disposable = new CompositeDisposable();

    public void registerPetOwner (String username, String password, String email, String number, String address, String petName, String petType) {
        loading.setValue(true);
        registered.setValue(false);
        disposable.add(dataApiService.addPetOwner(username, password, email, number, address, petName, petType)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<PetOwner>() {
                    @Override
                    public void onSuccess(PetOwner petOwner) {
                        registered.setValue(true);
                        loadError.setValue(false);
                        loading.setValue(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        registered.setValue(false);
                        loadError.setValue(true);
                        loading.setValue(false);
                    }
                })
        );
    }

    public void registerCareTaker (String username, String password, String email, String number, String address, String contract) {
        loading.setValue(true);
        registered.setValue(false);
        disposable.add(dataApiService.addCareTaker(username, password, email, number, address, contract)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<CareTaker>() {
                    @Override
                    public void onSuccess(CareTaker careTaker) {
                        registered.setValue(true);
                        loadError.setValue(false);
                        loading.setValue(false);
                    }

                    @Override
                    public void onError(Throwable e) {
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
