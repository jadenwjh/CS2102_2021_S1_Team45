package com.example.cs2102.view.petOwnerView;

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

import com.example.cs2102.R;
import com.example.cs2102.model.UserProfile;
import com.example.cs2102.model.retrofitApi.Strings;
import com.example.cs2102.view.loginView.LoginActivity;

import java.util.ArrayList;

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

    @BindView(R.id.profile)
    Button profilePage;

    @BindView(R.id.title)
    TextView title;

    private static final String CURRENT_FRAGMENT = "PetOwnerFragment";
    private FragmentManager fm;
    private FragmentTransaction ft;
    private PetOwnerListingFragment listingFragment;
    private PetsFragment petsFragment;
    private PetOwnerBidsFragment bidsFragment;
    private PetOwnerProfileFragment profileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UserProfile userProfile = UserProfile.getInstance();
        String username = userProfile.username;

        setContentView(R.layout.activity_pet_owner_homepage);
        ButterKnife.bind(this);
        title.setText(String.format("PetOwner: %s", username));
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

            profileFragment = PetOwnerProfileFragment.newInstance();

            //default listing page
            ft.add(R.id.petOwner_fragment, listingFragment, CURRENT_FRAGMENT).commit();
            viewListings.setBackgroundColor(Color.CYAN);
        }

        viewListings.setOnClickListener(view -> {
            switchFragment(Strings.LISTINGS);
        });

        viewPets.setOnClickListener(view -> {
            switchFragment(Strings.PETS);
        });

        viewReview.setOnClickListener(view -> {
            switchFragment(Strings.BIDS);
        });

        profilePage.setOnClickListener(view -> {
            switchFragment(Strings.PROFILE);
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
        viewListings.setBackgroundColor(Color.BLACK);
        viewPets.setBackgroundColor(Color.BLACK);
        viewReview.setBackgroundColor(Color.BLACK);
        switch (key) {
            case Strings.PROFILE:
                toggleHideNavigator(true);
                ft = fm.beginTransaction();
                ft.replace(R.id.petOwner_fragment, profileFragment, CURRENT_FRAGMENT).commit();
                break;
            case Strings.LISTINGS:
                viewListings.setBackgroundColor(Color.CYAN);
                ft = fm.beginTransaction();
                listingFragment.clearSelection();
                ft.replace(R.id.petOwner_fragment, listingFragment, CURRENT_FRAGMENT).commit();
                break;
            case Strings.PETS:
                viewPets.setBackgroundColor(Color.CYAN);
                ft = fm.beginTransaction();
                ft.replace(R.id.petOwner_fragment, petsFragment, CURRENT_FRAGMENT).commit();
                break;
            case Strings.BIDS:
                viewReview.setBackgroundColor(Color.CYAN);
                bidsFragment.petOwnerBidsAdapter.updateBidsList(new ArrayList<>());
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
        if (fm.findFragmentByTag(CURRENT_FRAGMENT) instanceof ReviewFragment) {
            switchFragment(Strings.BIDS);
        } else if (listingFragment.getUserVisibleHint()) {
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