package com.example.cs2102.view.careTakerView;

import android.os.Bundle;
import android.os.Parcel;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.cs2102.R;
import com.example.cs2102.view.careTakerView.viewModel.CareTakerSalaryViewModel;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CareTakerSalaryFragment extends Fragment {

    @BindView(R.id.monthSelected)
    TextView month;

    @BindView(R.id.amount)
    TextView amount;

    @BindView(R.id.search_button)
    Button datePicker;

    @BindView(R.id.loading)
    ProgressBar loadingBar;

    private CareTakerSalaryViewModel careTakerSalaryViewModel;
    private static String caretakerUsername;

    public static CareTakerSalaryFragment newInstance(String username) {
        caretakerUsername = username;
        return new CareTakerSalaryFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.care_taker_salary_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        careTakerSalaryViewModel = new ViewModelProvider(this).get(CareTakerSalaryViewModel.class);
        ButterKnife.bind(this, view);
        loadingBar.setVisibility(View.GONE);

        datePicker.setOnClickListener(v -> showRangePicker());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        salaryObserver();
    }

    private void salaryObserver() {
        careTakerSalaryViewModel.loading.observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                loadingBar.setVisibility(View.VISIBLE);
            } else {
                loadingBar.setVisibility(View.GONE);
            }
        });
        careTakerSalaryViewModel.salary.observe(getViewLifecycleOwner(), value -> {
            if (value.trim().length() != 0) {
                amount.setText(String.format("Salary for the month: $%s", value));
            }
        });
        careTakerSalaryViewModel.nothing.observe(getViewLifecycleOwner(), isNothing -> {
            if (isNothing) {
                String hint = "You earned nothing this month";
                amount.setText(hint);
            }
        });
    }

    private void showRangePicker() {
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        constraintsBuilder.setValidator(new CalendarConstraints.DateValidator() {
            @Override
            public boolean isValid(long date) {
                Calendar cal = Calendar.getInstance();
                if (date > cal.getTimeInMillis()) {
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {

            }
        });
        builder.setTheme(R.style.ThemeOverlay_MaterialComponents_MaterialCalendar);
        builder.setTitleText(R.string.select_dates);
        builder.setCalendarConstraints(constraintsBuilder.build());
        MaterialDatePicker<Long> picker = builder.build();
        picker.show(getParentFragmentManager(), picker.toString());
        picker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
            @Override
            public void onPositiveButtonClick(Long selection) {
                SimpleDateFormat spf = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat text = new SimpleDateFormat("MMM");
                String date = spf.format(new Date(selection));
                careTakerSalaryViewModel.fetchSalary(caretakerUsername, date);
                month.setText(String.format("Selected month: %s", text.format(new Date(selection))));
                picker.dismiss();
            }
        });
    }
}