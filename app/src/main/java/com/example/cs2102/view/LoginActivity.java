package com.example.cs2102.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.cs2102.R;
import com.example.cs2102.viewModel.LoginVM;
import com.example.cs2102.viewModel.PetOwnerVM;

import java.util.regex.Matcher;
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

    @BindView(R.id.loading)
    ProgressBar loading;

    @BindView(R.id.user_type)
    Spinner userType;

    private LoginVM loginViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        makeSpinner();

        loginViewModel = ViewModelProviders.of(this).get(LoginVM.class);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uName = username.getText().toString();
                String pw = password.getText().toString();
                String t = userType.getSelectedItem().toString();
                checkValidity(uName);
                checkValidity(pw);
                boolean loginSuccess = loginViewModel.isRegistered(uName, pw, t);
                if (loginSuccess) {
                    //new intent
                } else {
                    //invalid credentials
                }
                Log.d("Username:", uName);
                Log.d("Password:", pw);
                Log.d("Type:", t);
            }
        });
    }

    public void makeSpinner() {
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
}