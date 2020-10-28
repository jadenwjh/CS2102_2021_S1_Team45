package com.example.cs2102.view.careTakerView;

import androidx.lifecycle.ViewModelProviders;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import com.example.cs2102.R;
import com.example.cs2102.view.careTakerView.viewModel.CareTakerLeaveViewModel;

import java.sql.Date;
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

    private CareTakerLeaveViewModel careTakerLeaveViewModel;

    private static String currentCareTakerUsername;

    public static CareTakerLeaveFragment newInstance(String username) {
        currentCareTakerUsername = username;
        return new CareTakerLeaveFragment();
    }

    private ApplyLeaveListener applyLeaveListener;

    public interface ApplyLeaveListener {
        void onExitApplyLeave();
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

        datePicker.setOnDateChangedListener(new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                @SuppressLint("DefaultLocale")
                String date = String.format("%d-%d-%d", year, monthOfYear, dayOfMonth);
                dateSelected.setText(date);
            }
        });

        applyLeave.setOnClickListener(v -> {
            String date = dateSelected.getText().toString();
            careTakerLeaveViewModel.requestToApplyLeave(currentCareTakerUsername, date);
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof ApplyLeaveListener) {
            applyLeaveListener = (ApplyLeaveListener) context;
        } else {
            throw new ClassCastException("ApplyLeaveListener not implemented");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}