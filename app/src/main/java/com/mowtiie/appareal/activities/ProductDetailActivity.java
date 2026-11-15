package com.mowtiie.appareal.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.mowtiie.appareal.data.Data;
import com.mowtiie.appareal.databinding.ActivityProductDetailBinding;

public class ProductDetailActivity extends AppCompatActivity {

    private ActivityProductDetailBinding binding;
    private String productId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityProductDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        productId = getIntent().getStringExtra("product_id");

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.btnAdd.setOnClickListener(v -> {
            if (!Data.addToCart(this, productId)) {
                Snackbar.make(binding.getRoot(), "Stock limit reached", Snackbar.LENGTH_SHORT).show();
                return;
            }
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Added to cart")
                    .setMessage("What would you like to do next?")
                    .setNegativeButton("Order more", (d, w) -> finish())
                    .setPositiveButton("Checkout", (d, w) -> {
                        startActivity(new Intent(this, CartActivity.class));
                        finish();
                    })
                    .show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        String[] p = Data.getProduct(this, productId);
        Data.loadImage(binding.ivImage, p[5]);
        binding.tvName.setText(p[1]);
        binding.tvDescription.setText(p[2]);
        binding.tvPrice.setText(Data.money(Integer.parseInt(p[3])));
        int stock = Integer.parseInt(p[4]);
        binding.tvStock.setText(stock == 0 ? "Out of stock" : stock + " in stock");
        binding.btnAdd.setEnabled(stock > 0);
    }
}
