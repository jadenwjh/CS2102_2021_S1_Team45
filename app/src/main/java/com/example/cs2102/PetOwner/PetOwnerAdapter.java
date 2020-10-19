package com.example.cs2102.PetOwner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cs2102.R;

import java.util.ArrayList;

public class PetOwnerAdapter extends RecyclerView.Adapter<PetOwnerAdapter.PetOwnerViewHolder> {

    private ArrayList<PetOwner> petOwnerArrayList;

    public PetOwnerAdapter(ArrayList<PetOwner> dataList) {
        this.petOwnerArrayList = dataList;
    }

    @NonNull
    @Override
    public PetOwnerAdapter.PetOwnerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.pet_owner_row, parent, false);
        return new PetOwnerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PetOwnerAdapter.PetOwnerViewHolder holder, int position) {
        holder.petOwnerName.setText(petOwnerArrayList.get(position).getUid());
        holder.petOwnerPet.setText(petOwnerArrayList.get(position).getPet());
    }

    @Override
    public int getItemCount() {
        return petOwnerArrayList.size();
    }

    class PetOwnerViewHolder extends RecyclerView.ViewHolder {

        TextView petOwnerName, petOwnerPet;

        public PetOwnerViewHolder(@NonNull View itemView) {
            super(itemView);
            petOwnerName = itemView.findViewById(R.id.pet_owner_name);
            petOwnerPet = itemView.findViewById(R.id.pet_owner_pet);
        }
    }
}
