package com.example.cs2102.view.adminView;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.cs2102.R;
import com.example.cs2102.view.adminView.viewModel.AdminSetPriceViewModel;

public class AdminSetPriceFragment extends Fragment {

    private AdminSetPriceViewModel adminSetPriceViewModel;

    public static AdminSetPriceFragment newInstance() {
        return new AdminSetPriceFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.admin_set_price_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adminSetPriceViewModel = new ViewModelProvider(this).get(AdminSetPriceViewModel.class);
        // TODO: Use the ViewModel
    }

}