package com.example.cs2102.view.careTakerView;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import com.example.cs2102.model.PetTypeCost;
import com.example.cs2102.view.careTakerView.viewModel.CareTakerSetPriceViewModel;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CareTakerSetPriceFragment extends Fragment {

    @BindView(R.id.careTakerPricesRefresh)
    SwipeRefreshLayout refreshLayout;

    @BindView(R.id.careTakerPricesError)
    TextView listError;

    @BindView(R.id.careTakerNoTypes)
    TextView noTypesMsg;

    @BindView(R.id.careTakerPricesLoading)
    ProgressBar loadingView;

    @BindView(R.id.all_pets)
    RecyclerView pricesRecyclerView;

    @BindView(R.id.editPrice)
    EditText setPrice;

    @BindView(R.id.setPrice)
    Button confirmSet;

    private static String currentCareTakerUsername;

    private CareTakerSetPriceViewModel pricesVM;
    private CareTakerSetPriceAdapter careTakerSetPriceAdapter = new CareTakerSetPriceAdapter(new ArrayList<>());

    public static CareTakerSetPriceFragment newInstance(String username) {
        currentCareTakerUsername = username;
        return new CareTakerSetPriceFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.care_taker_set_price_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        pricesVM = ViewModelProviders.of(this).get(CareTakerSetPriceViewModel.class);
        pricesVM.loading.setValue(false);

        pricesRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        pricesRecyclerView.setAdapter(careTakerSetPriceAdapter);

        careTakerSetPriceAdapter.setPricesListener(new CareTakerSetPriceAdapter.PricesListener() {
            @Override
            public void onPriceCardSelected(PetTypeCost petTypeCost) {
                // set prices for edit text (Upper and base price)
                setPrice.setText(petTypeCost.getFee());
            }
        });

        refreshLayout.setOnRefreshListener(() -> {
            pricesVM.refreshPrices(currentCareTakerUsername);
            refreshLayout.setRefreshing(false);
        });

        confirmSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //set price
                //refresh layout
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        petPricesVMObserver();
        pricesVM.refreshPrices(currentCareTakerUsername);
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

    private void petPricesVMObserver() {
        pricesVM.petTypeCosts.observe(getViewLifecycleOwner(), petPrices -> {
            noTypesMsg.setVisibility(View.GONE);
            if (petPrices != null) {
                pricesRecyclerView.setVisibility(View.VISIBLE);
                careTakerSetPriceAdapter.updatePetPrices(petPrices);
                pricesRecyclerView.setVisibility(View.VISIBLE);
            } else {
                Log.e("petPricesVMObserver", "You have no prices");
                noTypesMsg.setVisibility(View.VISIBLE);
            }
        });
        pricesVM.loadError.observe(getViewLifecycleOwner(), isError -> {
            if (isError != null) {
                listError.setVisibility(isError ? View.VISIBLE : View.GONE);
            }
        });
        pricesVM.loading.observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                loadingView.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                if(isLoading) {
                    listError.setVisibility(View.GONE);
                    pricesRecyclerView.setVisibility(View.GONE);
                }
            }
        });
    }
}