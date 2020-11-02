package com.example.cs2102.view.petOwnerView;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.cs2102.R;
import com.example.cs2102.model.Listing;
import com.example.cs2102.model.Pet;
import com.example.cs2102.view.petOwnerView.viewModel.PetOwnerListingViewModel;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.gson.internal.LinkedTreeMap;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PetOwnerListingFragment extends Fragment {

    @BindView(R.id.refreshListing)
    SwipeRefreshLayout refreshLayout;

    @BindView(R.id.petTypes)
    Spinner petSpinner;

    @BindView(R.id.listingLoading)
    ProgressBar loadingBar;

    @BindView(R.id.pet_date_picker)
    Button datePicker;

    @BindView(R.id.search_listings)
    Button search;

    @BindView(R.id.no_listing)
    TextView noListing;

    @BindView(R.id.no_pets)
    TextView noPets;

    @BindView(R.id.all_listings)
    RecyclerView listingsRecyclerView;

    private static Pet currentPet;
    private static String endDate = "";
    private static String startDate = "";
    private static int currentPosition;

    private static String currentPetOwnerUsername;

    private PetOwnerListingViewModel petOwnerListingViewModel;
    private PetOwnerListingAdapter listingAdapter = new PetOwnerListingAdapter(new ArrayList<>());

    private PetOwnerListingSelectedListener selectedListener;

    public interface PetOwnerListingSelectedListener {
        void onListingSelected(ListingFragment selectedListing);
    }

    public void setPetOwnerListingSelectedListener(PetOwnerListingSelectedListener impl) {
        this.selectedListener = impl;
    }

    public static PetOwnerListingFragment newInstance(String username) {
        currentPetOwnerUsername = username;
        return new PetOwnerListingFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.pet_owner_listing_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        search.setEnabled(false);
        noPets.setVisibility(View.GONE);
        petSpinner.setVisibility(View.GONE);

        petOwnerListingViewModel = ViewModelProviders.of(this).get(PetOwnerListingViewModel.class);
        petOwnerListingViewModel.loading.setValue(false);

        listingsRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        listingsRecyclerView.setAdapter(listingAdapter);

        listingAdapter.setListingListener(listing -> {
            ListingFragment currentListing = ListingFragment.newInstance(currentPetOwnerUsername, listing, currentPet);
            selectedListener.onListingSelected(currentListing);
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPet != null && !startDate.equals("") && !endDate.equals("")) {
                    petOwnerListingViewModel.refreshListings(currentPet.getType(), startDate, endDate, currentPetOwnerUsername);
                } else {
                    Toast.makeText(getContext(), "Please select date range", Toast.LENGTH_SHORT).show();
                }
            }
        });

        refreshLayout.setOnRefreshListener(() -> {
            petOwnerListingViewModel.refreshListings(currentPet.getType(), startDate, endDate, currentPetOwnerUsername);
            refreshLayout.setRefreshing(false);
        });

        datePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRangePicker();
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listingsObserver();
        petOwnerListingViewModel.fetchOwnedPets(currentPetOwnerUsername);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    private void showRangePicker() {
        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        builder.setTheme(R.style.ThemeOverlay_MaterialComponents_MaterialCalendar);
        builder.setTitleText(R.string.select_dates);
        builder.setCalendarConstraints(constraintsBuilder.build());
        MaterialDatePicker<Pair<Long, Long>> picker = builder.build();
        picker.show(getParentFragmentManager(), picker.toString());
        picker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>>() {
            @Override
            public void onPositiveButtonClick(Pair<Long, Long> selection) {
                SimpleDateFormat spf = new SimpleDateFormat("yyyy-MM-dd");
                startDate = spf.format(new Date(selection.first));
                endDate = spf.format(new Date(selection.second));
                String setButton = String.format("%s/%s-%s/%s", startDate.substring(8,10), startDate.substring(5,7), endDate.substring(8,10), endDate.substring(5,7));
                datePicker.setText(setButton);
                picker.dismiss();
            }
        });
    }

    private void refreshSpinner(String[] arr) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, arr);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        petSpinner.setAdapter(adapter);
        petSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentPet = petOwnerListingViewModel.ownedPets.getValue().get(position);
                currentPosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        petSpinner.setSelection(currentPosition);
    }

    private void listingsObserver() {
        petOwnerListingViewModel.careTakers.observe(getViewLifecycleOwner(), _listings -> {
            if (_listings != null && _listings.size() != 0) {
                List<Listing> listings = new ArrayList<>();
                for (LinkedTreeMap<String,String> listing : _listings) {
                    Listing obj = new Listing(
                            listing.get("caretaker"),
                            listing.get("category"),
                            listing.get("feeperday"),
                            listing.get("startdate"),
                            listing.get("enddate"));
                    listings.add(obj);
                }
                listingAdapter.updateListings(listings);
                listingsRecyclerView.setVisibility(View.VISIBLE);
                listingsRecyclerView.setAdapter(listingAdapter);
            } else {
                noListing.setVisibility(View.VISIBLE);
            }
        });
        petOwnerListingViewModel.loading.observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                loadingBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                if (isLoading) {
                    listingsRecyclerView.setVisibility(View.GONE);
                }
            }
        });
        petOwnerListingViewModel.ownedPets.observe(getViewLifecycleOwner(), list -> {
            if (list != null) {
                petSpinner.setVisibility(View.GONE);
                if (list.size() != 0) {
                    search.setEnabled(true);
                }
                String[] typeArr = new String[list.size()];
                int i = 0;
                for (Pet p : list) {
                    typeArr[i] = p.getName();
                    i++;
                }
                refreshSpinner(typeArr);
                petSpinner.setVisibility(View.VISIBLE);
            }
        });
        petOwnerListingViewModel.emptyLists.observe(getViewLifecycleOwner(), empty -> {
            if (!empty) {
                noListing.setVisibility(View.GONE);
            }
        });
        petOwnerListingViewModel.noPets.observe(getViewLifecycleOwner(), none -> {
            if (none) {
                noPets.setVisibility(View.VISIBLE);
            }
        });
    }
}