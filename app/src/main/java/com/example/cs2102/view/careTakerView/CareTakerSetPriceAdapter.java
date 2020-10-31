package com.example.cs2102.view.careTakerView;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cs2102.R;
import com.example.cs2102.model.PetOwner;
import com.example.cs2102.model.PetTypeCost;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CareTakerSetPriceAdapter extends RecyclerView.Adapter<CareTakerSetPriceAdapter.PricesViewHolder> {

    private List<PetTypeCost> petPrices;
    private int selectedPos = RecyclerView.NO_POSITION;

    public CareTakerSetPriceAdapter(List<PetTypeCost> petPrices) {
        this.petPrices = petPrices;
    }

    public void updatePetPrices(List<PetTypeCost> priceList) {
        petPrices.clear();
        petPrices.addAll(priceList);
        notifyDataSetChanged();
    }

    private CareTakerSetPriceAdapter.PricesListener pricesListener;

    public interface PricesListener {
        void onPriceCardSelected(PetTypeCost petTypeCost); //set textViews upper and lower bound of fragment
    }

    public void setPricesListener(PricesListener listenerImpl) {
        this.pricesListener = listenerImpl;
    }

    @NonNull
    @Override
    public CareTakerSetPriceAdapter.PricesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_price_per_pet, parent, false);
        return new PricesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CareTakerSetPriceAdapter.PricesViewHolder holder, int position) {
        holder.priceCard.setCardBackgroundColor(selectedPos == position ? Color.CYAN : Color.WHITE);
        holder.bind(petPrices.get(position));
    }

    @Override
    public int getItemCount() {
        return petPrices.size();
    }

    class PricesViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.price_card)
        CardView priceCard;

        @BindView(R.id.pet_type)
        TextView petType;

        @BindView(R.id.price)
        TextView price;


        public PricesViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            priceCard.setOnClickListener(v -> {
                notifyItemChanged(selectedPos);
                selectedPos = getLayoutPosition();
                notifyItemChanged(selectedPos);
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    PetTypeCost currentPrice = petPrices.get(position);
                    pricesListener.onPriceCardSelected(currentPrice);
                }
            });
        }

        void bind(PetTypeCost petTypeCost) {
            petType.setText(petTypeCost.getType());
            price.setText(String.format("$%s per day", petTypeCost.getFee()));
        }
    }
}
