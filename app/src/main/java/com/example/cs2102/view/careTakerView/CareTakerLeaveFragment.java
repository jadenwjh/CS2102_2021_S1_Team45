package com.example.cs2102.view.careTakerView;

import android.annotation.SuppressLint;
import android.content.Context;
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

import com.example.cs2102.R;
import com.example.cs2102.view.careTakerView.viewModel.CareTakerLeaveViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CareTakerLeaveFragment extends Fragment {

    @BindView(R.id.datePickerLeave)
    Button datePicker;

    @BindView(R.id.date_selected)
    TextView dateSelected;

    @BindView(R.id.apply_leave)
    Button applyLeave;

    @BindView(R.id.loading)
    ProgressBar loading;

    private static CareTakerLeaveViewModel careTakerLeaveViewModel;

    private static String currentCareTakerUsername;

    public static CareTakerLeaveFragment newInstance(String username, CareTakerLeaveViewModel vm) {
        currentCareTakerUsername = username;
        careTakerLeaveViewModel = vm;
        return new CareTakerLeaveFragment();
    }

    public interface LeaveDatePickerCallback {
        void showDatePicker(Calendar[] days);
    }

    private LeaveDatePickerCallback datePickerCallback;

    public void setLeaveDatePicker(LeaveDatePickerCallback impl) {
        this.datePickerCallback = impl;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.care_taker_leave_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        datePicker.setOnClickListener(v -> {
            Calendar[] days = new Calendar[0];
            try {
                days = convertStringToDates(Objects.requireNonNull(careTakerLeaveViewModel.availableDates.getValue()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            datePickerCallback.showDatePicker(days);
        });

        applyLeave.setOnClickListener(v -> {
            String date = dateSelected.getText().toString();
            careTakerLeaveViewModel.requestToApplyLeave(currentCareTakerUsername, date);
            careTakerLeaveViewModel.refresh(currentCareTakerUsername);
        });

        careTakerLeaveViewModel.loading.observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean) {
                loading.setVisibility(View.VISIBLE);
            } else {
                loading.setVisibility(View.GONE);
            }
        });

        careTakerLeaveViewModel.selectedDate.observe(getViewLifecycleOwner(), date -> dateSelected.setText(date));
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof LeaveDatePickerCallback) {
            datePickerCallback = (LeaveDatePickerCallback) context;
        } else {
            throw new ClassCastException("RegisterPOListener not implemented");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private Calendar[] convertStringToDates(List<String> dates) throws ParseException {
        List<Calendar> calendarList = new ArrayList<>();
        for (String date : dates) {
            //YYYY-MM-DD
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Calendar cal = Calendar.getInstance();
            cal.setTime(Objects.requireNonNull(format.parse(date)));
            calendarList.add(cal);
        }
        return calendarList.toArray(new Calendar[0]);
    }
}