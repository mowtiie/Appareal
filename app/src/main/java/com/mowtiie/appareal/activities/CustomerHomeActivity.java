package com.mowtiie.appareal.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.mowtiie.appareal.R;
import com.mowtiie.appareal.adapters.GenericAdapter;
import com.mowtiie.appareal.data.Data;
import com.mowtiie.appareal.databinding.ActivityCustomerHomeBinding;

import java.util.ArrayList;

public class CustomerHomeActivity extends AppCompatActivity {

    private ActivityCustomerHomeBinding binding;
    private GenericAdapter<String[]> adapter;
    private BadgeDrawable cartBadge;

    @Override
    @ExperimentalBadgeUtils
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityCustomerHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cartBadge = BadgeDrawable.create(this);
        cartBadge.setVisible(false);
        binding.toolbar.post(() ->
                BadgeUtils.attachBadgeDrawable(cartBadge, binding.toolbar, R.id.action_cart));
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_cart) {
                startActivity(new Intent(this, CartActivity.class));
                return true;
            } else if (item.getItemId() == R.id.action_orders) {
                startActivity(new Intent(this, OrdersActivity.class));
                return true;
            } else if (item.getItemId() == R.id.action_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });

        // product row: {id, name, description, price, stock, image}
        adapter = new GenericAdapter<>((b, p, position) -> {
            b.ivImage.setVisibility(View.VISIBLE);
            Data.loadImage(b.ivImage, p[5]);
            b.tvTitle.setText(p[1]);
            b.tvSubtitle.setText(p[2]);
            b.tvTrailing.setText(Data.money(Integer.parseInt(p[3])));
            b.tvDetail.setText(Integer.parseInt(p[4]) == 0 ? "Out of stock" : "");
            b.getRoot().setOnClickListener(v ->
                    startActivity(new Intent(this, ProductDetailActivity.class).putExtra("product_id", p[0])));
        });
        binding.rvProducts.setLayoutManager(new LinearLayoutManager(this));
        binding.rvProducts.setAdapter(adapter);

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                load();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
        int count = Data.cartCount(this);
        cartBadge.setNumber(count);
        cartBadge.setVisible(count > 0);
    }

    private void load() {
        ArrayList<String[]> rows = Data.searchProducts(this, binding.etSearch.getText().toString());
        adapter.setItems(rows);
        binding.viewEmpty.getRoot().setVisibility(rows.isEmpty() ? View.VISIBLE : View.GONE);
        binding.viewEmpty.tvEmpty.setText("No products found");
    }
}
