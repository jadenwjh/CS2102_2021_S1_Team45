package com.example.cs2102.view.adminView.viewModel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cs2102.model.AdminStats;
import com.example.cs2102.model.Salary;
import com.example.cs2102.model.retrofitApi.DataApiService;
import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class AdminSalaryViewModel extends ViewModel {

    public MutableLiveData<List<Salary>> salarys = new MutableLiveData<>();
    public MutableLiveData<AdminStats> stats = new MutableLiveData<>();
    public MutableLiveData<Boolean> loading = new MutableLiveData<>();
    public MutableLiveData<Boolean> fetchedData = new MutableLiveData<>();
    public MutableLiveData<Boolean> nothing = new MutableLiveData<>();

    private DataApiService dataApiService = DataApiService.getInstance();
    private CompositeDisposable disposable = new CompositeDisposable();

    public void fetchSalary(String username, String date) {
        loading.setValue(true);
        fetchedData.setValue(false);
        nothing.setValue(false);
        disposable.add(dataApiService.fetchSalarys(username, date)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<ArrayList<LinkedTreeMap<String,String>>>() {

                    @Override
                    public void onSuccess(@NonNull ArrayList<LinkedTreeMap<String, String>> linkedTreeMaps) {
                        List<Salary> list = new ArrayList<>();
                        for (LinkedTreeMap<String, String> row : linkedTreeMaps) {
                            String name = row.get("caretaker");
                            String amount = row.get("ptsalary");
                            Salary current = new Salary(name, amount);
                            list.add(current);
                        }
                        salarys.setValue(list);
                        fetchStats(username, date);
                    }

                    @Override
                    public void onError(Throwable e) {
                        loading.setValue(false);
                        nothing.setValue(true);
                    }
                })
        );
    }

    private void fetchStats(String username, String date) {
        disposable.add(dataApiService.fetchAdminStats(username, date)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<LinkedTreeMap<String,String>>() {

                    @Override
                    public void onSuccess(@NonNull LinkedTreeMap<String, String> map) {
                        AdminStats adminStats = new AdminStats(map.get("totalpets"), map.get("petdays"));
                        stats.setValue(adminStats);
                        loading.setValue(false);
                        fetchedData.setValue(true);
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