package com.example.cs2102.view.registerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
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

    private FragmentTransaction ft;
    private CareTakerSignUpFragment careTakerFragment;
    private PetOwnerSignUpFragment petOwnerFragment;

    private RegisterVM registerViewModel;
    private static final String CURRENT_SIGN_UP = "RegisterSignUpFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        FragmentManager fm = getSupportFragmentManager();

        if (savedInstanceState == null) {
            ft = fm.beginTransaction();
            careTakerFragment = new CareTakerSignUpFragment();
            petOwnerFragment = new PetOwnerSignUpFragment();
            //default sign up page
            ft.add(R.id.register_form, petOwnerFragment, CURRENT_SIGN_UP).commit();
        }

        ButterKnife.bind(this);

        registerViewModel.loading.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    loadingBar.setVisibility(View.VISIBLE);
                } else {
                    loadingBar.setVisibility(View.GONE);
                }
            }
        });

        registerViewModel = ViewModelProviders.of(this).get(RegisterVM.class);

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
    public void onExitCareTakerRegister() {
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