package com.example.cs2102.view.petOwnerView;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cs2102.R;
import com.example.cs2102.view.petOwnerView.viewModel.PetsViewModel;

import java.util.ArrayList;

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
    private PetsAdapter petsAdapter = new PetsAdapter(new ArrayList<>());
    private static String petOwnerUsername;
    private static String selectedType;

    private PetsFragmentRefreshListener refreshListener;

    public interface PetsFragmentRefreshListener {
        void refreshPetsFragment();
    }

    public void setPetsFragmentRefreshListener(PetsFragmentRefreshListener impl) {
        this.refreshListener = impl;
    }

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
        loadingBar.setVisibility(View.GONE);

        petsRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        petsRecyclerView.setAdapter(petsAdapter);

        addPet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(getActivity());
                if (!selectedType.equals("")) {
                    String petName = name.getText().toString();
                    String petProfile = profile.getText().toString();
                    String needs = request.getText().toString();
                    if (petName.trim().length() != 0) {
                        if (petProfile.trim().length() != 0 || needs.trim().length() != 0) {
                            petsViewModel.addPet(petOwnerUsername, petName, selectedType, petProfile, needs, getContext());
                            actionObserver();
                        } else {
                            Toast.makeText(getContext(), "Your pet profile and requirements cannot be blank", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Your pet name cannot be blank", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Your pet type cannot be blank", Toast.LENGTH_SHORT).show();
                }
            }
        });

        modPet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(getActivity());
                if (!selectedType.equals("")) {
                    String petName = name.getText().toString();
                    String petProfile = profile.getText().toString();
                    String needs = request.getText().toString();
                    if (petName.trim().length() != 0) {
                        if (petProfile.trim().length() != 0 || needs.trim().length() != 0) {
                            petsViewModel.updatePet(petOwnerUsername, petName, selectedType, petProfile, needs, getContext());
                            actionObserver();
                        } else {
                            Toast.makeText(getContext(), "Your pet profile and requirements cannot be blank", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Your pet name cannot be blank", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Your pet type cannot be blank", Toast.LENGTH_SHORT).show();
                }
            }
        });

        deletePet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(getActivity());
                String petName = name.getText().toString();
                if (petName.trim().length() != 0) {
                    petsViewModel.deletePet(petOwnerUsername, petName, getContext());
                    actionObserver();
                } else {
                    Toast.makeText(getContext(), "Your pet name cannot be blank", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        petObserver();
        petsViewModel.refreshPage(petOwnerUsername);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    private void petObserver() {
        petsViewModel.allPets.observe(getViewLifecycleOwner(), petsArr -> {
            if (petsArr != null) {
                petType.setVisibility(View.GONE);
                generateTypes(petsArr);
                petType.setVisibility(View.VISIBLE);
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
    }

    private void actionObserver() {
        petsViewModel.modifyLoading.observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading!= null && isLoading) {
                petsViewModel.refreshPage(petOwnerUsername);
                refreshListener.refreshPetsFragment();
            }
        });
    }

    private void generateTypes(String[] arr) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, arr);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        petType.setAdapter(adapter);
        petType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedType = arr[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void hideKeyboard(Activity activity) {
        if (activity.getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }
}