package com.example.cs2102.view.petOwnerView.viewModel;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cs2102.model.CareTakerBid;
import com.example.cs2102.model.retrofitApi.DataApiService;
import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

// TODO fetching ongoing bids has a bug at backend, havnt test fetchAcceptedBids

public class PetOwnerBidsViewModel extends ViewModel {

    public MutableLiveData<ArrayList<CareTakerBid>> ongoingBids = new MutableLiveData<>();
    public MutableLiveData<ArrayList<CareTakerBid>> expiredBids = new MutableLiveData<>();
    public MutableLiveData<Boolean> loading = new MutableLiveData<>();

    private DataApiService dataApiService = DataApiService.getInstance();
    private CompositeDisposable disposable = new CompositeDisposable();

    public void fetchOngoingBids(String username) {
        loading.setValue(true);
        disposable.add(dataApiService.getOngoingBids(username)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<ArrayList<LinkedTreeMap<String,String>>>() {
                    @Override
                    public void onSuccess(ArrayList<LinkedTreeMap<String,String>> _ongoingBids) {
                        ArrayList<CareTakerBid> careTakerBids = new ArrayList<>();
                        for (LinkedTreeMap<String,String> bid : _ongoingBids) {
                            String petOwner = bid.get("petowner");
                            String petName = bid.get("petname");
                            String careTaker = bid.get("caretaker");
                            String startDate = bid.get("avail");
                            String endDate = bid.get("edate");
                            String transfer = bid.get("transfertype");
                            String payment = bid.get("paymenttype");
                            String price = bid.get("price");
                            String isPaid = bid.get("ispaid");
                            String status = bid.get("status");
                            String review = bid.get("review");
                            String rating = bid.get("rating");
                            CareTakerBid ongoing = new CareTakerBid(petOwner, petName, careTaker, startDate, endDate, transfer, payment, price, isPaid, status, review, rating);
                            careTakerBids.add(ongoing);
                        }
                        ongoingBids.setValue(careTakerBids);
                        loading.setValue(false);
                        Log.e("fetchOngoingBids", "Success");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("fetchOngoingBids", "Failed");
                        Log.e("fetchOngoingBids", e.getMessage());
                        loading.setValue(false);
                    }
                })
        );
    }

    public void fetchExpiredBids(String username) {
        loading.setValue(true);
        disposable.add(dataApiService.getExpiredBids(username)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<ArrayList<LinkedTreeMap<String,String>>>() {
                    @Override
                    public void onSuccess(ArrayList<LinkedTreeMap<String,String>> _expiredBids) {
                        ArrayList<CareTakerBid> careTakerBids = new ArrayList<>();
                        for (LinkedTreeMap<String,String> bid : _expiredBids) {
                            String petOwner = bid.get("petowner");
                            String petName = bid.get("petname");
                            String careTaker = bid.get("caretaker");
                            String startDate = bid.get("avail");
                            String endDate = bid.get("edate");
                            String transfer = bid.get("transfertype");
                            String payment = bid.get("paymenttype");
                            String price = bid.get("price");
                            String isPaid = bid.get("ispaid");
                            String status = bid.get("status");
                            String review = bid.get("review");
                            String rating = bid.get("rating");
                            CareTakerBid expired = new CareTakerBid(petOwner, petName, careTaker, startDate, endDate, transfer, payment, price, isPaid, status, review, rating);
                            careTakerBids.add(expired);
                        }
                        expiredBids.setValue(careTakerBids);
                        loading.setValue(false);
                        Log.e("fetchExpiredBids", "Success");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("fetchExpiredBids", "Failed");
                        Log.e("fetchExpiredBids", e.getMessage());
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