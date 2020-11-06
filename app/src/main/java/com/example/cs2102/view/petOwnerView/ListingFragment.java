package com.example.cs2102.view.petOwnerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cs2102.R;
import com.example.cs2102.model.Listing;
import com.example.cs2102.model.Pet;
import com.example.cs2102.view.petOwnerView.viewModel.ListingViewModel;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ListingFragment extends Fragment {

    @BindView(R.id.date_range_listing)
    TextView dates;

    @BindView(R.id.price_listing)
    TextView price;

    @BindView(R.id.listing_care_taker_name)
    TextView careTaker;

    @BindView(R.id.listing_payment)
    Spinner paymentType;

    @BindView(R.id.listing_transfer)
    EditText transferType;

    @BindView(R.id.bid_price)
    EditText inputBidPrice;

    @BindView(R.id.submit_bid)
    Button submitBid;

    @BindView(R.id.loading)
    ProgressBar loadingBar;

    @BindView(R.id.current_pet_name)
    TextView currentPetName;

    @BindView(R.id.current_pet_type)
    TextView currentPetType;

    @BindView(R.id.reviewList)
    RecyclerView reviewList;

    private ListingViewModel listingViewModel;
    private static Listing listing;
    private static String username;
    private static Pet petForBid;

    private ReviewAdapter reviewAdapter = new ReviewAdapter(new ArrayList<>());

    public interface ListingSelectedListener {
        void onListingSubmittedExitFragment();
    }

    private ListingSelectedListener listingSelectedListener;

    public void setListingSelectedListener(ListingSelectedListener listenerImpl) {
        this.listingSelectedListener = listenerImpl;
    }

    public static ListingFragment newInstance(String name, Listing list, Pet pet) {
        username = name;
        listing = list;
        petForBid = pet;
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

        reviewList.setLayoutManager(new LinearLayoutManager(view.getContext()));
        listingViewModel.fetchReviews(listing.getCareTaker());
        listingViewModel.reviews.observe(getViewLifecycleOwner(), reviewFetched -> {
            if (reviewFetched != null) {
                reviewAdapter.updateReviewList(reviewFetched);
            }
            reviewList.setAdapter(reviewAdapter);
        });

        loadingBar.setVisibility(View.GONE);

        careTaker.setText(String.format("Care Taker: %s", listing.getCareTaker()));
        dates.setText(String.format("Dates: %s - %s", listing.getStartDate(), listing.getEndDate()));
        price.setText(String.format("Minimum Bid Price: $%s", listing.getPrice()));
        currentPetName.setText(String.format("Pet Name: %s", petForBid.getName()));
        currentPetType.setText(String.format("Category: %s", petForBid.getType()));

        submitBid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String t = transferType.getText().toString();
                if (!petForBid.getName().equals("") && t.trim().length() != 0) {
                    String payment = paymentType.getSelectedItem().toString();
                    listingViewModel.submitBid(
                            username,
                            petForBid.getName(),
                            listing.getCareTaker(),
                            listing.getStartDate(),
                            listing.getEndDate(),
                            payment,
                            inputBidPrice.getText().toString().length() == 0 ? Float.parseFloat(listing.getPrice()) : Float.parseFloat(inputBidPrice.getText().toString()),
                            t,
                            getContext());
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
    }
}