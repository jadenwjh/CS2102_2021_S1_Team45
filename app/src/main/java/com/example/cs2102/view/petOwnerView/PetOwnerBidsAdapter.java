package com.example.cs2102.view.petOwnerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cs2102.R;
import com.example.cs2102.model.CareTakerBid;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PetOwnerBidsAdapter extends RecyclerView.Adapter<PetOwnerBidsAdapter.BidsForReview> {

    private List<CareTakerBid> careTakerBids;

    public interface ReviewListener {
        void onStartReview(CareTakerBid currentBid);
    }

    private ReviewListener reviewListener;

    public void setReviewListener(ReviewListener impl) {
        this.reviewListener = impl;
    }

    public PetOwnerBidsAdapter(List<CareTakerBid> list) {
        this.careTakerBids = list;
    }

    public void updateBidsList(List<CareTakerBid> newList) {
        careTakerBids.clear();
        careTakerBids.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PetOwnerBidsAdapter.BidsForReview onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_care_taker_bid, parent, false);
        return new BidsForReview(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BidsForReview holder, int position) {
        holder.bind(careTakerBids.get(position));
    }

    @Override
    public int getItemCount() {
        return careTakerBids.size();
    }

    class BidsForReview extends RecyclerView.ViewHolder {

        @BindView(R.id.care_taker_bid_card)
        CardView bidCard;

        @BindView(R.id.care_taker_name)
        TextView careTaker;

        @BindView(R.id.pet_name)
        TextView petName;

        @BindView(R.id.price)
        TextView price;

        @BindView(R.id.date_range)
        TextView date;

        @BindView(R.id.status)
        TextView status;

        public BidsForReview(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            bidCard.setOnClickListener(v -> {
                if (status.getText().toString().equals("Status: Accepted")) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        CareTakerBid bid = careTakerBids.get(position);
                        reviewListener.onStartReview(bid);
                    }
                }
            });
        }

        void bind(CareTakerBid bid) {
            careTaker.setText(String.format("Care Taker: %s", bid.getCareTaker()));
            petName.setText(String.format("Pet: %s", bid.getPetName()));
            price.setText(String.format("Price: $%s", bid.getPrice()));
            date.setText(String.format("Date: %s to %s", bid.getStartDate().substring(0,10), bid.getEndDate().substring(0,10)));
            String currentStatus = bid.getStatus().equals("p") ? "Pending" : "Accepted";
            status.setText(String.format("Status: %s", currentStatus));
        }
    }
}
