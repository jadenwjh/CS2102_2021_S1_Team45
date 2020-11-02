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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cs2102.R;
import com.example.cs2102.view.petOwnerView.viewModel.PetsViewModel;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PetsFragment extends Fragment {

    @BindView(R.id.add_pet)
    Button addPet;

    @BindView(R.id.mod_pet)
    Button modPet;

    @BindView(R.id.delete_pet)
    Button deletePet;

    @BindView(R.id.pet_name)
    EditText name;

    @BindView(R.id.pet_profile)
    EditText profile;

    @BindView(R.id.pet_needs)
    EditText request;

    @BindView(R.id.hints)
    TextView hints;

    @BindView(R.id.pet_type)
    Spinner petType;

    @BindView(R.id.loading)
    ProgressBar loadingBar;

    @BindView(R.id.pets)
    RecyclerView petsRecyclerView;

    private PetsViewModel petsViewModel;
    private PetsAdapter petsAdapter; // = new PetsAdapter
    private static String petOwnerUsername;

    public static PetsFragment newInstance(String username) {
        petOwnerUsername = username;
        return new PetsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.pets_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        petsViewModel = ViewModelProviders.of(this).get(PetsViewModel.class);

        addPet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String petName = name.getText().toString();
                String petProfile = profile.getText().toString();
                String needs = request.getText().toString();
                String type = petType.getSelectedItem().toString();
                petsViewModel.addPet(petOwnerUsername, petName, type, petProfile, needs);
                petsViewModel.refreshPage(petOwnerUsername);
            }
        });

        modPet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String petName = name.getText().toString();
                String petProfile = profile.getText().toString();
                String needs = request.getText().toString();
                String type = petType.getSelectedItem().toString();
                petsViewModel.updatePet(petOwnerUsername, petName, type, petProfile, needs);
                petsViewModel.refreshPage(petOwnerUsername);
            }
        });

        deletePet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String petName = name.getText().toString();
                petsViewModel.deletePet(petOwnerUsername, petName);
                petsViewModel.refreshPage(petOwnerUsername);
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        petObserver();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    private void petObserver() {
        petsViewModel.allPets.observe(getViewLifecycleOwner(), petsArr -> {
            if (petsArr != null) {
                generateTypes(petsArr);
            }
        });
        petsViewModel.ownedPets.observe(getViewLifecycleOwner(), pets -> {
            if (pets != null && pets.size() != 0) {
                petsAdapter.updatePets(pets);
                petsRecyclerView.setVisibility(View.VISIBLE);
                petsRecyclerView.setAdapter(petsAdapter);
            }
        });
        petsViewModel.loading.observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                loadingBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                if (isLoading) {
                    petsRecyclerView.setVisibility(View.GONE);
                }
            }
        });
        petsViewModel.showError.observe(getViewLifecycleOwner(), error -> {
            if (error != null && error) {
                Toast.makeText(getContext(), "Error modifying your pets, check if you filled parameters correctly", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generateTypes(String[] arr) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, arr);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        petType.setAdapter(adapter);
    }
}