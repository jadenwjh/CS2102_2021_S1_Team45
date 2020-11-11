package com.example.cs2102.view.adminView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cs2102.R;
import com.example.cs2102.model.Finance;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FinanceAdapter extends RecyclerView.Adapter<FinanceAdapter.RatingViewHolder> {

    private List<Finance> finances;

    public FinanceAdapter(List<Finance> list) {
        this.finances = list;
    }

    public void updateRatings(List<Finance> list) {
        finances.clear();
        finances.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RatingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_finance, parent, false);
        return new RatingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RatingViewHolder holder, int position) {
        holder.bind(finances.get(position));
    }

    @Override
    public int getItemCount() {
        return finances.size();
    }

    class RatingViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.date)
        TextView date;

        @BindView(R.id.profit)
        TextView profit;

        @BindView(R.id.revenue)
        TextView revenue;

        @BindView(R.id.salary)
        TextView salary;

        @BindView(R.id.pets)
        TextView pets;

        public RatingViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(Finance finances) {
            date.setText(String.format("Month: %s/%s", finances.getYear(), finances.getMonth()));
            profit.setText(String.format("Profit: $%s", finances.getProfit()));
            revenue.setText(String.format("Revenue: $%s", finances.getRevenue()));
            salary.setText(String.format("Total salary paid: $%s", finances.getSalary()));
            pets.setText(String.format("Total pets served: %s", finances.getPets()));
        }
    }
}
