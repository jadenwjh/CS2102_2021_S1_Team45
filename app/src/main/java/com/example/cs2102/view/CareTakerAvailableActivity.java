package com.example.cs2102.view;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.cs2102.R;
import com.example.cs2102.viewModel.CareTakerAvailableVM;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CareTakerAvailableActivity extends AppCompatActivity {

    @BindView(R.id.careTakerList)
    RecyclerView careTakerRecyclerView;

    @BindView(R.id.careTakerError)
    TextView listError;

    @BindView(R.id.careTakerLoading)
    ProgressBar loadingView;

    @BindView(R.id.careTakerRefresh)
    SwipeRefreshLayout refreshLayout;

    private CareTakerAvailableVM careTakerViewModel;
    private CareTakerAdapter careTakerAdapter = new CareTakerAdapter(new ArrayList<>());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_care_taker);

        ButterKnife.bind(this);

        careTakerRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        careTakerRecyclerView.setAdapter(careTakerAdapter);

        refreshLayout.setOnRefreshListener(() -> {
            careTakerViewModel.refreshPage();
            refreshLayout.setRefreshing(false);
        });

        observerViewModel();
    }

    private void observerViewModel() {
        careTakerViewModel.careTakers.observe(this, careTakers -> {
            if (careTakers != null) {
                careTakerRecyclerView.setVisibility(View.VISIBLE);
                careTakerAdapter.updateCareTakers(careTakers);
            }
        });
        careTakerViewModel.loadError.observe(this, isError -> {
            if (isError != null) {
                listError.setVisibility(isError ? View.VISIBLE : View.GONE);
            }
        });
        careTakerViewModel.loading.observe(this, isLoading -> {
            if (isLoading != null) {
                loadingView.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                if(isLoading) {
                    listError.setVisibility(View.GONE);
                    careTakerRecyclerView.setVisibility(View.GONE);
                }
            }
        });
    }
}