package com.mowtiie.appareal.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textview.MaterialTextView;
import com.mowtiie.appareal.data.Data;
import com.mowtiie.appareal.databinding.ActivityCheckoutBinding;

import java.util.ArrayList;

public class CheckoutActivity extends AppCompatActivity {

    private ActivityCheckoutBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityCheckoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.etAddress.setText(Data.USER_ADDRESS);
        binding.btnPlace.setOnClickListener(v -> {
            binding.tilAddress.setError(null);
            String address = binding.etAddress.getText().toString().trim();
            if (address.length() < 10) {
                binding.tilAddress.setError("Enter your full address (10+ characters)");
                return;
            }
            long orderId = Data.placeOrder(this, address);
            startActivity(new Intent(this, ReceiptActivity.class)
                    .putExtra("order_id", String.valueOf(orderId))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.llItems.removeAllViews();
        // cart row: {cartId, productId, name, price, qty, stock, image}
        ArrayList<String[]> rows = Data.cartRows(this);
        for (String[] r : rows) {
            int qty = Integer.parseInt(r[4]);
            int price = Integer.parseInt(r[3]);
            MaterialTextView tv = new MaterialTextView(this);
            tv.setText(qty + " x " + r[2] + "  " + Data.money(qty * price));
            binding.llItems.addView(tv);
        }
        binding.tvTotal.setText("Total: " + Data.money(Data.cartTotal(this)));
        binding.btnPlace.setEnabled(!rows.isEmpty());
    }
}
