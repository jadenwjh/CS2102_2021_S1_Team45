package com.example.cs2102.viewModel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cs2102.model.DataApiService;
import com.example.cs2102.model.User;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginVM extends ViewModel {

    public MutableLiveData<List<User>> users = new MutableLiveData<List<User>>();
    public MutableLiveData<Boolean> loadError = new MutableLiveData<Boolean>();
    public MutableLiveData<Boolean> loading = new MutableLiveData<Boolean>();
    public MutableLiveData<User> userProfile = new MutableLiveData<User>();

    private DataApiService dataApiService = DataApiService.getInstance();
    private CompositeDisposable disposable = new CompositeDisposable();

    public void fetchUsers() {
        loading.setValue(true);
        disposable.add(dataApiService.getUsers()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<List<User>>() {
                    @Override
                    public void onSuccess(List<User> _users) {
                        users.setValue(_users);
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

    public void loginAttempt(String username, String password, String type) {
        loading.setValue(true);
        dataApiService.verifyLogin(username, password, type).enqueue(
                new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        User user = response.body();
                        userProfile.setValue(user);
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        //Prompt error
                        //Make toast
                    }
                }
        );
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }

}