package com.example.cs2102.view.careTakerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import com.example.cs2102.R;
import com.example.cs2102.model.UserProfile;
import com.example.cs2102.model.retrofitApi.Strings;
import com.example.cs2102.view.careTakerView.viewModel.CareTakerHomepageViewModel;
import com.example.cs2102.view.loginView.LoginActivity;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CareTakerHomepageActivity extends AppCompatActivity {

    @BindView(R.id.viewBids)
    Button viewBids;

    @BindView(R.id.viewLeaves)
    Button viewLeavesOrFree;

    @BindView(R.id.homepageLoading)
    ProgressBar loadingBar;

    @BindView(R.id.viewPrices)
    Button viewPrices;

    @BindView(R.id.viewSalary)
    Button viewSalary;

    @BindView(R.id.profile)
    Button careTakerProfile;

    @BindView(R.id.title)
    TextView title;

    private static final String CURRENT_FRAGMENT = "CareTakerFragment";
    private FragmentManager fm;
    private FragmentTransaction ft;
    private CareTakerBidsFragment bidsFragment;
    private CareTakerSetPriceFragment priceFragment;
    private CareTakerAvailabilityFragment availabilityFragment;
    private CareTakerHomepageViewModel homepageViewModel;
    private CareTakerSalaryFragment salaryFragment;
    private CareTakerProfileFragment profileFragment;

    private DatePickerDialog.OnDateSetListener dateSetListener;

    public void setDateSetListener(DatePickerDialog.OnDateSetListener listener) {
        this.dateSetListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UserProfile userProfile = UserProfile.getInstance();
        String username = userProfile.username;
        homepageViewModel = ViewModelProviders.of(this).get(CareTakerHomepageViewModel.class);

        setContentView(R.layout.activity_care_taker_homepage);
        ButterKnife.bind(this);
        title.setText(String.format("Care Taker: %s", username));
        fm = getSupportFragmentManager();
        ft = fm.beginTransaction();

        homepageViewModel.fetchContract(username);
        homepageViewModel.contractFetched.observe(this, isReady -> {
            if (isReady) {
                String contract = homepageViewModel.contract.getValue();
                Log.e("Fetched contract", contract);
                if (savedInstanceState == null) {
                    priceFragment = CareTakerSetPriceFragment.newInstance(username);
                    priceFragment.setCareTakerSetPriceRefresh(new CareTakerSetPriceFragment.CareTakerSetPriceRefresh() {
                        @Override
                        public void refreshFragment() {
                            ft = fm.beginTransaction();
                            ft.detach(priceFragment);
                            ft.attach(priceFragment);
                            ft.commit();
                        }
                    });

                    bidsFragment = CareTakerBidsFragment.newInstance(username);
                    bidsFragment.setCareTakerBidsFragmentListener(selectedBid -> {
                        selectedBid.setBidSelectedFragmentListener(() -> {
                            switchFragment(Strings.BIDS);
                        });
                        ft = fm.beginTransaction();
                        toggleHideNavigator(true);
                        ft.replace(R.id.careTaker_fragment, selectedBid, CURRENT_FRAGMENT).commit();
                    });

                    setDateSetListener(new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                            String date = String.format("%d-%d-%d", year, monthOfYear, dayOfMonth);
                            if (contract.equals(Strings.PART_TIME)) {
                                homepageViewModel.requestToSendAvailability(username, date, CareTakerHomepageActivity.this);
                            }
                            if (contract.equals(Strings.FULL_TIME)) {
                                homepageViewModel.requestToApplyLeave(username, date, CareTakerHomepageActivity.this);
                            }
                        }
                    });

                    availabilityFragment = CareTakerAvailabilityFragment.newInstance(contract);

                    Calendar now = Calendar.getInstance();
                    Log.e("requestToApplyLeave", now.toString());
                    availabilityFragment.setAvailabilityDatePicker(days -> {
                        String date = "";
                        DatePickerDialog datePicker = DatePickerDialog.newInstance(dateSetListener, now);
                        datePicker.setAccentColor(Color.BLACK);
                        datePicker.setSelectableDays(days);
                        datePicker.show(getSupportFragmentManager(), CURRENT_FRAGMENT);
                    });

                    salaryFragment = CareTakerSalaryFragment.newInstance(username);

                    profileFragment = CareTakerProfileFragment.newInstance();

                    //default bid page
                    ft.add(R.id.careTaker_fragment, bidsFragment, CURRENT_FRAGMENT).commit();
                    viewBids.setBackgroundColor(Color.CYAN);
                }

                if (contract.equals(Strings.FULL_TIME)) {
                    viewLeavesOrFree.setText(R.string.leaves);
                }

                if (contract.equals(Strings.PART_TIME)) {
                    viewLeavesOrFree.setText(R.string.availability);
                }

                viewLeavesOrFree.setOnClickListener(view -> switchFragment(Strings.LEAVES_AVAILABILITY));

                viewBids.setOnClickListener(view -> switchFragment(Strings.BIDS));

                viewPrices.setOnClickListener(view -> switchFragment(Strings.PRICES));

                viewSalary.setOnClickListener(view -> switchFragment(Strings.SALARY));

                careTakerProfile.setOnClickListener(view -> switchFragment(Strings.PROFILE));

                careTakerHomepageObserver();

                loadingBar.setVisibility(View.GONE);
            }
        });
    }

    private void toggleHideNavigator(boolean hide) {
        if (hide) {
            viewBids.setVisibility(View.INVISIBLE);
            viewLeavesOrFree.setVisibility(View.INVISIBLE);
            viewPrices.setVisibility(View.INVISIBLE);
            viewSalary.setVisibility(View.INVISIBLE);
        } else {
            viewSalary.setVisibility(View.VISIBLE);
            viewBids.setVisibility(View.VISIBLE);
            viewLeavesOrFree.setVisibility(View.VISIBLE);
            viewPrices.setVisibility(View.VISIBLE);
        }
    }

    private void switchFragment(String key) {
        toggleHideNavigator(false);
        viewBids.setBackgroundColor(Color.BLACK);
        viewPrices.setBackgroundColor(Color.BLACK);
        viewLeavesOrFree.setBackgroundColor(Color.BLACK);
        viewSalary.setBackgroundColor(Color.BLACK);
        switch (key) {
            case Strings.PROFILE:
                toggleHideNavigator(true);
                ft = fm.beginTransaction();
                ft.replace(R.id.careTaker_fragment, profileFragment, CURRENT_FRAGMENT).commit();
                break;
            case Strings.BIDS:
                viewBids.setBackgroundColor(Color.CYAN);
                ft = fm.beginTransaction();
                ft.replace(R.id.careTaker_fragment, bidsFragment, CURRENT_FRAGMENT).commit();
                break;
            case Strings.PRICES:
                viewPrices.setBackgroundColor(Color.CYAN);
                ft = fm.beginTransaction();
                ft.replace(R.id.careTaker_fragment, priceFragment, CURRENT_FRAGMENT).commit();
                break;
            case Strings.LEAVES_AVAILABILITY:
                viewLeavesOrFree.setBackgroundColor(Color.CYAN);
                ft = fm.beginTransaction();
                ft.replace(R.id.careTaker_fragment, availabilityFragment, CURRENT_FRAGMENT).commit();
                break;
            case Strings.SALARY:
                viewSalary.setBackgroundColor(Color.CYAN);
                ft = fm.beginTransaction();
                ft.replace(R.id.careTaker_fragment, salaryFragment, CURRENT_FRAGMENT).commit();
                break;
            default:
                throw new RuntimeException(String.format("Unable to load %s fragment", key));
        }
    }

    private void careTakerHomepageObserver() {
        homepageViewModel.loading.observe(this, isLoading -> {
            if (isLoading) {
                loadingBar.setVisibility(View.VISIBLE);
            } else {
                loadingBar.setVisibility(View.GONE);
            }
        });
        homepageViewModel.loadErrorPT.observe(this, isLoading -> {
            if (isLoading) {
                Toast.makeText(this, "This date has already been chosen", Toast.LENGTH_SHORT).show();
            }
        });
        homepageViewModel.loadErrorFT.observe(this, isLoading -> {
            if (isLoading) {
                Toast.makeText(this, "Violates 150 days constraint", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (bidsFragment.getUserVisibleHint()) {
            switchFragment(Strings.BIDS);
        } else {
            Activity activity = this;
            if (activity.getCurrentFocus() != null) {
                InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
            }
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
}