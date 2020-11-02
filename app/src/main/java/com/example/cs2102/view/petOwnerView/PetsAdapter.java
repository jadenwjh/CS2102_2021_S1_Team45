package com.example.cs2102.view.petOwnerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cs2102.R;
import com.example.cs2102.model.Pet;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PetsAdapter extends RecyclerView.Adapter<PetsAdapter.PetsViewHolder> {

    private List<Pet> petList;

    public PetsAdapter(List<Pet> pets) {
        this.petList = pets;
    }

    public void updatePets(List<Pet> list) {
        petList.clear();
        petList.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PetsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pet_petowner, parent, false);
        return new PetsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PetsViewHolder holder, int position) {
        holder.bind(petList.get(position));
    }

    @Override
    public int getItemCount() {
        return petList.size();
    }

    public class PetsViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.pet_petowner_card)
        CardView petCard;

        @BindView(R.id.pet_name)
        TextView name;

        @BindView(R.id.pet_type)
        TextView type;

        @BindView(R.id.pet_profile)
        TextView profile;

        @BindView(R.id.pet_needs)
        TextView needs;

        public PetsViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(Pet pet) {
            name.setText(String.format("Name: %s", pet.getName()));
            type.setText(String.format("Category: %s", pet.getType()));
            profile.setText(String.format("Profile: %s", pet.getProfile()));
            needs.setText(String.format("Requests: %s", pet.getNeeds()));
        }
    }
}
