package com.example.cs2102.view.careTakerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.cs2102.R;
import com.example.cs2102.model.PetOwnerBid;
import com.example.cs2102.view.careTakerView.viewModel.BidSelectedViewModel;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BidSelectedFragment extends Fragment {

    @BindView(R.id.pet_owner_name)
    TextView petOwnerName;

    @BindView(R.id.pet_owner_pet)
    TextView pet;

    @BindView(R.id.date_avail)
    TextView dates;

    @BindView(R.id.fees)
    TextView fees;

    @BindView(R.id.payment)
    TextView payment;

    @BindView(R.id.acceptBid)
    Button acceptBid;

    @BindView(R.id.rejectBid)
    Button rejectBid;

    @BindView(R.id.loading)
    ProgressBar loadingBar;

    private BidSelectedViewModel bidSelectedViewModel;
    private BidSelectedFragmentListener bidSelectedFragmentListener;

    private static PetOwnerBid petOwner;
    private static String username;

    public interface BidSelectedFragmentListener {
        void onBidAcceptedExitFragment();
    }

    public void setBidSelectedFragmentListener(BidSelectedFragmentListener listenerImpl) {
        this.bidSelectedFragmentListener = listenerImpl;
    }

    public static BidSelectedFragment newInstance(String uName, PetOwnerBid current) {
        petOwner = current;
        username = uName;
        return new BidSelectedFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bid_selected_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        bidSelectedViewModel = ViewModelProviders.of(this).get(BidSelectedViewModel.class);
        petOwnerName.setText(String.format("Pet Owner: %s", petOwner.getPetOwner()));
        pet.setText(String.format("Pet: %s, Type: %s", petOwner.getPetName(), petOwner.getPetType()));
        dates.setText(String.format("Date: %s to %s", petOwner.getAvailability(), petOwner.getEndDate()));
        fees.setText(String.format("Fee per day: $%s", petOwner.getPrice()));
        payment.setText(String.format("Payment by: %s", petOwner.getPayment()));

        acceptBid.setOnClickListener(v -> {
            bidSelectedViewModel.acceptRejectBid(petOwner.getPetOwner(), petOwner.getPetName(), username, petOwner.getAvailability(), "a");
        });

        rejectBid.setOnClickListener(v -> {
            bidSelectedViewModel.acceptRejectBid(petOwner.getPetOwner(), petOwner.getPetName(), username, petOwner.getAvailability(), "r");
        });

        bidSelectedViewModel.loading.setValue(false);
        bidSelectedViewModel.acceptedBid.setValue(false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        bidsSelectedVMObserver();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    private void bidsSelectedVMObserver() {
        bidSelectedViewModel.loading.observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean) {
                loadingBar.setVisibility(View.VISIBLE);
            } else {
                loadingBar.setVisibility(View.GONE);
            }
        });
        bidSelectedViewModel.acceptedBid.observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean) {
                bidSelectedFragmentListener.onBidAcceptedExitFragment();
            }
        });
    }
}