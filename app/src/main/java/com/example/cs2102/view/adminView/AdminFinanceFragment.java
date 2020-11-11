package com.example.cs2102.view.adminView;

import androidx.core.util.Pair;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcel;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.cs2102.R;
import com.example.cs2102.view.adminView.viewModel.AdminFinanceViewModel;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AdminFinanceFragment extends Fragment {

    @BindView(R.id.ratingList)
    RecyclerView finances;

    @BindView(R.id.errorListing)
    TextView error;

    @BindView(R.id.loading)
    ProgressBar loadingBar;

    @BindView(R.id.search)
    Button datePicker;

    private AdminFinanceViewModel adminFinanceViewModel;
    private FinanceAdapter financeAdapter = new FinanceAdapter(new ArrayList<>());
    private static String adminUsername;

    public static AdminFinanceFragment newInstance(String username) {
        adminUsername = username;
        return new AdminFinanceFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.admin_finance_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        error.setVisibility(View.GONE);
        loadingBar.setVisibility(View.GONE);
        adminFinanceViewModel = new ViewModelProvider(this).get(AdminFinanceViewModel.class);

        finances.setLayoutManager(new LinearLayoutManager(view.getContext()));

        datePicker.setOnClickListener(v -> showRangePicker());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ratingObserver();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    private void ratingObserver() {
        adminFinanceViewModel.loading.observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                loadingBar.setVisibility(View.VISIBLE);
                finances.setVisibility(View.GONE);
            } else {
                loadingBar.setVisibility(View.GONE);
            }
        });
        adminFinanceViewModel.finances.observe(getViewLifecycleOwner(), list -> {
            if (list.size() != 0) {
                financeAdapter.updateRatings(list);
                finances.setAdapter(financeAdapter);
                finances.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showRangePicker() {
        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        constraintsBuilder.setValidator(new CalendarConstraints.DateValidator() {
            @Override
            public boolean isValid(long date) {
                if (date > Calendar.getInstance().getTimeInMillis()) {
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
        MaterialDatePicker<Pair<Long, Long>> picker = builder.build();
        picker.show(getParentFragmentManager(), picker.toString());
        picker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>>() {
            @Override
            public void onPositiveButtonClick(Pair<Long, Long> selection) {
                SimpleDateFormat spf = new SimpleDateFormat("yyyy-MM-dd");
                String startDate = spf.format(new Date(selection.first));
                String endDate = spf.format(new Date(selection.second));
                String setButton = String.format("%s - %s", startDate.substring(5,7), endDate.substring(5,7));
                datePicker.setText(setButton);
                adminFinanceViewModel.fetchFinance(startDate, endDate);
                picker.dismiss();
            }
        });
    }
}