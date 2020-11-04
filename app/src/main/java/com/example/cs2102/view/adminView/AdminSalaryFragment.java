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
import com.example.cs2102.model.AdminStats;
import com.example.cs2102.view.adminView.viewModel.AdminSalaryViewModel;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AdminSalaryFragment extends Fragment {

    @BindView(R.id.searchSalary)
    Button searchByMonth;

    @BindView(R.id.stats)
    TextView stats;

    @BindView(R.id.monthSelected)
    TextView monthSelected;

    @BindView(R.id.loading)
    ProgressBar loadingBar;

    @BindView(R.id.salaryList)
    RecyclerView salaryList;

    private AdminSalaryViewModel adminSalaryViewModel;
    private static String adminUsername;
    private SalaryAdapter salaryAdapter = new SalaryAdapter(new ArrayList<>());

    public static AdminSalaryFragment newInstance(String username) {
        adminUsername = username;
        return new AdminSalaryFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.admin_salary_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adminSalaryViewModel = new ViewModelProvider(this).get(AdminSalaryViewModel.class);
        ButterKnife.bind(this, view);
        loadingBar.setVisibility(View.GONE);

        salaryList.setLayoutManager(new LinearLayoutManager(view.getContext()));
        salaryList.setAdapter(salaryAdapter);

        searchByMonth.setOnClickListener(v -> {
            showRangePicker();
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        salaryObserver();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    private void salaryObserver() {
        adminSalaryViewModel.fetchedData.observe(getViewLifecycleOwner(), fetched -> {
            if (fetched) {
                AdminStats stat = adminSalaryViewModel.stats.getValue();
                stats.setText(String.format("Total pets served: %s. Pet-days: %s", stat.getTotalpets() == null ? "0" : stat.getTotalpets(), stat.getPetdays()));
            } else {
                String error = "No service for this month";
                stats.setText(error);
            }
        });
        adminSalaryViewModel.salarys.observe(getViewLifecycleOwner(), salarys -> {
            if (salarys.size() != 0) {
                salaryAdapter.updateSalary(salarys);
                salaryList.setAdapter(salaryAdapter);
                salaryList.setVisibility(View.VISIBLE);
            }
        });
        adminSalaryViewModel.loading.observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                loadingBar.setVisibility(View.VISIBLE);
                salaryList.setVisibility(View.GONE);
            } else {
                loadingBar.setVisibility(View.GONE);
            }
        });
    }

    private void showRangePicker() {
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        builder.setTheme(R.style.ThemeOverlay_MaterialComponents_MaterialCalendar);
        builder.setCalendarConstraints(constraintsBuilder.build());
        MaterialDatePicker<Long> picker = builder.build();
        picker.show(getParentFragmentManager(), picker.toString());
        picker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
            @Override
            public void onPositiveButtonClick(Long selection) {
                SimpleDateFormat forButton = new SimpleDateFormat("MMM-yyyy");
                SimpleDateFormat forData = new SimpleDateFormat("yyyy-MM-dd");
                String month = forButton.format(new Date(selection));

                String parse = forData.format(new Date(selection));
                adminSalaryViewModel.fetchSalary(adminUsername, parse);

                searchByMonth.setText(month);
                monthSelected.setText(String.format("Month selected: %s", month));
                picker.dismiss();
            }
        });
    }
}