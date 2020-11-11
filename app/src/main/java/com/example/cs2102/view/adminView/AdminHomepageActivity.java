package com.example.cs2102.view.adminView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.cs2102.R;
import com.example.cs2102.model.UserProfile;
import com.example.cs2102.model.retrofitApi.Strings;
import com.example.cs2102.view.loginView.LoginActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AdminHomepageActivity extends AppCompatActivity {

    @BindView(R.id.viewCT)
    Button viewSalary;

    @BindView(R.id.finance)
    Button viewFinance;

    @BindView(R.id.setBasePrice)
    Button viewPetPrice;

    @BindView(R.id.homepageLoading)
    ProgressBar loadingBar;

    private static final String CURRENT_FRAGMENT = "AdminFragment";
    private FragmentManager fm;
    private FragmentTransaction ft;
    private AdminSetPriceFragment priceFragment;
    private AdminFinanceFragment ratingFragment;
    private AdminSalaryFragment salaryFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        ButterKnife.bind(this);
        loadingBar.setVisibility(View.VISIBLE);

        UserProfile userProfile = UserProfile.getInstance();
        String username = userProfile.username;

        fm = getSupportFragmentManager();
        ft = fm.beginTransaction();

        if (savedInstanceState == null) {
            priceFragment = AdminSetPriceFragment.newInstance();
            ratingFragment = AdminFinanceFragment.newInstance(username);
            salaryFragment = AdminSalaryFragment.newInstance(username);

            ft.add(R.id.admin_fragment, salaryFragment, CURRENT_FRAGMENT).commit();
            viewSalary.setBackgroundColor(Color.CYAN);
        }

        viewPetPrice.setOnClickListener(v -> switchFragment(Strings.MOD_BASE_PRICE));

        viewFinance.setOnClickListener(v -> switchFragment(Strings.RATING));

        viewSalary.setOnClickListener(v -> switchFragment(Strings.SALARY));

        loadingBar.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        Activity activity = this;
        if (activity.getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Toast.makeText(this, "You have logged out successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    private void switchFragment(String key) {
        viewFinance.setBackgroundColor(Color.BLACK);
        viewPetPrice.setBackgroundColor(Color.BLACK);
        viewSalary.setBackgroundColor(Color.BLACK);
        switch (key) {
            case Strings.MOD_BASE_PRICE:
                viewPetPrice.setBackgroundColor(Color.CYAN);
                ft = fm.beginTransaction();
                ft.replace(R.id.admin_fragment, priceFragment, CURRENT_FRAGMENT).commit();
                break;
            case Strings.SALARY:
                viewSalary.setBackgroundColor(Color.CYAN);
                ft = fm.beginTransaction();
                ft.replace(R.id.admin_fragment, salaryFragment, CURRENT_FRAGMENT).commit();
                break;
            case Strings.RATING:
                viewFinance.setBackgroundColor(Color.CYAN);
                ft = fm.beginTransaction();
                ft.replace(R.id.admin_fragment, ratingFragment, CURRENT_FRAGMENT).commit();
                break;
            default:
                throw new RuntimeException(String.format("Unable to load %s fragment", key));
        }
    }
}