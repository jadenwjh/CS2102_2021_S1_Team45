package com.example.cs2102.view.careTakerView;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.cs2102.R;
import com.example.cs2102.model.PetTypeCost;
import com.example.cs2102.view.careTakerView.viewModel.CareTakerSetPriceViewModel;

import java.util.ArrayList;
import java.util.Objects;

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

    @BindView(R.id.priceRange)
    TextView range;

    @BindView(R.id.addNewPrice)
    Button addPrice;

    @BindView(R.id.setPrice)
    Button confirmSet;

    @BindView(R.id.petTypesList)
    Spinner petTypesList;

    @BindView(R.id.removePrice)
    Button removePrice;

    @BindView(R.id.deletePetTypesList)
    Spinner deletePetTypesList;

    private static String currentCareTakerUsername;

    private CareTakerSetPriceViewModel pricesVM;
    private CareTakerSetPriceAdapter careTakerSetPriceAdapter = new CareTakerSetPriceAdapter(new ArrayList<>());
    private static PetTypeCost typeCost = null;
    private static String selectedPetType = null;
    private static String deletePetType = null;
    private static String selectedPetTypePrice = null;

    private CareTakerSetPriceRefresh careTakerSetPriceRefresh;

    public interface CareTakerSetPriceRefresh {
        void refreshFragment();
    }

    public void setCareTakerSetPriceRefresh(CareTakerSetPriceRefresh impl) {
        this.careTakerSetPriceRefresh = impl;
    }

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
        range.setVisibility(View.GONE);
        setPrice.setVisibility(View.GONE);
        removePrice.setEnabled(false);
        addPrice.setEnabled(false);

        pricesVM = ViewModelProviders.of(this).get(CareTakerSetPriceViewModel.class);
        pricesVM.loading.setValue(false);

        pricesRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        pricesRecyclerView.setAdapter(careTakerSetPriceAdapter);

        careTakerSetPriceAdapter.setPricesListener(new CareTakerSetPriceAdapter.PricesListener() {
            @Override
            public void onPriceCardSelected(PetTypeCost petTypeCost) {
                range.setVisibility(View.VISIBLE);
                setPrice.setVisibility(View.VISIBLE);
                range.setText(String.format("Valid: %s - %s", petTypeCost.getMin().substring(0,2), petTypeCost.getMax().substring(0,2)));
                setPrice.setText(petTypeCost.getFee());
                typeCost = petTypeCost;
            }
        });

        refreshLayout.setOnRefreshListener(() -> {
            pricesVM.refreshPage(currentCareTakerUsername);
            refreshLayout.setRefreshing(false);
        });

        confirmSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(getActivity());
                if (typeCost != null) {
                    pricesVM.updatePetTypeCost(currentCareTakerUsername, typeCost.getType(), Integer.parseInt(setPrice.getText().toString()));
                    pricesVM.refreshPage(currentCareTakerUsername);
                    range.setVisibility(View.GONE);
                    setPrice.setVisibility(View.GONE);
                    careTakerSetPriceRefresh.refreshFragment();
                }
            }
        });

        addPrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pricesVM.addPetType(currentCareTakerUsername, selectedPetType, Integer.parseInt(selectedPetTypePrice));
                pricesVM.refreshPage(currentCareTakerUsername);
                refreshAddSpinner(pricesVM.getPetTypesToShow());
                careTakerSetPriceRefresh.refreshFragment();
            }
        });

        removePrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pricesVM.deletePetType(currentCareTakerUsername, deletePetType);
                pricesVM.refreshPage(currentCareTakerUsername);
                refreshDeleteSpinner(pricesVM.getDeletePetTypesToShow());
                careTakerSetPriceRefresh.refreshFragment();
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        petPricesVMObserver();
        pricesVM.refreshPage(currentCareTakerUsername);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    private void petPricesVMObserver() {
        pricesVM.petTypeCosts.observe(getViewLifecycleOwner(), petPrices -> {
            noTypesMsg.setVisibility(View.GONE);
            if (petPrices != null) {
                Log.e("petPricesVMObserver", "Updating prices");
                careTakerSetPriceAdapter.updatePetPrices(petPrices);
                pricesRecyclerView.setVisibility(View.VISIBLE);
                pricesRecyclerView.setAdapter(careTakerSetPriceAdapter);
            } else {
                Toast.makeText(getActivity(), "You have no categories set", Toast.LENGTH_SHORT).show();
                noTypesMsg.setVisibility(View.VISIBLE);
            }
        });
        pricesVM.loadError.observe(getViewLifecycleOwner(), isError -> {
            if (isError != null) {
                listError.setVisibility(isError ? View.VISIBLE : View.GONE);
            }
        });
        pricesVM.setPriceError.observe(getViewLifecycleOwner(), isError -> {
            if (isError != null && isError) {
                Toast.makeText(getActivity(), "Price does not fall within base or upper limit", Toast.LENGTH_SHORT).show();
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
        pricesVM.petTypeAdapter.observe(getViewLifecycleOwner(), typeArr -> {
            if (typeArr != null) {
                petTypesList.setVisibility(View.GONE);
                if (typeArr.length != 0) {
                    addPrice.setEnabled(true);
                }
                refreshAddSpinner(typeArr);
                petTypesList.setVisibility(View.VISIBLE);
            }
        });
        pricesVM.removePetTypeAdapter.observe(getViewLifecycleOwner(), typeArr -> {
            if (typeArr != null) {
                deletePetTypesList.setVisibility(View.GONE);
                if (typeArr.length != 0) {
                    removePrice.setEnabled(true);
                }
                refreshDeleteSpinner(typeArr);
                deletePetTypesList.setVisibility(View.VISIBLE);
            }
        });
    }

    private void refreshAddSpinner(String[] arr) {
        ArrayAdapter<String> petTypeAdapterList = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, arr);
        petTypeAdapterList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        petTypesList.setAdapter(petTypeAdapterList);
        petTypesList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPetType = arr[position];
                selectedPetTypePrice = Objects.requireNonNull(pricesVM.petTypeBasePrices.getValue())[position];
                Log.e("refreshAddSpinner at", selectedPetType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void refreshDeleteSpinner(String[] arr) {
        ArrayAdapter<String> petTypeAdapterList = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, arr);
        petTypeAdapterList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        deletePetTypesList.setAdapter(petTypeAdapterList);
        deletePetTypesList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                deletePetType = arr[position];
                Log.e("refreshDeleteSpinner at", deletePetType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void hideKeyboard(Activity activity) {
        if (activity.getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }
}