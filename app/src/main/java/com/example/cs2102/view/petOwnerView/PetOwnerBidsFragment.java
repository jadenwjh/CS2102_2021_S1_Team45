package com.example.cs2102.view.petOwnerView;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cs2102.R;
import com.example.cs2102.model.CareTakerBid;
import com.example.cs2102.view.petOwnerView.viewModel.PetOwnerBidsViewModel;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PetOwnerBidsFragment extends Fragment {

    @BindView(R.id.loading)
    ProgressBar loadingBar;

    @BindView(R.id.ongoing)
    Button ongoingButton;

    @BindView(R.id.for_review)
    Button historyButton;

    @BindView(R.id.bids_recycler_view)
    RecyclerView bidsRecyclerView;

    private PetOwnerBidsViewModel petOwnerBidsViewModel;
    public PetOwnerBidsAdapter petOwnerBidsAdapter = new PetOwnerBidsAdapter(new ArrayList<>());

    private static String petOwnerUsername;

    public static PetOwnerBidsFragment newInstance(String username) {
        petOwnerUsername = username;
        return new PetOwnerBidsFragment();
    }

    public interface BidsFragmentReviewListener {
        void exitReviewFragment(ReviewFragment review);
    }

    private BidsFragmentReviewListener bidsFragmentReviewListener;

    public void setBidsFragmentReviewListener(BidsFragmentReviewListener impl) {
        this.bidsFragmentReviewListener = impl;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.pet_owner_bids_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        petOwnerBidsViewModel = new ViewModelProvider(this).get(PetOwnerBidsViewModel.class);
        petOwnerBidsViewModel.loading.setValue(false);
        historyButton.setBackgroundColor(Color.BLACK);
        ongoingButton.setBackgroundColor(Color.BLACK);


        bidsRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        bidsRecyclerView.setAdapter(petOwnerBidsAdapter);

        petOwnerBidsAdapter.setReviewListener(new PetOwnerBidsAdapter.ReviewListener() {
            @Override
            public void onStartReview(CareTakerBid currentBid) {
                ReviewFragment reviewFragment = ReviewFragment.newInstance(currentBid);
                bidsFragmentReviewListener.exitReviewFragment(reviewFragment);
                historyButton.setBackgroundColor(Color.CYAN);
            }
        });

        ongoingButton.setOnClickListener(v -> {
            petOwnerBidsViewModel.fetchOngoingBids(petOwnerUsername);
            ongoingButton.setBackgroundColor(Color.CYAN);
            historyButton.setBackgroundColor(Color.BLACK);
        });

        historyButton.setOnClickListener(v -> {
            petOwnerBidsViewModel.fetchExpiredBids(petOwnerUsername);
            ongoingButton.setBackgroundColor(Color.BLACK);
            historyButton.setBackgroundColor(Color.CYAN);
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        bidsFragmentObserver();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    private void bidsFragmentObserver() {
        petOwnerBidsViewModel.ongoingBids.observe(getViewLifecycleOwner(), bidsList -> {
            if (bidsList != null) {
                petOwnerBidsAdapter.updateBidsList(bidsList);
                bidsRecyclerView.setVisibility(View.VISIBLE);
                bidsRecyclerView.setAdapter(petOwnerBidsAdapter);
            } else {
                Toast.makeText(getContext(), "You have no ongoing bids", Toast.LENGTH_SHORT).show();
            }
        });
        petOwnerBidsViewModel.expiredBids.observe(getViewLifecycleOwner(), bidsList -> {
            if (bidsList != null) {
                petOwnerBidsAdapter.updateBidsList(bidsList);
                bidsRecyclerView.setVisibility(View.VISIBLE);
                bidsRecyclerView.setAdapter(petOwnerBidsAdapter);
            } else {
                Toast.makeText(getContext(), "You have no accepted bids", Toast.LENGTH_SHORT).show();
            }
        });
        petOwnerBidsViewModel.loading.observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                loadingBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                if (isLoading) {
                    bidsRecyclerView.setVisibility(View.GONE);
                }
            }
        });
    }
}