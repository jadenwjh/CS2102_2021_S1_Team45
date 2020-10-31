package com.example.cs2102.view.careTakerView;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.cs2102.R;
import com.example.cs2102.model.PetOwnerBid;
import com.example.cs2102.view.careTakerView.viewModel.CareTakerBidsViewModel;
import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CareTakerBidsFragment extends Fragment {

    @BindView(R.id.careTakerBidsReceived)
    RecyclerView bidsRecyclerView;

    @BindView(R.id.careTakerBidsError)
    TextView listError;

    @BindView(R.id.careTakerNoBids)
    TextView noBidsMsg;

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

    private CareTakerBidsFragmentListener careTakerBidsFragmentListener;

    public interface CareTakerBidsFragmentListener {
        void onBidSelectedFromBidsFragment(BidSelectedFragment selectedBid);
    }

    public void setCareTakerBidsFragmentListener(CareTakerBidsFragmentListener listenerImpl) {
        this.careTakerBidsFragmentListener = listenerImpl;
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
        bidsVM = ViewModelProviders.of(this).get(CareTakerBidsViewModel.class);
        bidsVM.loading.setValue(false);

        bidsRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        bidsRecyclerView.setAdapter(careTakerBidsAdapter);

        careTakerBidsAdapter.setBidsListener(petOwner -> {
            BidSelectedFragment currentBid = BidSelectedFragment.newInstance(currentCareTakerUsername, petOwner);
            careTakerBidsFragmentListener.onBidSelectedFromBidsFragment(currentBid);
        });

        refreshLayout.setOnRefreshListener(() -> {
            bidsVM.refreshBids(currentCareTakerUsername);
            refreshLayout.setRefreshing(false);
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
            noBidsMsg.setVisibility(View.GONE);
            if (petOwners != null) {
                List<PetOwnerBid> bids = new ArrayList<>();
                for (LinkedTreeMap<String,String> petOwner : petOwners) {
                    String petOwnerName = petOwner.get("petowner");
                    String petName = petOwner.get("petname");
//                    String petType = petOwner.get("pettype");
                    String avail = petOwner.get("avail").substring(0,10);
                    PetOwnerBid petOwnerBid = new PetOwnerBid(petOwnerName, petName, "petType", avail);
                    bids.add(petOwnerBid);
                    Log.e("bidsVMObserver", "Added petOwner" + petOwnerName);
                }
                careTakerBidsAdapter.updatePetOwners(bids);
                bidsRecyclerView.setVisibility(View.VISIBLE);
            } else {
                Log.e("bidsVMObserver", "You have no bids");
                noBidsMsg.setVisibility(View.VISIBLE);
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
                if (isLoading) {
                    listError.setVisibility(View.GONE);
                    bidsRecyclerView.setVisibility(View.GONE);
                }
            }
        });
    }
}