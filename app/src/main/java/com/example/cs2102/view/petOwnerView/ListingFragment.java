package com.example.cs2102.view.petOwnerView;

import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.example.cs2102.R;
import com.example.cs2102.model.Listing;
import com.example.cs2102.view.petOwnerView.viewModel.ListingViewModel;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ListingFragment extends Fragment {

    @BindView(R.id.get_own_pets)
    Spinner allPets;

    @BindView(R.id.date_range_listing)
    TextView dates;

    @BindView(R.id.price_listing)
    TextView price;

    @BindView(R.id.listing_care_taker_name)
    TextView careTaker;

    @BindView(R.id.listing_payment)
    Spinner paymentType;

    @BindView(R.id.submit_bid)
    Button submitBid;

    @BindView(R.id.loading)
    ProgressBar loadingBar;

    private ListingViewModel listingViewModel;
    private static Listing listing;
    private static String username;

    public interface ListingSelectedListener {
        void onListingSubmittedExitFragment();
    }

    private ListingSelectedListener listingSelectedListener;

    public void setListingSelectedListener(ListingSelectedListener listenerImpl) {
        this.listingSelectedListener = listenerImpl;
    }

    public static ListingFragment newInstance(String name, Listing list) {
        username = name;
        listing = list;
        return new ListingFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.listing_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        listingViewModel = ViewModelProviders.of(this).get(ListingViewModel.class);

        careTaker.setText(String.format("Care Taker: %s", listing.getCareTaker()));
        dates.setText(String.format("Dates: %s - %s", listing.getStartDate(), listing.getEndDate()));
        price.setText(String.format("Price: $%s", listing.getPrice()));

        submitBid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String payment = paymentType.getSelectedItem().toString();
                String petName = allPets.getSelectedItem().toString();
                if (!payment.equals("") && !petName.equals("")) {
                    listingViewModel.submitBid(
                            username,
                            petName,
                            listing.getCareTaker(),
                            listing.getStartDate(),
                            listing.getEndDate(),
                            payment,
                            Float.parseFloat(listing.getPrice()));
                }
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listingSelectedObserver();
        generatePaymentTypes();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    private void refreshSpinner(String[] arr) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, arr);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        allPets.setAdapter(adapter);
    }

    public void generatePaymentTypes() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.payment_selection, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        paymentType.setAdapter(adapter);
    }

    private void listingSelectedObserver() {
        listingViewModel.bidSubmitted.observe(getViewLifecycleOwner(), submitted -> {
            if (submitted) {
                listingSelectedListener.onListingSubmittedExitFragment();
            }
        });
        listingViewModel.loading.observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean) {
                loadingBar.setVisibility(View.VISIBLE);
            } else {
                loadingBar.setVisibility(View.GONE);
            }
        });
        listingViewModel.ownedPets.observe(getViewLifecycleOwner(), nameArr -> {
            if (nameArr != null) {
                allPets.setVisibility(View.GONE);
                refreshSpinner(nameArr);
                allPets.setVisibility(View.VISIBLE);
            }
        });
    }
}