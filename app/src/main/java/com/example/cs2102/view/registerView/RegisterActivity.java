package com.example.cs2102.view.registerView;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import com.example.cs2102.R;
import com.example.cs2102.model.retrofitApi.Strings;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RegisterActivity extends AppCompatActivity {

    @BindView(R.id.pet_owner)
    Button petOwner;

    @BindView(R.id.care_taker)
    Button careTaker;

    @BindView(R.id.as_both)
    Button both;

    @BindView(R.id.loading)
    ProgressBar loadingBar;

    private FragmentTransaction ft;
    private CareTakerSignUpFragment careTakerFragment;
    private CareTakerSignUpFragment bothFragment;
    private PetOwnerSignUpFragment petOwnerFragment;
    private FragmentManager fm;
    private RegisterViewModel registerViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        petOwner.setBackgroundColor(Color.CYAN);
        careTaker.setBackgroundColor(Color.BLACK);
        both.setBackgroundColor(Color.BLACK);
        registerViewModel = ViewModelProviders.of(this).get(RegisterViewModel.class);
        registerViewModel.loading.setValue(false);
        fm = getSupportFragmentManager();
        ft = fm.beginTransaction();

        if (savedInstanceState == null) {
            careTakerFragment = CareTakerSignUpFragment.newInstance();
            careTakerFragment.setRegisterListener(new CareTakerSignUpFragment.RegisterCTListener() {
                @Override
                public void onExitCareTakerRegister() {
                    finish();
                }

                @Override
                public void onRegisterCareTaker(String username, String email, String password, String profile, String address, int phoneNum, int creditCard, int bankAcc, String acctype, boolean isPT, String admin) {
                    registerViewModel.registerCareTaker(username, email, password, profile, address, phoneNum, creditCard, bankAcc, acctype, isPT, admin);
                }
            });
            petOwnerFragment = PetOwnerSignUpFragment.newInstance();
            petOwnerFragment.setRegisterListener(new PetOwnerSignUpFragment.RegisterPOListener() {
                @Override
                public void onExitPetOwnerRegister() {
                    finish();
                }

                @Override
                public void onRegisterPetOwner(String username, String email, String password, String profile, String address, int phoneNum, int creditCard, int bankAcc, String acctype) {
                    registerViewModel.registerPetOwner(username, email, password, profile, address, phoneNum, creditCard, bankAcc, acctype);
                }
            });
            bothFragment = CareTakerSignUpFragment.newInstance();
            bothFragment.setRegisterListener(new CareTakerSignUpFragment.RegisterCTListener() {
                @Override
                public void onExitCareTakerRegister() {
                    finish();
                }

                @Override
                public void onRegisterCareTaker(String username, String email, String password, String profile, String address, int phoneNum, int creditCard, int bankAcc, String acctype, boolean isPT, String admin) {
                    registerViewModel.registerAsBoth(username, email, password, profile, address, phoneNum, creditCard, bankAcc, isPT, admin);
                }
            });
            //default sign up page
            ft.add(R.id.register_form, petOwnerFragment);
            ft.add(R.id.register_form, careTakerFragment);
            ft.add(R.id.register_form, bothFragment);
            ft.commit();
        }

        petOwner.setOnClickListener(view -> switchFragment(Strings.PET_OWNER_SIGN_UP));
        careTaker.setOnClickListener(view -> switchFragment(Strings.CARE_TAKER_SIGN_UP));
        both.setOnClickListener(view -> switchFragment(Strings.BOTH_SIGN_UP));

        registerObserver();
    }

    @Override
    protected void onStart() {
        super.onStart();
        switchFragment(Strings.PET_OWNER_SIGN_UP);
    }

    private void registerObserver() {
        registerViewModel.loading.observe(this, aBoolean -> {
            if (aBoolean) {
                loadingBar.setVisibility(View.VISIBLE);
            } else {
                loadingBar.setVisibility(View.GONE);
            }
        });
        registerViewModel.registered.observe(this, aBoolean -> {
            if (aBoolean) {
                Toast.makeText(this, "You have successfully registered", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        registerViewModel.usernameTaken.observe(this, taken -> {
            if (taken) {
                Toast.makeText(this, "This username is taken", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void switchFragment(String id) {
        switch (id) {
            case Strings.PET_OWNER_SIGN_UP: {
                petOwner.setBackgroundColor(Color.CYAN);
                careTaker.setBackgroundColor(Color.BLACK);
                both.setBackgroundColor(Color.BLACK);
                ft = fm.beginTransaction();
                ft.show(petOwnerFragment);
                ft.hide(careTakerFragment);
                ft.hide(bothFragment);
                ft.commit();
                break;
            }
            case Strings.CARE_TAKER_SIGN_UP: {
                careTaker.setBackgroundColor(Color.CYAN);
                petOwner.setBackgroundColor(Color.BLACK);
                both.setBackgroundColor(Color.BLACK);
                ft = fm.beginTransaction();
                ft.show(careTakerFragment);
                ft.hide(petOwnerFragment);
                ft.hide(bothFragment);
                ft.commit();
                break;
            }
            case Strings.BOTH_SIGN_UP: {
                both.setBackgroundColor(Color.CYAN);
                petOwner.setBackgroundColor(Color.BLACK);
                careTaker.setBackgroundColor(Color.BLACK);
                ft = fm.beginTransaction();
                ft.show(bothFragment);
                ft.hide(petOwnerFragment);
                ft.hide(careTakerFragment);
                ft.commit();
                break;
            }
            default:
                throw new RuntimeException("Already at current fragment");
        }
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            super.onBackPressed();
            return true;
        }

        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        Activity activity = this;
        if (activity.getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }
}