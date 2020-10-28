package com.example.cs2102.view.careTakerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cs2102.R;
import com.example.cs2102.model.PetOwner;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CareTakerBidsAdapter extends RecyclerView.Adapter<CareTakerBidsAdapter.BidsReceivedViewHolder> {

    private List<PetOwner> petOwners;

    public CareTakerBidsAdapter(List<PetOwner> petOwners) {
        this.petOwners = petOwners;
    }

    public void updatePetOwners(List<PetOwner> newPetOwners) {
        petOwners.clear();
        petOwners.addAll(newPetOwners);
        notifyDataSetChanged();
    }

    private CareTakerBidsAdapter.BidsListener bidsListener;

    public interface BidsListener {
        //TODO: implement onBidSelected in either Fragment or Activity
        void onBidSelected(PetOwner petOwner); //can be sent to fragment or the activity or show another fragment
    }

    @NonNull
    @Override
    public BidsReceivedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pet_owner, parent, false);
        return new BidsReceivedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BidsReceivedViewHolder holder, int position) {
        holder.bind(petOwners.get(position));
    }

    @Override
    public int getItemCount() {
        return petOwners.size();
    }

    class BidsReceivedViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.bid_card)
        CardView petOwnerBid;

        @BindView(R.id.pet_owner_name)
        TextView petOwnerName;

        @BindView(R.id.pet_owner_pet_name)
        TextView petOwnerPet;

        public BidsReceivedViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            petOwnerBid.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    PetOwner petOwner = petOwners.get(position);
                    bidsListener.onBidSelected(petOwner);
                }
            });
        }

        void bind(PetOwner petOwner) {
            petOwnerName.setText(petOwner.getUserID());
            petOwnerPet.setText(petOwner.getPetName());
        }
    }
}
