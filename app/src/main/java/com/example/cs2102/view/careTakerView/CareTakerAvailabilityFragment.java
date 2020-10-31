package com.example.cs2102.view.careTakerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cs2102.R;
import com.example.cs2102.widgets.Strings;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CareTakerAvailabilityFragment extends Fragment {

    @BindView(R.id.datePickerAvailability)
    Button datePicker;

    private static String contract;

    public static CareTakerAvailabilityFragment newInstance(String type) {
        contract = type;
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        datePicker.setOnClickListener(v -> {
            if (contract.equals(Strings.PART_TIME)) {
                int limit = 10;
                Calendar[] days = new Calendar[limit];
                for(int i = 0; i < limit; i++){
                    Calendar mCalendar = Calendar.getInstance();
                    days[i] = mCalendar;
                    mCalendar.add(Calendar.DAY_OF_MONTH, i);
                }
                availabilityDatePickerCallBack.showDatePicker(days);
            }
            if (contract.equals(Strings.FULL_TIME)) {
                Calendar[] days = new Calendar[0];
                availabilityDatePickerCallBack.showDatePicker(days);
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