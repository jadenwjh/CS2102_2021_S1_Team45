package com.example.cs2102.view.loginView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.example.cs2102.R;
import com.example.cs2102.model.UserProfile;
import com.example.cs2102.model.retrofitApi.Strings;
import com.example.cs2102.view.adminView.AdminHomepageActivity;
import com.example.cs2102.view.careTakerView.CareTakerHomepageActivity;
import com.example.cs2102.view.deleteView.DeleteActivity;
import com.example.cs2102.view.petOwnerView.PetOwnerHomepageActivity;
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

    @BindView(R.id.delete)
    TextView delete;


    private LoginViewModel loginViewModel;
    private UserProfile userProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        userProfile = UserProfile.getInstance();

        loginViewModel = ViewModelProviders.of(this).get(LoginViewModel.class);
        loginViewModel.loading.setValue(false);
        generateMenu();

        login.setOnClickListener(v -> {
            hideKeyboard(this);
            String uName = username.getText().toString();
            String pw = password.getText().toString();
            String t = userType.getSelectedItem().toString();
            checkValidity(uName);
            checkValidity(pw);
            loginViewModel.loginAttempt(uName, pw, t);
        });

        signUp.setOnClickListener(view -> startRegisterPage());

        delete.setOnClickListener(view -> startDeleteUser());

        loginObserver();
    }

    private void generateMenu() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.user_selection, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userType.setAdapter(adapter);
    }

    private void checkValidity(String input) {
        if (input.contains(" ")) {
            Log.d("WhiteSpaceError", "do not use spaces");
            return;
        }
        if (!Pattern.matches("[a-zA-Z]+", input)) {
            Log.d("SpecialCharactersError", "use only alphabets");
        }
    }

    private void startUserPage(String type) {
        switch (type) {
            case Strings.ADMIN:
                startActivity(new Intent(this, AdminHomepageActivity.class));
                finish();
                break;
            case Strings.PET_OWNER:
                startActivity(new Intent(this, PetOwnerHomepageActivity.class));
                finish();
                break;
            case Strings.CARE_TAKER:
                startActivity(new Intent(this, CareTakerHomepageActivity.class));
                finish();
                break;
        }
    }

    private void loginObserver() {
        loginViewModel.loginSuccess.observe(this, success -> {
            if (success && userProfile.accType != null) {
                Log.e("Account", userProfile.accType);
                startUserPage(userProfile.accType);
            }
        });

        loginViewModel.loginFailed.observe(this, failed -> {
            if (failed) {
                Toast.makeText(this, "Check that your credentials are correctly filled", Toast.LENGTH_SHORT).show();
            }
        });

        loginViewModel.loading.observe(this, aBoolean -> {
            if (aBoolean) {
                loadingBar.setVisibility(View.VISIBLE);
            } else {
                loadingBar.setVisibility(View.INVISIBLE);
            }
        });

        loginViewModel.userProfile.observe(this, profile -> userProfile.setUserProfile(
                profile.get("username").toString(),
                profile.get("password").toString(),
                profile.get("email").toString(),
                profile.get("profile").toString(),
                profile.get("address").toString(),
                profile.get("phonenum").toString(),
                profile.get("acctype").toString()));
    }

    private void startRegisterPage() {
        Intent registerUser = new Intent(this, RegisterActivity.class);
        startActivity(registerUser);
    }

    private void startDeleteUser() {
        Intent deleteUser = new Intent(this, DeleteActivity.class);
        startActivity(deleteUser);
    }

    private void hideKeyboard(Activity activity) {
        if (activity.getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }
}