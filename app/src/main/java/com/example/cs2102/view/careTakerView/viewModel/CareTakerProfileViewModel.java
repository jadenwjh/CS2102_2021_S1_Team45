package com.example.cs2102.view.careTakerView.viewModel;

import android.util.Log;

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
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class CareTakerProfileViewModel extends ViewModel {
    public MutableLiveData<Boolean> loadingStats = new MutableLiveData<>();
    public MutableLiveData<List<Review>> reviews = new MutableLiveData<>();
    public MutableLiveData<LinkedTreeMap<String,Object>> stats = new MutableLiveData<>();

    private DataApiService dataApiService = DataApiService.getInstance();
    private CompositeDisposable disposable = new CompositeDisposable();

    public void fetchData(String username, String date) {
        fetchStats(username, date);
        fetchReviews(username);
    }

    private void fetchStats(String username, String date) {
        loadingStats.setValue(true);
        disposable.add(dataApiService.fetchCTStats(username, date)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<LinkedTreeMap<String,Object>>() {

                    @Override
                    public void onSuccess(@NonNull LinkedTreeMap<String, Object> data) {
                        stats.setValue(data);
                        loadingStats.setValue(false);
                        Log.e("fetchStats", data.toString());
                    }

                    @Override
                    public void onError(Throwable e) {
                        loadingStats.setValue(false);
                    }
                })
        );
    }

    private void fetchReviews(String caretaker) {
        loadingStats.setValue(true);
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
                        loadingStats.setValue(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        loadingStats.setValue(false);
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