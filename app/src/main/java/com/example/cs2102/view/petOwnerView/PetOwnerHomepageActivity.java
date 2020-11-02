package com.example.cs2102.view.petOwnerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.cs2102.R;
import com.example.cs2102.model.UserProfile;
import com.example.cs2102.model.retrofitApi.Strings;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PetOwnerHomepageActivity extends AppCompatActivity {

    @BindView(R.id.homepageLoading)
    ProgressBar loadingBar;

    @BindView(R.id.viewListings)
    Button viewListings;

    @BindView(R.id.viewPets)
    Button viewPets;

    @BindView(R.id.viewOngoing)
    Button viewOngoing;

    @BindView(R.id.viewReview)
    Button viewReview;

    private static final String CURRENT_FRAGMENT = "PetOwnerFragment";
    private FragmentManager fm;
    private FragmentTransaction ft;
    private PetOwnerListingFragment listingFragment;
    private PetsFragment petsFragment;

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

        loadingBar.setVisibility(View.GONE);
    }

    private void toggleHideNavigator(boolean hide) {
        if (hide) {
            viewListings.setVisibility(View.INVISIBLE);
            viewPets.setVisibility(View.INVISIBLE);
            viewOngoing.setVisibility(View.INVISIBLE);
            viewReview.setVisibility(View.INVISIBLE);
        } else {
            viewListings.setVisibility(View.VISIBLE);
            viewPets.setVisibility(View.VISIBLE);
            viewOngoing.setVisibility(View.VISIBLE);
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
            default:
                throw new RuntimeException(String.format("Unable to load %s fragment", key));
        }
    }

}