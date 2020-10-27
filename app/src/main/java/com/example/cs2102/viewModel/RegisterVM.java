package com.example.cs2102.viewModel;

import android.content.Context;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cs2102.model.CareTaker;
import com.example.cs2102.model.DataApiService;
import com.example.cs2102.model.PetOwner;
import com.example.cs2102.model.User;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class RegisterVM extends ViewModel {

    public MutableLiveData<Boolean> isUsernameTaken = new MutableLiveData<Boolean>();
    public MutableLiveData<Boolean> registered = new MutableLiveData<Boolean>();
    public MutableLiveData<Boolean> loadError = new MutableLiveData<Boolean>();
    public MutableLiveData<Boolean> loading = new MutableLiveData<Boolean>();

    private DataApiService dataApiService = DataApiService.getInstance();
    private CompositeDisposable disposable = new CompositeDisposable();

    public void fetchUsername(String qUsername) {
        loading.setValue(true);
        isUsernameTaken.setValue(false);
        disposable.add(dataApiService.getUsername(qUsername)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<User>() {
                    @Override
                    public void onSuccess(User user) {
                        isUsernameTaken.setValue(true);
                        loadError.setValue(false);
                        loading.setValue(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        isUsernameTaken.setValue(false);
                        loadError.setValue(true);
                        loading.setValue(false);
                    }
                })
        );
    }

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

    public boolean getRegisterOutcome() {
        return registered.getValue() != null ? registered.getValue() : false;
    }

    public void displayRegisterOutcome(Context context, String type) {
        if (getRegisterOutcome()) {
            Toast.makeText(context, String.format("Successfully registered as %s", type), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Failed to register", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean getIsUsernameTaken() {
        return isUsernameTaken.getValue() != null ? isUsernameTaken.getValue() : true;
    }

    public boolean displayUsernameTaken(Context context) {
        boolean outcome = getIsUsernameTaken();
        if (outcome) {
            Toast.makeText(context, "Username is taken", Toast.LENGTH_SHORT).show();
        }
        return outcome;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }
}
