package com.example.cs2102.view.careTakerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cs2102.R;
import com.example.cs2102.model.PetOwner;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PetOwnerAdapter extends RecyclerView.Adapter<PetOwnerAdapter.PetOwnerViewHolder> {

    private List<PetOwner> petOwners;

    public PetOwnerAdapter(List<PetOwner> petOwners) {
        this.petOwners = petOwners;
    }

    public void updatePetOwners(List<PetOwner> newPetOwners) {
        petOwners.clear();
        petOwners.addAll(newPetOwners);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PetOwnerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pet_owner, parent, false);
        return new PetOwnerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PetOwnerViewHolder holder, int position) {
        holder.bind(petOwners.get(position));
    }

    @Override
    public int getItemCount() {
        return petOwners.size();
    }

    static class PetOwnerViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.pet_owner_name)
        TextView petOwnerName;

        @BindView(R.id.pet_owner_pet_name)
        TextView petOwnerPet;

        public PetOwnerViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(PetOwner petOwner) {
            petOwnerName.setText(petOwner.getUserID());
            petOwnerPet.setText(petOwner.getPetName());
        }
    }
}
