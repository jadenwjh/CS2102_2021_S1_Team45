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

public class ReviewViewModel extends ViewModel {

    public MutableLiveData<Boolean> loading = new MutableLiveData<>();
    public MutableLiveData<Boolean> reviewSubmitted = new MutableLiveData<>();

    private DataApiService dataApiService = DataApiService.getInstance();
    private CompositeDisposable disposable = new CompositeDisposable();

    public void submitReview(String petOwner, String petName, String careTaker, String avail, int rating, String review, Context context, boolean isPaid) {
        reviewSubmitted.setValue(false);
        loading.setValue(true);
        Log.e("Review send values", petOwner);
        Log.e("Review send values", petName);
        Log.e("Review send values", careTaker);
        Log.e("Review send values", avail);
        Log.e("Review send values", Integer.toString(rating));
        Log.e("Review send values", review);
        Log.e("Review send values", isPaid ? "true" : "false");
        disposable.add(dataApiService.leaveReview(petOwner, petName, careTaker, avail, rating, review, isPaid)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        Log.e("submitReview", "Success");
                        Toast.makeText(context, "You have successfully left a review", Toast.LENGTH_SHORT).show();
                        loading.setValue(false);
                        reviewSubmitted.setValue(true);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("submitReview", "Failed");
                        Toast.makeText(context, "Check that you have filled in all parameters", Toast.LENGTH_SHORT).show();
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