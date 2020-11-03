package com.example.cs2102.view.deleteView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cs2102.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DeleteActivity extends AppCompatActivity {

    @BindView(R.id.loading)
    ProgressBar loadingBar;

    @BindView(R.id.username)
    EditText username;

    @BindView(R.id.password)
    EditText password;

    @BindView(R.id.delete)
    Button delete;

    private DeleteViewModel deleteViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete);
        ButterKnife.bind(this);

        deleteViewModel = ViewModelProviders.of(this).get(DeleteViewModel.class);
        deleteViewModel.loading.setValue(false);

        delete.setOnClickListener(v -> {
            hideKeyboard(this);
            String uName = username.getText().toString();
            String pw = password.getText().toString();
            deleteViewModel.deleteUser(uName, pw);
        });

        deleteObserver();
    }

    private void deleteObserver() {
        deleteViewModel.deleteSuccess.observe(this, deleted -> {
            if (deleted) {
                super.onBackPressed();
            }
        });
        deleteViewModel.loading.observe(this, isLoading -> {
            if (isLoading) {
                loadingBar.setVisibility(View.VISIBLE);
            } else {
                loadingBar.setVisibility(View.GONE);
            }
        });
        deleteViewModel.loadError.observe(this, failed -> {
            if (failed) {
                Toast.makeText(this, "This account does not exist or is currently part of a bid", Toast.LENGTH_SHORT).show();
            }
        });
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

    private void hideKeyboard(Activity activity) {
        if (activity.getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }
}