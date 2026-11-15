package com.mowtiie.appareal.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.mowtiie.appareal.adapters.GenericAdapter;
import com.mowtiie.appareal.data.Data;
import com.mowtiie.appareal.databinding.ActivityOrdersBinding;

import java.util.ArrayList;

public class OrdersActivity extends AppCompatActivity {

    private ActivityOrdersBinding binding;
    private GenericAdapter<String[]> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityOrdersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        // order row: {id, po_number, order_date, total, status, eta}
        adapter = new GenericAdapter<>((b, o, position) -> {
            b.tvTitle.setText(o[1]);
            b.tvSubtitle.setText(o[2]);
            b.tvDetail.setText(Data.statusLabel(o[4]));
            b.tvTrailing.setText(Data.money(Integer.parseInt(o[3])));
            b.getRoot().setOnClickListener(v ->
                    startActivity(new Intent(this, OrderTrackingActivity.class).putExtra("order_id", o[0])));
        });
        binding.rvOrders.setLayoutManager(new LinearLayoutManager(this));
        binding.rvOrders.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ArrayList<String[]> rows = Data.ordersForUser(this);
        adapter.setItems(rows);
        binding.viewEmpty.getRoot().setVisibility(rows.isEmpty() ? View.VISIBLE : View.GONE);
        binding.viewEmpty.tvEmpty.setText("You have no orders yet");
    }
}
