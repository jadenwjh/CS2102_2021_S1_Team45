package com.example.cs2102.view.petOwnerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

public class PetOwnerHomepageActivity extends AppCompatActivity {

    @BindView(R.id.homepageLoading)
    ProgressBar loadingBar;

    @BindView(R.id.viewListings)
    Button viewListings;

    @BindView(R.id.viewPets)
    Button viewPets;
    @BindView(R.id.viewReview)
    Button viewReview;

    private static final String CURRENT_FRAGMENT = "PetOwnerFragment";
    private FragmentManager fm;
    private FragmentTransaction ft;
    private PetOwnerListingFragment listingFragment;
    private PetsFragment petsFragment;
    private PetOwnerBidsFragment bidsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UserProfile userProfile = UserProfile.getInstance();
        String username = userProfile.username;

        setContentView(R.layout.activity_pet_owner_homepage);
        fm = getSupportFragmentManager();
        ft = fm.beginTransaction();

        if (savedInstanceState == null) {
            listingFragment = PetOwnerListingFragment.newInstance(username);
            listingFragment.setPetOwnerListingSelectedListener(selectedListing -> {
                selectedListing.setListingSelectedListener(() -> {
                    switchFragment(Strings.LISTINGS);
                });
                ft = fm.beginTransaction();
                toggleHideNavigator(true);
                ft.replace(R.id.petOwner_fragment, selectedListing, CURRENT_FRAGMENT).commit();
            });

            petsFragment = PetsFragment.newInstance(username);
            petsFragment.setPetsFragmentRefreshListener(new PetsFragment.PetsFragmentRefreshListener() {
                @Override
                public void refreshPetsFragment() {
                    ft = fm.beginTransaction();
                    ft.detach(petsFragment);
                    ft.attach(petsFragment);
                    ft.commit();
                }
            });

            bidsFragment = PetOwnerBidsFragment.newInstance(username);
            bidsFragment.setBidsFragmentReviewListener(selectedBid -> {
                selectedBid.setExitReviewFragmentCallback(() -> {
                    switchFragment(Strings.BIDS);
                });
                ft = fm.beginTransaction();
                toggleHideNavigator(true);
                ft.replace(R.id.petOwner_fragment, selectedBid, CURRENT_FRAGMENT).commit();
            });

            //default listing page
            ft.add(R.id.petOwner_fragment, listingFragment, CURRENT_FRAGMENT).commit();
        }

        ButterKnife.bind(this);

        viewListings.setOnClickListener(view -> {
            switchFragment(Strings.LISTINGS);
        });

        viewPets.setOnClickListener(view -> {
            switchFragment(Strings.PETS);
        });

        viewReview.setOnClickListener(view -> {
            switchFragment(Strings.BIDS);
        });

        loadingBar.setVisibility(View.GONE);
    }

    private void toggleHideNavigator(boolean hide) {
        if (hide) {
            viewListings.setVisibility(View.INVISIBLE);
            viewPets.setVisibility(View.INVISIBLE);
            viewReview.setVisibility(View.INVISIBLE);
        } else {
            viewListings.setVisibility(View.VISIBLE);
            viewPets.setVisibility(View.VISIBLE);
            viewReview.setVisibility(View.VISIBLE);
        }
    }

    private void switchFragment(String key) {
        toggleHideNavigator(false);
        switch (key) {
            case Strings.LISTINGS:
                ft = fm.beginTransaction();
                ft.replace(R.id.petOwner_fragment, listingFragment, CURRENT_FRAGMENT).commit();
                break;
            case Strings.PETS:
                ft = fm.beginTransaction();
                ft.replace(R.id.petOwner_fragment, petsFragment, CURRENT_FRAGMENT).commit();
                break;
            case Strings.BIDS:
                ft = fm.beginTransaction();
                ft.replace(R.id.petOwner_fragment, bidsFragment, CURRENT_FRAGMENT).commit();
                break;
            default:
                throw new RuntimeException(String.format("Unable to load %s fragment", key));
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

    @Override
    public void onBackPressed() {
        if (listingFragment.getUserVisibleHint()) {
            switchFragment(Strings.LISTINGS);
        }
        hideKeyboard(this);
    }

    private void hideKeyboard(Activity activity) {
        if (activity.getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }
}