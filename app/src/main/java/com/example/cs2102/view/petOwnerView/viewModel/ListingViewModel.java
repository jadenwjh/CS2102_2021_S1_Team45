package com.example.cs2102.view.petOwnerView.viewModel;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cs2102.model.Review;
import com.example.cs2102.model.retrofitApi.DataApiService;
import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class ListingViewModel extends ViewModel {

    public MutableLiveData<Boolean> loading = new MutableLiveData<>();
    public MutableLiveData<Boolean> bidSubmitted = new MutableLiveData<>();
    public MutableLiveData<List<Review>> reviews = new MutableLiveData<>();

    private DataApiService dataApiService = DataApiService.getInstance();
    private CompositeDisposable disposable = new CompositeDisposable();

    public void submitBid(String username, String petName, String careTaker, String sdate, String edate, String payment, float price, String transfer, Context context) {
        bidSubmitted.setValue(false);
        loading.setValue(true);
        disposable.add(dataApiService.submitPObid(username, petName, careTaker, sdate, edate, payment, price, transfer)
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

    public void fetchReviews(String caretaker) {
        disposable.add(dataApiService.getCareTakerReviews(caretaker)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<ArrayList<LinkedTreeMap<String,String>>>() {

                    @Override
                    public void onSuccess(@NonNull ArrayList<LinkedTreeMap<String, String>> _reviews) {
                        List<Review> list = new ArrayList<>();
                        for (LinkedTreeMap<String,String> r : _reviews) {
                            String rating = r.get("rating");
                            String review = r.get("review");
                            String petowner = r.get("petowner");
                            String date = r.get("edate").substring(0,10);
                            String p = r.get("petname");
                            Review newReview = new Review(petowner,rating, review, date, p);
                            list.add(newReview);
                        }
                        reviews.setValue(list);
                        Log.e("fetchReviews", "Success");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("fetchReviews", "Failed");
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