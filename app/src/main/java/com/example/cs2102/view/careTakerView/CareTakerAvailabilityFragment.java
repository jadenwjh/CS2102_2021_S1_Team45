package com.example.cs2102.view.careTakerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.cs2102.R;
import com.example.cs2102.view.careTakerView.viewModel.CareTakerAvailabilityViewModel;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CareTakerAvailabilityFragment extends Fragment {

    @BindView(R.id.datePickerAvailability)
    DatePicker datePicker;

    @BindView(R.id.confirm_dates)
    Button sendDates;

    @BindView(R.id.loading)
    ProgressBar loading;

    private CareTakerAvailabilityViewModel careTakerAvailabilityViewModel;

    private static String username;

    public static CareTakerAvailabilityFragment newInstance(String uName) {
        username = uName;
        return new CareTakerAvailabilityFragment();
    }

    private SetFreeDatesListener setFreeDatesListener;

    public interface SetFreeDatesListener {
        void onExitSetAvailability();
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

        initDatePicker();
    }

    private void initDatePicker() {
        datePicker.setMinDate(System.currentTimeMillis());
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
        calendar.set(Calendar.MONTH, (calendar.get(Calendar.YEAR)+1));
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH));
        datePicker.setMaxDate(calendar.getTimeInMillis());

    }
}