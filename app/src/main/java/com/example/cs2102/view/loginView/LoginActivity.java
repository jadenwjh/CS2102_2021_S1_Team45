package com.example.cs2102.view.loginView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.cs2102.R;
import com.example.cs2102.constants.Strings;
import com.example.cs2102.model.User;
import com.example.cs2102.view.adminView.AdminActivity;
import com.example.cs2102.view.careTakerView.CareTakerHomepageActivity;
import com.example.cs2102.view.petOwnerView.PetOwnerHomepage;
import com.example.cs2102.view.registerView.RegisterActivity;

import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.username)
    EditText username;

    @BindView(R.id.password)
    EditText password;

    @BindView(R.id.login)
    Button login;

    @BindView(R.id.user_type)
    Spinner userType;

    @BindView(R.id.sign_up)
    Button signUp;

    @BindView(R.id.loading)
    ProgressBar loadingBar;

    private LoginViewModel loginViewModel;
    public SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        sharedPreferences = getSharedPreferences(Strings.PROFILE, Context.MODE_PRIVATE);
        loginViewModel = ViewModelProviders.of(this).get(LoginViewModel.class);
        generateMenu();

        login.setOnClickListener(v -> {
            String uName = username.getText().toString();
            String pw = password.getText().toString();
            String t = userType.getSelectedItem().toString();
            checkValidity(uName);
            checkValidity(pw);
            loginViewModel.loginAttempt(uName, pw, t);
            if (loginViewModel.userProfile.getValue() != null) {
                User currentUser = loginViewModel.userProfile.getValue();
                sharedPreferences.edit().putString(Strings.PROFILE, currentUser.getUserID()).apply();
                startUserPage(loginViewModel.userProfile.getValue());
            }
        });

        signUp.setOnClickListener(view -> startRegisterPage());

        loginViewModel.loading.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    loadingBar.setVisibility(View.VISIBLE);
                } else {
                    loadingBar.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    public void generateMenu() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.user_selection, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userType.setAdapter(adapter);
    }

    public void checkValidity(String input) {
        if (input.contains(" ")) {
            Log.d("WhiteSpaceError", "do not use spaces");
            return;
        }
        if (!Pattern.matches("[a-zA-Z]+", input)) {
            Log.d("SpecialCharactersError", "use only alphabets");
        }
    }

    private void startUserPage(User user) {
        String type = user.getType();
        switch (type) {
            case Strings.ADMIN:
                startActivity(new Intent(this, AdminActivity.class));
                finish();
                break;
            case Strings.PET_OWNER:
                startActivity(new Intent(this, PetOwnerHomepage.class));
                finish();
                break;
            case Strings.CARE_TAKER:
                startActivity(new Intent(this, CareTakerHomepageActivity.class));
                finish();
                break;
        }
    }

    private void startRegisterPage() {
        Intent registerUser = new Intent(this, RegisterActivity.class);
        startActivity(registerUser);
    }
}