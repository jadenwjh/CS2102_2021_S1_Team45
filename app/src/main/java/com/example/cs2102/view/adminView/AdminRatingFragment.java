package com.example.cs2102.view.adminView;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.cs2102.R;
import com.example.cs2102.view.adminView.viewModel.AdminRatingViewModel;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AdminRatingFragment extends Fragment {

    @BindView(R.id.refreshListing)
    SwipeRefreshLayout refreshLayout;

    @BindView(R.id.ratingList)
    RecyclerView ratings;

    @BindView(R.id.errorListing)
    TextView error;

    @BindView(R.id.loading)
    ProgressBar loadingBar;

    private AdminRatingViewModel adminRatingViewModel;
    private RatingAdapter ratingAdapter = new RatingAdapter(new ArrayList<>());
    private static String adminUsername;

    public static AdminRatingFragment newInstance(String username) {
        adminUsername = username;
        return new AdminRatingFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.admin_rating_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        error.setVisibility(View.GONE);
        loadingBar.setVisibility(View.GONE);
        adminRatingViewModel = new ViewModelProvider(this).get(AdminRatingViewModel.class);

        ratings.setLayoutManager(new LinearLayoutManager(view.getContext()));

        refreshLayout.setOnRefreshListener(() -> {
            adminRatingViewModel.fetchRating(adminUsername);
            refreshLayout.setRefreshing(false);
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ratingObserver();
        adminRatingViewModel.fetchRating(adminUsername);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    private void ratingObserver() {
        adminRatingViewModel.loading.observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                loadingBar.setVisibility(View.VISIBLE);
                ratings.setVisibility(View.GONE);
            } else {
                loadingBar.setVisibility(View.GONE);
            }
        });
        adminRatingViewModel.ratings.observe(getViewLifecycleOwner(), list -> {
            if (list.size() != 0) {
                ratingAdapter.updateRatings(list);
                ratings.setAdapter(ratingAdapter);
                ratings.setVisibility(View.VISIBLE);
            }
        });
    }
}