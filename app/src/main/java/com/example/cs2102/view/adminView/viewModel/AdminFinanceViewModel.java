package com.example.cs2102.view.adminView.viewModel;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cs2102.model.Finance;
import com.example.cs2102.model.retrofitApi.DataApiService;
import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class AdminFinanceViewModel extends ViewModel {

    public MutableLiveData<List<Finance>> finances = new MutableLiveData<>();
    public MutableLiveData<Boolean> loading = new MutableLiveData<>();

    private DataApiService dataApiService = DataApiService.getInstance();
    private CompositeDisposable disposable = new CompositeDisposable();

    public void fetchFinance(String sdate, String edate) {
        Log.e("fetching finances", String.format("Start: %s End: %s", sdate, edate));
        loading.setValue(true);
        disposable.add(dataApiService.fetchFinances(sdate, edate)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<ArrayList<LinkedTreeMap<String,String>>>() {

                    @Override
                    public void onSuccess(@NonNull ArrayList<LinkedTreeMap<String, String>> linkedTreeMaps) {
                        List<Finance> list = new ArrayList<>();
                        for (LinkedTreeMap<String, String> row : linkedTreeMaps) {
                            String year = row.get("year_");
                            String month = row.get("month_");
                            String profit = row.get("profit");
                            String revenue = row.get("totalrevenue");
                            String salary = row.get("totalsalarypaid");
                            String pets = row.get("totalpets");
                            Finance current = new Finance(year, month, profit, revenue, salary, pets);
                            list.add(current);
                        }
                        finances.setValue(list);
                        loading.setValue(false);
                    }

                    @Override
                    public void onError(Throwable e) {
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