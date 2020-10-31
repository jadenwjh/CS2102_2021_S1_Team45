package com.example.cs2102.view.registerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

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

    @BindView(R.id.loading)
    ProgressBar loadingBar;

    private FragmentTransaction ft;
    private CareTakerSignUpFragment careTakerFragment;
    private PetOwnerSignUpFragment petOwnerFragment;
    private FragmentManager fm;
    private RegisterViewModel registerViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
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
            //default sign up page
            ft.add(R.id.register_form, petOwnerFragment);
            ft.add(R.id.register_form, careTakerFragment);
            ft.commit();
        }

        registerViewModel.loading.observe(this, aBoolean -> {
            if (aBoolean) {
                loadingBar.setVisibility(View.VISIBLE);
            } else {
                loadingBar.setVisibility(View.GONE);
            }
        });

        registerViewModel.registered.observe(this, aBoolean -> {
            if (aBoolean) {
                Log.e(this.toString(), "Registered so exiting");
                finish();
            }
        });

        petOwner.setOnClickListener(view -> switchFragment(Strings.PET_OWNER_SIGN_UP));

        careTaker.setOnClickListener(view -> switchFragment(Strings.CARE_TAKER_SIGN_UP));
    }

    @Override
    protected void onStart() {
        super.onStart();
        switchFragment(Strings.PET_OWNER_SIGN_UP);
    }

    private void switchFragment(String id) {
        switch (id) {
            case Strings.PET_OWNER_SIGN_UP: {
                Log.e(this.toString(), "show PO");
                ft = fm.beginTransaction();
                ft.show(petOwnerFragment);
                ft.hide(careTakerFragment);
                ft.commit();
                break;
            }
            case Strings.CARE_TAKER_SIGN_UP: {
                Log.e(this.toString(), "show CT");
                ft = fm.beginTransaction();
                ft.show(careTakerFragment);
                ft.hide(petOwnerFragment);
                ft.commit();
                break;
            }
            default:
                throw new RuntimeException("Already at current fragment");
        }
    }
}