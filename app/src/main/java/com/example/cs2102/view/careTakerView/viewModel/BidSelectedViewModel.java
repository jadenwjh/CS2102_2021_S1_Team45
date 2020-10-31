package com.example.cs2102.view.careTakerView.viewModel;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cs2102.model.retrofitApi.DataApiService;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class BidSelectedViewModel extends ViewModel {

    public MutableLiveData<Boolean> loadError = new MutableLiveData<Boolean>();
    public MutableLiveData<Boolean> loading = new MutableLiveData<Boolean>();
    public MutableLiveData<Boolean> acceptedBid = new MutableLiveData<Boolean>();

    private DataApiService dataApiService = DataApiService.getInstance();
    private CompositeDisposable disposable = new CompositeDisposable();

    public void acceptBid(String petOwner, String petName, String careTaker, String avail, String approveReject) {
        Log.e("Accept Bids", "Trying to accept bid");
        acceptedBid.setValue(false);
        loading.setValue(true);
        disposable.add(dataApiService.acceptBid(petOwner, petName, careTaker, avail, approveReject)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        Log.e("Accept Bids", "Success");
                        Log.e("Accept bid", String.format("petowner:%s, petname:%s, caretaker:%s, avail:%s, approveReject:%s", petOwner, petName, careTaker, avail, approveReject));
                        loadError.setValue(false);
                        loading.setValue(false);
                        acceptedBid.setValue(true);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("Accept Bids", "Failed");
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