package com.example.cs2102.view.petOwnerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cs2102.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PetOwnerHomepageActivity extends AppCompatActivity {

    @BindView(R.id.homepageLoading)
    ProgressBar loadingBar;

    @BindView(R.id.viewListings)
    Button viewListings;

    @BindView(R.id.viewPets)
    Button viewPets;

    @BindView(R.id.viewOngoing)
    Button viewOngoing;

    @BindView(R.id.viewReview)
    Button viewReview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_owner_homepage);

        ButterKnife.bind(this);


        loadingBar.setVisibility(View.GONE);
    }

}