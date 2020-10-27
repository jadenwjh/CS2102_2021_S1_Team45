package com.example.cs2102.viewModel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cs2102.model.CareTaker;
import com.example.cs2102.model.DataApiService;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class CareTakerAvailableVM extends ViewModel {
    public MutableLiveData<List<CareTaker>> careTakers = new MutableLiveData<>();
    public MutableLiveData<Boolean> loadError = new MutableLiveData<>();
    public MutableLiveData<Boolean> loading = new MutableLiveData<>();

    private DataApiService dataApiService = DataApiService.getInstance();
    private CompositeDisposable disposable = new CompositeDisposable();

    public void refreshPage() {fetchCareTakers();}

    public void fetchCareTakers() {
        loading.setValue(true);
        disposable.add(dataApiService.getCareTakers()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<List<CareTaker>>() {
                    @Override
                    public void onSuccess(List<CareTaker> _CareTakers) {
                        careTakers.setValue(_CareTakers);
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
