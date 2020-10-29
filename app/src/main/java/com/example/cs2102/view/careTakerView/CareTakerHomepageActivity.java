package com.example.cs2102.view.careTakerView;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.MutableLiveData;

import com.example.cs2102.R;
import com.example.cs2102.constants.Strings;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CareTakerHomepageActivity extends AppCompatActivity {

    @BindView(R.id.loading)
    ProgressBar loading;

    @BindView(R.id.viewBids)
    Button viewBids;

    @BindView(R.id.viewLeaves)
    Button viewLeaves;

    @BindView(R.id.viewPrices)
    Button viewPrices;

    private FragmentTransaction ft;
    private CareTakerBidsFragment bidsFragment;
    private CareTakerLeaveFragment leaveFragment;
    private CareTakerSetPriceFragment priceFragment;

    private static final String CURRENT_FRAGMENT = "CareTakerFragment";

    public MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isLoading.setValue(false);
        String username = getSharedPreferences(Strings.PROFILE, Context.MODE_PRIVATE).getString(Strings.PROFILE, Strings.PROFILE);
        setContentView(R.layout.activity_care_taker_homepage);
        FragmentManager fm = getSupportFragmentManager();

        if (savedInstanceState == null) {
            isLoading.setValue(true);
            ft = fm.beginTransaction();
            bidsFragment = CareTakerBidsFragment.newInstance(username);
            leaveFragment = CareTakerLeaveFragment.newInstance(username);
            priceFragment = CareTakerSetPriceFragment.newInstance(username);

            bidsFragment.setCareTakerBidsFragmentListener(selectedBid -> {
                isLoading.setValue(true);
                toggleHideNavigator(true);
                selectedBid.setBidSelectedFragmentListener(() -> {
                    switchFragment(Strings.BIDS);
                    toggleHideNavigator(false);
                });
                ft.replace(R.id.careTaker_fragment, selectedBid, CURRENT_FRAGMENT).commit();
                isLoading.setValue(false);
            });

            //default bid page
            ft.add(R.id.careTaker_fragment, bidsFragment, CURRENT_FRAGMENT).commit();
            isLoading.setValue(false);
        }

        ButterKnife.bind(this);

        viewBids.setOnClickListener(view -> {
            isLoading.setValue(true);
            switchFragment(Strings.BIDS);
            isLoading.setValue(false);
        });

        viewLeaves.setOnClickListener(view -> {
            isLoading.setValue(true);
            switchFragment(Strings.LEAVES);
            isLoading.setValue(false);
        });

        viewPrices.setOnClickListener(view -> {
            isLoading.setValue(true);
            switchFragment(Strings.PRICES);
            isLoading.setValue(false);
        });

        isLoading.observe(this, aBoolean -> {
            if (aBoolean) {
                loading.setVisibility(View.VISIBLE);
            } else {
                loading.setVisibility(View.GONE);
            }
        });
    }

    private void toggleHideNavigator(boolean hide) {
        if (hide) {
            viewBids.setVisibility(View.INVISIBLE);
            viewLeaves.setVisibility(View.INVISIBLE);
            viewPrices.setVisibility(View.INVISIBLE);
        } else {
            viewBids.setVisibility(View.VISIBLE);
            viewLeaves.setVisibility(View.VISIBLE);
            viewPrices.setVisibility(View.VISIBLE);
        }
    }

    private void switchFragment(String key) {
        isLoading.setValue(true);
        switch (key) {
            case Strings.BIDS:
                ft.replace(R.id.careTaker_fragment, bidsFragment, CURRENT_FRAGMENT).commit();
                break;
            case Strings.PRICES:
                ft.replace(R.id.careTaker_fragment, priceFragment, CURRENT_FRAGMENT).commit();
                break;
            case Strings.LEAVES:
                ft.replace(R.id.careTaker_fragment, leaveFragment, CURRENT_FRAGMENT).commit();
                break;
            default:
                throw new RuntimeException(String.format("Unable to load %s fragment", key));
        }
        isLoading.setValue(false);
    }
}