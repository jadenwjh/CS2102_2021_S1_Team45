package com.example.cs2102.view.adminView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cs2102.R;
import com.example.cs2102.model.PetType;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PetPriceAdapter extends RecyclerView.Adapter<PetPriceAdapter.PetPriceViewHolder> {

    private List<PetType> petPrices;

    public PetPriceAdapter(List<PetType> list) {
        this.petPrices = list;
    }

    public void updatePetPrices(List<PetType> list) {
        petPrices.clear();
        petPrices.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PetPriceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_price_per_pet, parent, false);
        return new PetPriceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PetPriceViewHolder holder, int position) {
        holder.bind(petPrices.get(position));
    }

    @Override
    public int getItemCount() {
        return petPrices.size();
    }


    class PetPriceViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.pet_type)
        TextView type;

        @BindView(R.id.price)
        TextView price;

        public PetPriceViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(PetType petType) {
            type.setText(String.format("Category: %s", petType.getType()));
            price.setText(String.format("Base Price: $%s", petType.getBasePrice()));
        }
    }
}
