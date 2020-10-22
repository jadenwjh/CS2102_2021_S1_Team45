package com.example.cs2102.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cs2102.R;
import com.example.cs2102.model.CareTaker;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CareTakerAdapter extends RecyclerView.Adapter<CareTakerAdapter.CareTakerViewHolder>{

    private List<CareTaker> careTakers;

    public CareTakerAdapter(List<CareTaker> careTakers) {
        this.careTakers = careTakers;
    }

    public void updateCareTakers(List<CareTaker> newCareTakers) {
        careTakers.clear();
        careTakers.addAll(newCareTakers);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CareTakerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_care_taker, parent, false);
        return new CareTakerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CareTakerViewHolder holder, int position) {
        holder.bind(careTakers.get(position));
    }

    @Override
    public int getItemCount() {
        return careTakers.size();
    }

    static class CareTakerViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.care_taker_name)
        TextView careTakerName;

        public CareTakerViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(CareTaker careTaker) {
            careTakerName.setText(careTaker.getUserID());
        }
    }
}
