package com.example.cs2102.view.careTakerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import com.example.cs2102.R;
import com.example.cs2102.model.UserProfile;
import com.example.cs2102.view.careTakerView.viewModel.CareTakerAvailabilityViewModel;
import com.example.cs2102.view.careTakerView.viewModel.CareTakerLeaveViewModel;
import com.example.cs2102.widgets.Strings;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CareTakerHomepageActivity extends AppCompatActivity {

    @BindView(R.id.viewBids)
    Button viewBids;

    @BindView(R.id.viewLeaves)
    Button viewLeavesOrFree;

    @BindView(R.id.viewPrices)
    Button viewPrices;

    private FragmentTransaction ft;
    private CareTakerBidsFragment bidsFragment;
    private CareTakerLeaveFragment leaveFragment;
    private CareTakerSetPriceFragment priceFragment;
    private CareTakerAvailabilityFragment availabilityFragment;

    private FragmentManager fm;

    private static final String CURRENT_FRAGMENT = "CareTakerFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UserProfile userProfile = UserProfile.getInstance();
        String username = userProfile.username;

        setContentView(R.layout.activity_care_taker_homepage);
        fm = getSupportFragmentManager();
        ft = fm.beginTransaction();

        if (savedInstanceState == null) {
            bidsFragment = CareTakerBidsFragment.newInstance(username);
            priceFragment = CareTakerSetPriceFragment.newInstance(username);

            bidsFragment.setCareTakerBidsFragmentListener(selectedBid -> {
                toggleHideNavigator(true);
                selectedBid.setBidSelectedFragmentListener(() -> {
                    switchFragment(Strings.BIDS);
                    toggleHideNavigator(false);
                });
                ft = fm.beginTransaction();
                ft.replace(R.id.careTaker_fragment, selectedBid, CURRENT_FRAGMENT).commit();
            });

            if (userProfile.contract.equals(Strings.FULL_TIME)) {
                CareTakerLeaveViewModel careTakerLeaveViewModel = ViewModelProviders.of(this).get(CareTakerLeaveViewModel.class);
                leaveFragment = CareTakerLeaveFragment.newInstance(username, careTakerLeaveViewModel);
                leaveFragment.setLeaveDatePicker(days -> {
                    @SuppressLint("DefaultLocale")
                    DatePickerDialog datePicker = DatePickerDialog.newInstance((view, year, monthOfYear, dayOfMonth) -> careTakerLeaveViewModel.selectedDate.setValue((String.format("%d-%d-%d", year, monthOfYear, dayOfMonth))));
                    datePicker.setSelectableDays(days);
                    datePicker.show(getSupportFragmentManager(), CURRENT_FRAGMENT);
                });
            }

            if (userProfile.contract.equals(Strings.PART_TIME)) {
                CareTakerAvailabilityViewModel careTakerAvailabilityViewModel = ViewModelProviders.of(this).get(CareTakerAvailabilityViewModel.class);
                availabilityFragment = CareTakerAvailabilityFragment.newInstance(username);
                availabilityFragment.setAvailabilityDatePicker(days -> {
                    @SuppressLint("DefaultLocale")
                    DatePickerDialog datePicker = DatePickerDialog.newInstance((view, year, monthOfYear, dayOfMonth) -> careTakerAvailabilityViewModel.selectedDate.setValue((String.format("%d-%d-%d", year, monthOfYear, dayOfMonth))));
                    datePicker.setSelectableDays(days);
                    datePicker.show(getSupportFragmentManager(), CURRENT_FRAGMENT);
                });
            }

            //default bid page
            ft.add(R.id.careTaker_fragment, bidsFragment, CURRENT_FRAGMENT).commit();
        }

        ButterKnife.bind(this);

        if (userProfile.contract.equals(Strings.FULL_TIME)) {
            viewLeavesOrFree.setText(R.string.leaves);
            viewLeavesOrFree.setOnClickListener(view -> switchFragment(Strings.LEAVES));
        }

        if (userProfile.contract.equals(Strings.PART_TIME)) {
            viewLeavesOrFree.setText(R.string.availability);
            viewLeavesOrFree.setOnClickListener(view -> switchFragment(Strings.PT_FREE));
        }

        viewBids.setOnClickListener(view -> switchFragment(Strings.BIDS));

        viewPrices.setOnClickListener(view -> switchFragment(Strings.PRICES));

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
        switch (key) {
            case Strings.BIDS:
                ft = fm.beginTransaction();
                ft.replace(R.id.careTaker_fragment, bidsFragment, CURRENT_FRAGMENT).commit();
                break;
            case Strings.PRICES:
                ft = fm.beginTransaction();
                ft.replace(R.id.careTaker_fragment, priceFragment, CURRENT_FRAGMENT).commit();
                break;
            case Strings.LEAVES:
                ft = fm.beginTransaction();
                ft.replace(R.id.careTaker_fragment, leaveFragment, CURRENT_FRAGMENT).commit();
                break;
            case Strings.PT_FREE:
                ft = fm.beginTransaction();
                ft.replace(R.id.careTaker_fragment, availabilityFragment, CURRENT_FRAGMENT).commit();
                break;
            default:
                throw new RuntimeException(String.format("Unable to load %s fragment", key));
        }
    }
}