package com.example.cs2102.view.careTakerView;

import android.annotation.SuppressLint;
import android.content.Context;
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
import com.example.cs2102.view.careTakerView.viewModel.CareTakerAvailabilityViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CareTakerAvailabilityFragment extends Fragment {

    @BindView(R.id.datePickerAvailability)
    Button datePicker;

    @BindView(R.id.date_selected)
    TextView dateSelected;

    @BindView(R.id.set_available)
    Button sendDates;

    @BindView(R.id.loading)
    ProgressBar loading;

    private CareTakerAvailabilityViewModel careTakerAvailabilityViewModel;

    private static String username;

    public static CareTakerAvailabilityFragment newInstance(String uName) {
        username = uName;
        return new CareTakerAvailabilityFragment();
    }

    private AvailabilityDatePickerCallBack availabilityDatePickerCallBack;

    public interface AvailabilityDatePickerCallBack {
        void showDatePicker(Calendar[] days);
    }

    public void setAvailabilityDatePicker(AvailabilityDatePickerCallBack impl) {
        this.availabilityDatePickerCallBack = impl;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.care_taker_availability_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        careTakerAvailabilityViewModel = ViewModelProviders.of(this).get(CareTakerAvailabilityViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        datePicker.setOnClickListener(v -> {
            Calendar mCalendar = Calendar.getInstance();
            int limit = 30;
            Calendar[] days = new Calendar[30];
            for(int i = 0; i < limit; i++){
                days[i] = mCalendar;
                mCalendar.add(Calendar.DAY_OF_MONTH, 1);
            }
            availabilityDatePickerCallBack.showDatePicker(days);
        });

        sendDates.setOnClickListener(v -> {
            String date = dateSelected.getText().toString();
            careTakerAvailabilityViewModel.requestToSendAvailability(username, date);
        });

        careTakerAvailabilityViewModel.loading.observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean) {
                loading.setVisibility(View.VISIBLE);
            } else {
                loading.setVisibility(View.GONE);
            }
        });

        careTakerAvailabilityViewModel.selectedDate.observe(getViewLifecycleOwner(), date -> dateSelected.setText(date));
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof AvailabilityDatePickerCallBack) {
            availabilityDatePickerCallBack = (AvailabilityDatePickerCallBack) context;
        } else {
            throw new ClassCastException("RegisterPOListener not implemented");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}