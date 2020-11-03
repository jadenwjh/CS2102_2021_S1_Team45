package com.example.cs2102.view.petOwnerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.cs2102.R;
import com.example.cs2102.model.CareTakerBid;
import com.example.cs2102.view.petOwnerView.viewModel.ReviewViewModel;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReviewFragment extends Fragment {

    @BindView(R.id.care_taker)
    TextView caretaker;

    @BindView(R.id.pet_name)
    TextView petname;

    @BindView(R.id.date)
    TextView dateRange;

    @BindView(R.id.price)
    TextView priceText;

    @BindView(R.id.transfer)
    TextView transferType;

    @BindView(R.id.payment)
    TextView paymentType;

    @BindView(R.id.isPaid)
    Switch hasPaid;

    @BindView(R.id.rating)
    Spinner setRating;

    @BindView(R.id.review)
    EditText reviewText;

    @BindView(R.id.submit_review)
    Button submitReview;

    @BindView(R.id.loading)
    ProgressBar loadingBar;

    private ReviewViewModel reviewViewModel;

    private static CareTakerBid careTakerBid;

    public static ReviewFragment newInstance(CareTakerBid bid) {
        careTakerBid = bid;
        return new ReviewFragment();
    }

    public interface ExitReviewFragmentCallback {
        void onExitReview();
    }

    private ExitReviewFragmentCallback exitReviewFragmentCallback; //exit after button press

    public void setExitReviewFragmentCallback(ExitReviewFragmentCallback callback) {
        this.exitReviewFragmentCallback = callback;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.review_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        loadingBar.setVisibility(View.VISIBLE);
        reviewViewModel = new ViewModelProvider(this).get(ReviewViewModel.class);
        generateRatings();

        caretaker.setText(String.format("Care Taker: %s", careTakerBid.getCareTaker()));
        petname.setText(String.format("Pet name: %s", careTakerBid.getPetName()));
        dateRange.setText(String.format("Date: %s - %s", careTakerBid.getStartDate(), careTakerBid.getEndDate()));
        priceText.setText(String.format("Price: $%s", careTakerBid.getPrice()));
        transferType.setText(String.format("Transfer type: %s", careTakerBid.getTransfer()));
        paymentType.setText(String.format("Payment type: %s", careTakerBid.getPayment()));

        submitReview.setOnClickListener(v -> {
            boolean isPaid = hasPaid.isChecked();
            String writtenReview = reviewText.getText().toString();
            int rate = Integer.parseInt(setRating.getSelectedItem().toString());
            if (isPaid && writtenReview.trim().length() != 0 && rate != 0) {
                reviewViewModel.submitReview(
                        careTakerBid.getCareTaker(),
                        careTakerBid.getPetName(),
                        careTakerBid.getCareTaker(),
                        careTakerBid.getStartDate(),
                        rate,
                        writtenReview,
                        getContext(),
                        true
                );
                exitReviewFragmentCallback.onExitReview();
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        reviewObserver();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    private void generateRatings() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.user_rating, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        setRating.setAdapter(adapter);
    }

    private void reviewObserver() {
        reviewViewModel.reviewSubmitted.observe(getViewLifecycleOwner(), submitted -> {
            if (submitted) {
                exitReviewFragmentCallback.onExitReview();
            }
        });
        reviewViewModel.loading.observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                loadingBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });
    }
}