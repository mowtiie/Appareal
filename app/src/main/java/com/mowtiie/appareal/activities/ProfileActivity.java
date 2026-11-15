package com.mowtiie.appareal.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.mowtiie.appareal.data.Data;
import com.mowtiie.appareal.databinding.ActivityProfileBinding;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ActivityProfileBinding binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.tvName.setText(Data.fullName());
        if (Data.ROLE_CUSTOMER.equals(Data.USER_ROLE)) {
            binding.tvInfo.setText("Username: " + Data.USER_USERNAME + "\nContact: " + Data.USER_CONTACT
                    + "\nAddress: " + Data.USER_ADDRESS + "\nValid ID: " + Data.USER_ID_TYPE);
            binding.ivId.setVisibility(View.VISIBLE);
            Data.loadImage(binding.ivId, Data.USER_ID_IMAGE);
        } else {
            binding.tvInfo.setText("Username: " + Data.USER_USERNAME + "\nRole: Store Manager");
        }
        binding.btnLogout.setOnClickListener(v -> {
            Data.logout();
            startActivity(new Intent(this, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        });
    }
}
