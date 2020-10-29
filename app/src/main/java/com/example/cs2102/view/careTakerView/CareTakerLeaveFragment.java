package com.example.cs2102.view.careTakerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.cs2102.R;
import com.example.cs2102.view.careTakerView.viewModel.CareTakerLeaveViewModel;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CareTakerLeaveFragment extends Fragment {

    @BindView(R.id.datePickerLeave)
    DatePicker datePicker;

    @BindView(R.id.date_selected)
    TextView dateSelected;

    @BindView(R.id.apply_leave)
    Button applyLeave;

    @BindView(R.id.loading)
    ProgressBar loading;

    private CareTakerLeaveViewModel careTakerLeaveViewModel;

    private static String currentCareTakerUsername;

    public static CareTakerLeaveFragment newInstance(String username) {
        currentCareTakerUsername = username;
        return new CareTakerLeaveFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.care_taker_leave_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        careTakerLeaveViewModel = ViewModelProviders.of(this).get(CareTakerLeaveViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        datePicker.setMinDate(Calendar.DATE);

        datePicker.setOnDateChangedListener((view1, year, monthOfYear, dayOfMonth) -> {

            @SuppressLint("DefaultLocale")
            String date = String.format("%d-%d-%d", year, monthOfYear, dayOfMonth);
            dateSelected.setText(date);
        });

        applyLeave.setOnClickListener(v -> {
            String date = dateSelected.getText().toString();
            careTakerLeaveViewModel.requestToApplyLeave(currentCareTakerUsername, date);
        });

        careTakerLeaveViewModel.loading.observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean) {
                loading.setVisibility(View.VISIBLE);
            } else {
                loading.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}