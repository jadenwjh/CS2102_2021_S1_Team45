package com.example.cs2102.view.adminView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cs2102.R;
import com.example.cs2102.model.CaretakerInfo;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SalaryAdapter extends RecyclerView.Adapter<SalaryAdapter.SalaryViewHolder> {

    private List<CaretakerInfo> salaryList;

    public SalaryAdapter(List<CaretakerInfo> salaryList) {
        this.salaryList = salaryList;
    }

    public void updateSalary(List<CaretakerInfo> list) {
        salaryList.clear();
        salaryList.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SalaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_salary, parent, false);
        return new SalaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SalaryViewHolder holder, int position) {
        holder.bind(salaryList.get(position));
    }

    @Override
    public int getItemCount() {
        return salaryList.size();
    }

    class SalaryViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.caretaker)
        TextView caretaker;

        @BindView(R.id.contract)
        TextView contract;

        @BindView(R.id.salary)
        TextView salaryText;

        @BindView(R.id.days)
        TextView petDays;

        @BindView(R.id.ratings)
        TextView averageRating;

        public SalaryViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(CaretakerInfo info) {
            caretaker.setText(String.format("Care Taker: %s", info.getCaretaker()));
            contract.setText(String.format("Contract: %s", info.getContract()));
            salaryText.setText(String.format("Salary: $%s", info.getSalary()));
            petDays.setText(String.format("Pet-days: %s", info.getDays()));
            averageRating.setText(String.format("Ratings: Average of %s from %s users", info.getRating(), info.getFrequency()));
        }
    }
}
