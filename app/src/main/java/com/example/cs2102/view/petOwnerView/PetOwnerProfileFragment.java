package com.example.cs2102.view.petOwnerView;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.cs2102.R;
import com.example.cs2102.model.UserProfile;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PetOwnerProfileFragment extends Fragment {

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

    private static UserProfile petOwner = UserProfile.getInstance();

    public static PetOwnerProfileFragment newInstance() {
        return new PetOwnerProfileFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.pet_owner_profile_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        username.setText(String.format("PetOwner Username: %s", petOwner.username));
        email.setText(String.format("Email: %s", petOwner.email));
        phone.setText(String.format("Phone number: %s", petOwner.phoneNum));
        address.setText(String.format("Address: %s", petOwner.address));
        profile.setText(String.format("Profile: %s", petOwner.profile));
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }
}