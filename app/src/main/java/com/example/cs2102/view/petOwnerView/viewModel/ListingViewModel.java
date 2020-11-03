package com.example.cs2102.view.petOwnerView.viewModel;

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

public class ListingViewModel extends ViewModel {

    public MutableLiveData<Boolean> loading = new MutableLiveData<>();
    public MutableLiveData<Boolean> bidSubmitted = new MutableLiveData<>();

    private DataApiService dataApiService = DataApiService.getInstance();
    private CompositeDisposable disposable = new CompositeDisposable();

    public void submitBid(String username, String petName, String careTaker, String sdate, String edate, String payment, float price, Context context) {
        bidSubmitted.setValue(false);
        loading.setValue(true);
        disposable.add(dataApiService.submitPObid(username, petName, careTaker, sdate, edate, payment, price)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        Log.e("submitBid", "Success");
                        Toast.makeText(context, "You have successfully made this bid", Toast.LENGTH_SHORT).show();
                        loading.setValue(false);
                        bidSubmitted.setValue(true);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("submitBid", "Failed");
                        Toast.makeText(context, "This pet is served by a CT during this date range", Toast.LENGTH_SHORT).show();
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