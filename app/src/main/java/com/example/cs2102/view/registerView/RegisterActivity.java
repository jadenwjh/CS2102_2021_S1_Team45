package com.example.cs2102.view.registerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import com.example.cs2102.R;
import com.example.cs2102.widgets.Strings;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RegisterActivity extends AppCompatActivity implements CareTakerSignUpFragment.RegisterCTListener, PetOwnerSignUpFragment.RegisterPOListener {

    @BindView(R.id.pet_owner)
    Button petOwner;

    @BindView(R.id.care_taker)
    Button careTaker;

    @BindView(R.id.loading)
    ProgressBar loadingBar;

    private FragmentTransaction ft;
    private CareTakerSignUpFragment careTakerFragment;
    private PetOwnerSignUpFragment petOwnerFragment;

    private RegisterViewModel registerViewModel;
    private static final String CURRENT_SIGN_UP = "RegisterSignUpFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        registerViewModel = ViewModelProviders.of(this).get(RegisterViewModel.class);
        FragmentManager fm = getSupportFragmentManager();

        if (savedInstanceState == null) {
            ft = fm.beginTransaction();
            careTakerFragment = CareTakerSignUpFragment.newInstance();
            petOwnerFragment = PetOwnerSignUpFragment.newInstance();
            //default sign up page
            ft.add(R.id.register_form, petOwnerFragment, CURRENT_SIGN_UP).commit();
        }

        ButterKnife.bind(this);

        registerViewModel.loading.observe(this, aBoolean -> {
            if (aBoolean) {
                loadingBar.setVisibility(View.VISIBLE);
            } else {
                loadingBar.setVisibility(View.GONE);
            }
        });

        registerViewModel.registered.observe(this, aBoolean -> {
            if (aBoolean) {
                finish();
            }
        });

        petOwner.setOnClickListener(view -> switchFragment(Strings.PET_OWNER_SIGN_UP));

        careTaker.setOnClickListener(view -> switchFragment(Strings.CARE_TAKER_SIGN_UP));
    }

    private void switchFragment(String id) {
        switch (id) {
            case Strings.PET_OWNER_SIGN_UP: {
                ft.replace(R.id.register_form, petOwnerFragment, CURRENT_SIGN_UP).commit();
                break;
            }
            case Strings.CARE_TAKER_SIGN_UP: {
                ft.replace(R.id.register_form, careTakerFragment, CURRENT_SIGN_UP).commit();
                break;
            }
            default:
                throw new RuntimeException("Unable to load sign up fragment");
        }
    }

    @Override
    public void onRegisterPetOwner(String username, String password, String email, String number, String address, String petName, String petType) {
        registerViewModel.registerPetOwner(username, password, email, number, address, petName, petType);
    }

    @Override
    public void onRegisterCareTaker(String username, String password, String email, String number, String address, String contract) {
        registerViewModel.registerCareTaker(username, password, email, number, address, contract);
    }

    @Override
    public void onExitPetOwnerRegister() {
        finish();
    }

    @Override
    public void onExitCareTakerRegister() {
        finish();
    }
}