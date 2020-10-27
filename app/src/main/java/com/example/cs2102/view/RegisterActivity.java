package com.example.cs2102.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.cs2102.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RegisterActivity extends AppCompatActivity implements CareTakerSignUpFragment.RegisterCTListener, PetOwnerSignUpFragment.RegisterPOListener {

    @BindView(R.id.pet_owner)
    Button petOwner;

    @BindView(R.id.care_taker)
    Button careTaker;

    private FragmentManager fm = getSupportFragmentManager();
    private FragmentTransaction ft = fm.beginTransaction();
    private Fragment careTakerFragment = fm.findFragmentByTag("CareTakerSignUp");
    private Fragment petOwnerFragment = fm.findFragmentByTag("PetOwnerSignUp");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

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
        Toast.makeText(this, "registered as CT", Toast.LENGTH_SHORT).show();
        //Register
        //VM methods
    }

    @Override
    public boolean onCheckCareTakerUsernameTaken(String username) {
        Toast.makeText(this, "Username is taken", Toast.LENGTH_SHORT).show();
        //VM methods
        return true;
    }

    @Override
    public void onExitPetOwnerRegister() {
        Intent loginPage = new Intent(this, LoginActivity.class);
        startActivity(loginPage);
        finish();
    }

    @Override
    public boolean onCheckPetOwnerUsernameTaken(String username) {
        Toast.makeText(this, "Username is taken", Toast.LENGTH_SHORT).show();
        //VM methods
        return false;
    }

    @Override
    public void onRegisterPetOwner(String username, String password, String email, String number, String address, String petName, String petType) {
        Toast.makeText(this, "registered as PO", Toast.LENGTH_SHORT).show();
        //Register
        //VM methods
    }
}