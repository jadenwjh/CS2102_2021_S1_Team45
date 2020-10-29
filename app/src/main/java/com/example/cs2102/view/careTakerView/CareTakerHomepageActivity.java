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
import androidx.lifecycle.Observer;

import com.example.cs2102.R;
import com.example.cs2102.constants.Strings;
import com.example.cs2102.model.PetOwner;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CareTakerHomepageActivity extends AppCompatActivity implements CareTakerBidsAdapter.BidsListener, CareTakerLeaveFragment.ApplyLeaveListener, CareTakerSetPriceFragment.SetPetPriceListener {

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
    private static String username;
    public MutableLiveData<Boolean> isLoading = new MutableLiveData<Boolean>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isLoading.setValue(false);
        username = getSharedPreferences(Strings.PROFILE, Context.MODE_PRIVATE).getString(Strings.PROFILE, Strings.PROFILE);
        setContentView(R.layout.activity_care_taker_homepage);
        FragmentManager fm = getSupportFragmentManager();

        if (savedInstanceState == null) {
            isLoading.setValue(true);
            ft = fm.beginTransaction();
            bidsFragment = CareTakerBidsFragment.newInstance(username);
            leaveFragment = CareTakerLeaveFragment.newInstance(username);
            priceFragment = CareTakerSetPriceFragment.newInstance(username);
            //default bid page
            ft.add(R.id.careTaker_fragment, bidsFragment, CURRENT_FRAGMENT).commit();
            isLoading.setValue(false);
        }

        ButterKnife.bind(this);

        viewBids.setOnClickListener(view -> {
            switchFragment(Strings.BIDS);
        });

        viewLeaves.setOnClickListener(view -> {
            switchFragment(Strings.LEAVES);
        });

        viewPrices.setOnClickListener(view -> {
            switchFragment(Strings.PRICES);
        });

        isLoading.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    loading.setVisibility(View.VISIBLE);
                } else {
                    loading.setVisibility(View.GONE);
                }
            }
        });
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

    @Override
    public void onBidSelected(PetOwner petOwner) {
        //load new fragment
        //TODO: using petOwner, generate fragment with his profile and a button to accept bid
    }

    @Override
    public void onExitApplyLeave() {
        switchFragment(Strings.BIDS);
    }

    @Override
    public void onExitSetPetPrice() {
        switchFragment(Strings.BIDS);
    }
}