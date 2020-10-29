package com.example.cs2102.view.careTakerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.cs2102.R;
import com.example.cs2102.view.careTakerView.viewModel.CareTakerBidsViewModel;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CareTakerBidsFragment extends Fragment {

    @BindView(R.id.careTakerBidsReceived)
    RecyclerView bidsRecyclerView;

    @BindView(R.id.careTakerBidsError)
    TextView listError;

    @BindView(R.id.careTakerBidsLoading)
    ProgressBar loadingView;

    @BindView(R.id.careTakerBidsRefresh)
    SwipeRefreshLayout refreshLayout;

    private static String currentCareTakerUsername;

    private CareTakerBidsViewModel bidsVM;
    private CareTakerBidsAdapter careTakerBidsAdapter = new CareTakerBidsAdapter(new ArrayList<>());

    public static CareTakerBidsFragment newInstance(String username) {
        currentCareTakerUsername = username;
        return new CareTakerBidsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.care_taker_bids_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        bidsRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        bidsRecyclerView.setAdapter(careTakerBidsAdapter);

        refreshLayout.setOnRefreshListener(() -> {
            bidsVM.refreshBids(currentCareTakerUsername);
            refreshLayout.setRefreshing(false);
        });

        bidsVM.loading.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    loadingView.setVisibility(View.VISIBLE);
                } else {
                    loadingView.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        bidsVM = ViewModelProviders.of(this).get(CareTakerBidsViewModel.class);
        bidsVMObserver();
        bidsVM.refreshBids(currentCareTakerUsername);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void bidsVMObserver() {
        bidsVM.petOwners.observe(getViewLifecycleOwner(), petOwners -> {
            if (petOwners != null) {
                bidsRecyclerView.setVisibility(View.VISIBLE);
                careTakerBidsAdapter.updatePetOwners(petOwners);
            }
        });
        bidsVM.loadError.observe(getViewLifecycleOwner(), isError -> {
            if (isError != null) {
                listError.setVisibility(isError ? View.VISIBLE : View.GONE);
            }
        });
        bidsVM.loading.observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                loadingView.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                if(isLoading) {
                    listError.setVisibility(View.GONE);
                    bidsRecyclerView.setVisibility(View.GONE);
                }
            }
        });
    }
}