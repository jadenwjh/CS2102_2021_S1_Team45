package com.example.cs2102.view;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.cs2102.R;
import com.example.cs2102.viewModel.CareTakerHomepageVM;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CareTakerHomepage extends AppCompatActivity {

    @BindView(R.id.petOwnerList)
    RecyclerView petOwnerRecyclerView;

    @BindView(R.id.petOwnerError)
    TextView listError;

    @BindView(R.id.petOwnerLoading)
    ProgressBar loadingView;

    @BindView(R.id.petOwnerRefresh)
    SwipeRefreshLayout refreshLayout;

    private CareTakerHomepageVM petOwnerViewModel;
    private PetOwnerAdapter petOwnerAdapter = new PetOwnerAdapter(new ArrayList<>());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_owner);

        ButterKnife.bind(this);

        petOwnerViewModel = ViewModelProviders.of(this).get(CareTakerHomepageVM.class);
        petOwnerViewModel.refreshPage();

        petOwnerRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        petOwnerRecyclerView.setAdapter(petOwnerAdapter);

        refreshLayout.setOnRefreshListener(() -> {
            petOwnerViewModel.refreshPage();
            refreshLayout.setRefreshing(false);
        });

        observerViewModel();
    }

    private void observerViewModel() {
        petOwnerViewModel.petOwners.observe(this, petOwners -> {
            if (petOwners != null) {
                petOwnerRecyclerView.setVisibility(View.VISIBLE);
                petOwnerAdapter.updatePetOwners(petOwners);
            }
        });
        petOwnerViewModel.loadError.observe(this, isError -> {
            if (isError != null) {
                listError.setVisibility(isError ? View.VISIBLE : View.GONE);
            }
        });
        petOwnerViewModel.loading.observe(this, isLoading -> {
            if (isLoading != null) {
                loadingView.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                if(isLoading) {
                    listError.setVisibility(View.GONE);
                    petOwnerRecyclerView.setVisibility(View.GONE);
                }
            }
        });
    }
}