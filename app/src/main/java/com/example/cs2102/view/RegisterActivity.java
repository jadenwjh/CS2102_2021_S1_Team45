package com.example.cs2102.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.cs2102.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RegisterActivity extends AppCompatActivity {

    @BindView(R.id.pet_owner)
    Button petOwner;

    @BindView(R.id.care_taker)
    Button careTaker;

    @BindView(R.id.register)
    Button register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        petOwner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchFragment(1);
            }
        });

        careTaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchFragment(2);
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
                backToLoginPage();
            }
        });
    }

    private void switchFragment(int id) {
        FragmentManager fm = getSupportFragmentManager();

        switch (id) {
            case 1: {
                Fragment careTakerFragment = fm.findFragmentByTag("CareTakerSignUp");
                FragmentTransaction ft = fm.beginTransaction()
                        .replace(R.id.register_form, new PetOwnerSignUpFragment(), "PetOwnerSignUp");

                if (careTakerFragment != null) {
                    ft.remove(careTakerFragment);
                }

                ft.commit();
                break;
            }
            case 2: {
                Fragment petOwnerFragment = fm.findFragmentByTag("PetOwnerSignUp");
                FragmentTransaction ft = fm.beginTransaction()
                        .replace(R.id.register_form, new CareTakerSignUpFragment(), "CareTakerSignUp");

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

    private void registerUser() {}

    private void backToLoginPage() {
        Intent loginPage = new Intent(this, LoginActivity.class);
        startActivity(loginPage);
        finish();
    }
}