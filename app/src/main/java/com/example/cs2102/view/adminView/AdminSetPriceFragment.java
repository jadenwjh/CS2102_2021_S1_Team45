package com.example.cs2102.view.adminView;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.example.cs2102.R;
import com.example.cs2102.view.adminView.viewModel.AdminSetPriceViewModel;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AdminSetPriceFragment extends Fragment {

    @BindView(R.id.pet_list)
    RecyclerView petList;

    @BindView(R.id.pet_type)
    EditText petType;

    @BindView(R.id.price)
    EditText price;

    @BindView(R.id.set_price)
    Button setPrice;

    @BindView(R.id.loading)
    ProgressBar loadingBar;

    private AdminSetPriceViewModel adminSetPriceViewModel;
    private PetPriceAdapter petPriceAdapter = new PetPriceAdapter(new ArrayList<>());

    public static AdminSetPriceFragment newInstance() {
        return new AdminSetPriceFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.admin_set_price_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        adminSetPriceViewModel = new ViewModelProvider(this).get(AdminSetPriceViewModel.class);

        petList.setLayoutManager(new LinearLayoutManager(view.getContext()));

        setPrice.setOnClickListener(v -> {
            String type = petType.getText().toString();
            String amount = price.getText().toString();
            if (type.trim().length() != 0 && amount.trim().length() != 0) {
                float amountFloat = Float.parseFloat(price.getText().toString());
                if (amountFloat > 0) {
                    adminSetPriceViewModel.updateBasePrice(type, amountFloat, getContext());
                }
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setPriceObserver();
        adminSetPriceViewModel.fetchBasePrices();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    private void setPriceObserver() {
        adminSetPriceViewModel.petBasePrices.observe(getViewLifecycleOwner(), list -> {
            petType.getText().clear();
            price.getText().clear();
            if (list.size() != 0) {
                petPriceAdapter.updatePetPrices(list);
                petList.setAdapter(petPriceAdapter);
                petList.setVisibility(View.VISIBLE);
            }
        });
        adminSetPriceViewModel.loading.observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                loadingBar.setVisibility(View.VISIBLE);
                petList.setVisibility(View.GONE);
            } else {
                loadingBar.setVisibility(View.GONE);
            }
        });
        adminSetPriceViewModel.updatedPrice.observe(getViewLifecycleOwner(), updated -> {
            if (updated) {
                adminSetPriceViewModel.fetchBasePrices();
            }
        });
    }
}