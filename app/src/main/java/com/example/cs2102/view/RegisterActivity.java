package com.example.cs2102.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.cs2102.R;
import com.example.cs2102.constants.Strings;
import com.example.cs2102.viewModel.RegisterVM;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RegisterActivity extends AppCompatActivity implements CareTakerSignUpFragment.RegisterCTListener, PetOwnerSignUpFragment.RegisterPOListener {

    @BindView(R.id.pet_owner)
    Button petOwner;

    @BindView(R.id.care_taker)
    Button careTaker;

    @BindView(R.id.loading)
    ProgressBar loadingBar;

    private FragmentManager fm = getSupportFragmentManager();
    private FragmentTransaction ft = fm.beginTransaction();
    private Fragment careTakerFragment = fm.findFragmentByTag("CareTakerSignUp");
    private Fragment petOwnerFragment = fm.findFragmentByTag("PetOwnerSignUp");

    private RegisterVM registerViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        registerViewModel.loading.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    loadingBar.setVisibility(View.VISIBLE);
                } else {
                    loadingBar.setVisibility(View.INVISIBLE);
                }
            }
        });

        registerViewModel = ViewModelProviders.of(this).get(RegisterVM.class);
        switchFragment(1);

        petOwner.setOnClickListener(view -> switchFragment(1));

        careTaker.setOnClickListener(view -> switchFragment(2));
    }

    private void switchFragment(int id) {

        switch (id) {
            case 1: {
                ft.replace(R.id.register_form, new PetOwnerSignUpFragment(), "PetOwnerSignUp");
                if (careTakerFragment != null) {
                    ft.remove(careTakerFragment);
                }
                ft.commit();
                break;
            }
            case 2: {
                ft.replace(R.id.register_form, new CareTakerSignUpFragment(), "CareTakerSignUp");
                if (petOwnerFragment != null) {
                    ft.remove(petOwnerFragment);
                }
                ft.commit();
                break;
            }
            default:
                throw new RuntimeException("Unknown ID");
        }
    }

    @Override
    public void onExitCareTakerRegister() {
        Intent loginPage = new Intent(this, LoginActivity.class);
        startActivity(loginPage);
        finish();
    }

    @Override
    public void onRegisterCareTaker(String username, String password, String email, String number, String address, String contract) {
        registerViewModel.registerCareTaker(username, password, email, number, address, contract);
        registerViewModel.displayRegisterOutcome(this, Strings.CARE_TAKER);
    }

    @Override
    public boolean onCheckCareTakerUsernameTaken(String username) {
        registerViewModel.fetchUsername(username);
        return registerViewModel.displayUsernameTaken(this);
    }

    @Override
    public void onExitPetOwnerRegister() {
        Intent loginPage = new Intent(this, LoginActivity.class);
        startActivity(loginPage);
        finish();
    }

    @Override
    public boolean onCheckPetOwnerUsernameTaken(String username) {
        registerViewModel.fetchUsername(username);
        return registerViewModel.displayUsernameTaken(this);
    }

    @Override
    public void onRegisterPetOwner(String username, String password, String email, String number, String address, String petName, String petType) {
        registerViewModel.registerPetOwner(username, password, email, number, address, petName, petType);
        registerViewModel.displayRegisterOutcome(this, Strings.PET_OWNER);
    }
}