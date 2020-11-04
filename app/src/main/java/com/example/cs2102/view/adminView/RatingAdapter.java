package com.example.cs2102.view.adminView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cs2102.R;
import com.example.cs2102.model.Rating;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RatingAdapter extends RecyclerView.Adapter<RatingAdapter.RatingViewHolder> {

    private List<Rating> ratings;

    public RatingAdapter(List<Rating> list) {
        this.ratings = list;
    }

    public void updateRatings(List<Rating> list) {
        ratings.clear();
        ratings.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RatingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rating, parent, false);
        return new RatingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RatingViewHolder holder, int position) {
        holder.bind(ratings.get(position));
    }

    @Override
    public int getItemCount() {
        return ratings.size();
    }

    class RatingViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.caretaker)
        TextView caretaker;

        @BindView(R.id.rating)
        TextView rate;

        public RatingViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(Rating rating) {
            caretaker.setText(String.format("Care Taker: %s", rating.getCareTaker()));
            rate.setText(String.format("Average Rating: %s", rating.getRating()));
        }
    }
}
