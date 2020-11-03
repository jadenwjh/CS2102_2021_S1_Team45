package com.example.cs2102.view.careTakerView.viewModel;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cs2102.model.retrofitApi.DataApiService;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.schedulers.Schedulers;

public class CareTakerHomepageViewModel extends ViewModel {

    public MutableLiveData<Boolean> loadErrorPT = new MutableLiveData<>();
    public MutableLiveData<Boolean> loadErrorFT = new MutableLiveData<>();
    public MutableLiveData<Boolean> loading = new MutableLiveData<>();

    private DataApiService dataApiService = DataApiService.getInstance();
    private CompositeDisposable disposable = new CompositeDisposable();

    public void requestToSendAvailability(String careTakerUsername, String date, Context context) {
        loading.setValue(true);
        disposable.add(dataApiService.setPartTimerFree(careTakerUsername, date)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableCompletableObserver() {

                    @Override
                    public void onComplete() {
                        Log.e("Set availability", "Success");
                        Log.e("sent avail for PT", date);
                        Toast.makeText(context, "Availability successfully set on " + date, Toast.LENGTH_SHORT).show();
                        loadErrorPT.setValue(false);
                        loading.setValue(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("sent avail for PT", "Failed");
                        Toast.makeText(context, "Availability failed to set, you might already be available at this date", Toast.LENGTH_SHORT).show();
                        loadErrorPT.setValue(true);
                        loading.setValue(false);
                    }
                })
        );
    }

    public void requestToApplyLeave(String careTakerUsername, String date, Context context) {
        loading.setValue(true);
        disposable.add(dataApiService.applyLeave(careTakerUsername, date)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableCompletableObserver() {

                    @Override
                    public void onComplete() {
                        Log.e("Apply leave", "Success");
                        Log.e("sent leave for FT", date);
                        Toast.makeText(context, "Leave successfully set on " + date, Toast.LENGTH_SHORT).show();
                        loadErrorFT.setValue(false);
                        loading.setValue(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("sent avail for FT", "Failed");
                        Toast.makeText(context, "Leave failed to set, you might already be on leave at this date", Toast.LENGTH_SHORT).show();
                        loadErrorFT.setValue(true);
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
