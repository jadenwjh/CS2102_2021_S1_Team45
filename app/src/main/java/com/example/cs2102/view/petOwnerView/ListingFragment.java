package com.example.cs2102.view.petOwnerView;

import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.cs2102.R;
import com.example.cs2102.model.Listing;
import com.example.cs2102.view.petOwnerView.viewModel.ListingViewModel;

import butterknife.ButterKnife;

public class ListingFragment extends Fragment {

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

        //set text

        //onclicks
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listingSelectedObserver();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    private void listingSelectedObserver() {

    }
}