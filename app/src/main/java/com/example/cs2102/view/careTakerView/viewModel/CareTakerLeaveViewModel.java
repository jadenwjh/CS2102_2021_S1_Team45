package com.example.cs2102.view.careTakerView.viewModel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cs2102.model.PetTypeCost;
import com.example.cs2102.model.retrofitApi.DataApiService;

import java.sql.Date;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class CareTakerLeaveViewModel extends ViewModel {

    public MutableLiveData<Boolean> loadError = new MutableLiveData<>();
    public MutableLiveData<Boolean> loading = new MutableLiveData<>();
    public MutableLiveData<List<String>> availableDates = new MutableLiveData<>();

    public MutableLiveData<String> selectedDate = new MutableLiveData<>();

    private DataApiService dataApiService = DataApiService.getInstance();
    private CompositeDisposable disposable = new CompositeDisposable();

    public void refresh(String uName) {getWorkingDays(uName);}

    public void getWorkingDays(String username) {
        loading.setValue(true);
        disposable.add(dataApiService.careTakerFullTimeFree(username)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<List<String>>() {

                    @Override
                    public void onSuccess(List<String> dates) {
                        availableDates.setValue(dates);
                        loadError.setValue(false);
                        loading.setValue(false);
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

    public void requestToApplyLeave(String careTakerUsername, String date) {
        loading.setValue(true);
        disposable.add(dataApiService.applyLeave(careTakerUsername, date)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<String>() {

                    @Override
                    public void onSuccess(String date) {
                        loadError.setValue(false);
                        loading.setValue(false);
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