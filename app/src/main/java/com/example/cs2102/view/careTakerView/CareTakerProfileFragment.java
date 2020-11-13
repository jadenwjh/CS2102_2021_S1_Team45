package com.example.cs2102.view.careTakerView;

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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.cs2102.R;
import com.example.cs2102.model.UserProfile;
import com.example.cs2102.view.careTakerView.viewModel.CareTakerProfileViewModel;
import com.example.cs2102.view.petOwnerView.CTReviewViewAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CareTakerProfileFragment extends Fragment {

    @BindView(R.id.username)
    TextView username;

    @BindView(R.id.email)
    TextView email;

    @BindView(R.id.phoneNum)
    TextView phone;

    @BindView(R.id.address)
    TextView address;

    @BindView(R.id.profile)
    TextView profile;

    @BindView(R.id.contract)
    TextView contract;

    @BindView(R.id.salary)
    TextView salary;

    @BindView(R.id.petdays)
    TextView petdays;

    @BindView(R.id.rating)
    TextView rating;

    @BindView(R.id.reviewList)
    RecyclerView reviewList;

    @BindView(R.id.loading)
    ProgressBar loadingBar;

    @BindView(R.id.noReview)
    TextView noReview;

    private CareTakerProfileViewModel careTakerProfileViewModel;
    private CTReviewViewAdapter reviewAdapter = new CTReviewViewAdapter(new ArrayList<>());
    private static UserProfile petOwner = UserProfile.getInstance();
    private static String date = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());

    public static CareTakerProfileFragment newInstance() {
        return new CareTakerProfileFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.care_taker_profile_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        careTakerProfileViewModel = new ViewModelProvider(this).get(CareTakerProfileViewModel.class);
        ButterKnife.bind(this, view);
        noReview.setVisibility(View.GONE);
        careTakerProfileViewModel.fetchData(petOwner.username, date);

        reviewList.setLayoutManager(new LinearLayoutManager(view.getContext()));
        careTakerProfileViewModel.reviews.observe(getViewLifecycleOwner(), reviews -> {
            if (reviews != null) {
                reviewAdapter.updateReviewList(reviews);
            }
            reviewList.setAdapter(reviewAdapter);
            if (reviews.size() == 0) {
                noReview.setVisibility(View.VISIBLE);
            }
        });

        username.setText(String.format("PetOwner Username: %s", petOwner.username));
        email.setText(String.format("Email: %s", petOwner.email));
        phone.setText(String.format("Phone number: %s", petOwner.phoneNum));
        address.setText(String.format("Address: %s", petOwner.address));
        profile.setText(String.format("Profile: %s", petOwner.profile));

        careTakerProfileViewModel.stats.observe(getViewLifecycleOwner(), stats -> {
            String con = stats.get("contract").toString();
            double sal = (double) stats.get("salary");
            String day = stats.get("petdaysclocked").toString();
            String avg = stats.get("avgrating") != null ? stats.get("avgrating").toString().substring(0,3) : "Currently not rated";
            String num = stats.get("numratings").toString();

            contract.setText(String.format("Contract type: %s", con));
            salary.setText(String.format("Expected earning this month: $%s", Double.toString(sal)));
            petdays.setText(String.format("Pet Days accumulated: %s", day));
            rating.setText(avg.equals("Currently not rated") ? "Currently not rated" : String.format("Average Rating: %s from %s users", avg, num));
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        profileObserver();
    }

    private void profileObserver() {
        careTakerProfileViewModel.loadingStats.observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                loadingBar.setVisibility(View.VISIBLE);
            } else {
                loadingBar.setVisibility(View.GONE);
            }
        });
    }
}