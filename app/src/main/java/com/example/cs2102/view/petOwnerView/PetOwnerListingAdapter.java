package com.example.cs2102.view.petOwnerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cs2102.R;
import com.example.cs2102.model.Listing;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PetOwnerListingAdapter extends RecyclerView.Adapter<PetOwnerListingAdapter.ListingViewHolder>{

    private List<Listing> listings;

    public PetOwnerListingAdapter(List<Listing> listings) {
        this.listings = listings;
    }

    public void updateListings(List<Listing> lists) {
        listings.clear();
        listings.addAll(lists);
        notifyDataSetChanged();
    }

    private ListingListener listingListener;

    public interface ListingListener {
        void onListingSelected(Listing listing);
    }

    public void setListingListener(ListingListener impl) {
        this.listingListener = impl;
    }

    @NonNull
    @Override
    public PetOwnerListingAdapter.ListingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_listing, parent, false);
        return new ListingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PetOwnerListingAdapter.ListingViewHolder holder, int position) {
        holder.bind(listings.get(position));
    }

    @Override
    public int getItemCount() {
        return listings.size();
    }

    class ListingViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.listing_card)
        CardView listing;

        @BindView(R.id.care_taker_name_listing)
        TextView careTaker;

        @BindView(R.id.listing_type)
        TextView category;

        @BindView(R.id.listing_price)
        TextView price;

        @BindView(R.id.listing_date)
        TextView avail;

        public ListingViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            listing.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Listing currentListing = listings.get(position);
                    listingListener.onListingSelected(currentListing);
                }
            });
        }

        void bind(Listing listing) {
            careTaker.setText(String.format("Care Taker: %s", listing.getCareTaker()));
            category.setText(String.format("Category: %s", listing.getPetType()));
            price.setText(String.format("Fee per day: $%s", listing.getPrice()));
            avail.setText(String.format("Date: %s to %s", listing.getStartDate(), listing.getEndDate()));
        }
    }
}
