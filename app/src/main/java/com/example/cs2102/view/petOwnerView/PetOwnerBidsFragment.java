package com.example.cs2102.view.petOwnerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.cs2102.R;
import com.example.cs2102.view.petOwnerView.viewModel.PetOwnerBidsViewModel;

import butterknife.ButterKnife;

public class PetOwnerBidsFragment extends Fragment {

    private PetOwnerBidsViewModel petOwnerBidsViewModel;

    public static PetOwnerBidsFragment newInstance() {
        return new PetOwnerBidsFragment();
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
            // TODO
        });
    }
}