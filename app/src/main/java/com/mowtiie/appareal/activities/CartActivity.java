package com.mowtiie.appareal.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.mowtiie.appareal.adapters.GenericAdapter;
import com.mowtiie.appareal.data.Data;
import com.mowtiie.appareal.databinding.ActivityCartBinding;

import java.util.ArrayList;

public class CartActivity extends AppCompatActivity {

    private ActivityCartBinding binding;
    private GenericAdapter<String[]> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.btnOrderMore.setOnClickListener(v -> startActivity(
                new Intent(this, CustomerHomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
        binding.btnCheckout.setOnClickListener(v -> startActivity(new Intent(this, CheckoutActivity.class)));

        // cart row: {cartId, productId, name, price, qty, stock, image}
        adapter = new GenericAdapter<>((b, r, position) -> {
            b.ivImage.setVisibility(View.VISIBLE);
            Data.loadImage(b.ivImage, r[6]);
            b.tvTitle.setText(r[2]);
            b.tvSubtitle.setText("");
            b.tvSubtitle.setVisibility(View.GONE);
            b.tvTrailing.setText(Data.money(Integer.parseInt(r[3])));
            b.llStepper.setVisibility(View.VISIBLE);
            b.tvQty.setText(r[4]);
            b.btnDelete.setVisibility(View.VISIBLE);
            b.btnMinus.setOnClickListener(v -> changeQty(r, -1));
            b.btnPlus.setOnClickListener(v -> changeQty(r, 1));
            b.btnDelete.setOnClickListener(v -> {
                int pos = adapter.getItems().indexOf(r);
                Data.deleteCart(this, r[0]);
                adapter.getItems().remove(pos);
                adapter.notifyItemRemoved(pos);
                refreshBar();
                Snackbar.make(binding.getRoot(), "Item removed", Snackbar.LENGTH_LONG)
                        .setAction("Undo", u -> {
                            Data.restoreCart(this, r[1], Integer.parseInt(r[4]));
                            load();
                        }).show();
            });
        });
        binding.rvCart.setLayoutManager(new LinearLayoutManager(this));
        binding.rvCart.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        adapter.setItems(Data.cartRows(this));
        refreshBar();
    }

    private void changeQty(String[] r, int delta) {
        int qty = Integer.parseInt(r[4]) + delta;
        if (qty < 1) return;
        if (qty > Integer.parseInt(r[5])) {
            Snackbar.make(binding.getRoot(), "Stock limit reached", Snackbar.LENGTH_SHORT).show();
            return;
        }
        Data.setCartQty(this, r[0], qty);
        r[4] = String.valueOf(qty);
        adapter.notifyItemChanged(adapter.getItems().indexOf(r));
        refreshBar();
    }

    private void refreshBar() {
        boolean empty = adapter.getItems().isEmpty();
        binding.viewEmpty.getRoot().setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.viewEmpty.tvEmpty.setText("Your cart is empty");
        binding.btnCheckout.setEnabled(!empty);
        binding.tvTotal.setText("Total: " + Data.money(Data.cartTotal(this)));
    }
}
