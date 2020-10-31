package com.example.cs2102.view.careTakerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import com.example.cs2102.R;
import com.example.cs2102.model.UserProfile;
import com.example.cs2102.view.careTakerView.viewModel.CareTakerHomepageViewModel;
import com.example.cs2102.widgets.Strings;
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

    private FragmentTransaction ft;
    private CareTakerBidsFragment bidsFragment;
    private CareTakerSetPriceFragment priceFragment;
    private CareTakerAvailabilityFragment availabilityFragment;
    private CareTakerHomepageViewModel homepageViewModel;

    private DatePickerDialog.OnDateSetListener dateSetListener;

    public void setDateSetListener(DatePickerDialog.OnDateSetListener listener) {
        this.dateSetListener = listener;
    }

    private FragmentManager fm;

    private static final String CURRENT_FRAGMENT = "CareTakerFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UserProfile userProfile = UserProfile.getInstance();
        String username = userProfile.username;
        homepageViewModel = ViewModelProviders.of(this).get(CareTakerHomepageViewModel.class);

        setContentView(R.layout.activity_care_taker_homepage);
        fm = getSupportFragmentManager();
        ft = fm.beginTransaction();

        if (savedInstanceState == null) {
            bidsFragment = CareTakerBidsFragment.newInstance(username);
            priceFragment = CareTakerSetPriceFragment.newInstance(username);

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
                    if (userProfile.contract.equals(Strings.PART_TIME)) {
                        homepageViewModel.requestToSendAvailability(username, date);
                    }
                    if (userProfile.contract.equals(Strings.FULL_TIME)) {
                        homepageViewModel.requestToApplyLeave(username, date);
                    }
                }
            });

            availabilityFragment = CareTakerAvailabilityFragment.newInstance(userProfile.contract);
            Calendar now = Calendar.getInstance();
            availabilityFragment.setAvailabilityDatePicker(days -> {
                String date = "";
                DatePickerDialog datePicker = DatePickerDialog.newInstance(dateSetListener, now);
                datePicker.setSelectableDays(days);
                datePicker.show(getSupportFragmentManager(), CURRENT_FRAGMENT);
            });

            //default bid page
            ft.add(R.id.careTaker_fragment, bidsFragment, CURRENT_FRAGMENT).commit();
        }

        ButterKnife.bind(this);
        loadingBar.setVisibility(View.GONE);

        if (userProfile.contract.equals(Strings.FULL_TIME)) {
            viewLeavesOrFree.setText(R.string.leaves);
        }

        if (userProfile.contract.equals(Strings.PART_TIME)) {
            viewLeavesOrFree.setText(R.string.availability);
        }

        viewLeavesOrFree.setOnClickListener(view -> switchFragment(Strings.LEAVES_AVAILABILITY));

        viewBids.setOnClickListener(view -> switchFragment(Strings.BIDS));

        viewPrices.setOnClickListener(view -> switchFragment(Strings.PRICES));

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

    private void toggleHideNavigator(boolean hide) {
        if (hide) {
            viewBids.setVisibility(View.INVISIBLE);
            viewLeavesOrFree.setVisibility(View.INVISIBLE);
            viewPrices.setVisibility(View.INVISIBLE);
        } else {
            viewBids.setVisibility(View.VISIBLE);
            viewLeavesOrFree.setVisibility(View.VISIBLE);
            viewPrices.setVisibility(View.VISIBLE);
        }
    }

    private void switchFragment(String key) {
        toggleHideNavigator(false);
        switch (key) {
            case Strings.BIDS:
                ft = fm.beginTransaction();
                ft.replace(R.id.careTaker_fragment, bidsFragment, CURRENT_FRAGMENT).commit();
                break;
            case Strings.PRICES:
                ft = fm.beginTransaction();
                ft.replace(R.id.careTaker_fragment, priceFragment, CURRENT_FRAGMENT).commit();
                break;
            case Strings.LEAVES_AVAILABILITY:
                ft = fm.beginTransaction();
                ft.replace(R.id.careTaker_fragment, availabilityFragment, CURRENT_FRAGMENT).commit();
                break;
            default:
                throw new RuntimeException(String.format("Unable to load %s fragment", key));
        }
    }

    @Override
    public void onBackPressed() {
        switchFragment(Strings.BIDS);
    }
}