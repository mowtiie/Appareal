package com.mowtiie.appareal.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.mowtiie.appareal.R;
import com.mowtiie.appareal.adapters.GenericAdapter;
import com.mowtiie.appareal.data.Data;
import com.mowtiie.appareal.databinding.ActivityManagerHomeBinding;

import java.util.ArrayList;

public class ManagerHomeActivity extends AppCompatActivity {

    private ActivityManagerHomeBinding binding;
    private GenericAdapter<String[]> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityManagerHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_products) {
                startActivity(new Intent(this, ProductListingActivity.class));
                return true;
            } else if (item.getItemId() == R.id.action_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });

        // manager row: {id, po_number, order_date, total, status, customer name}
        adapter = new GenericAdapter<>((b, o, position) -> {
            b.tvTitle.setText(o[1] + " - " + o[5]);
            b.tvSubtitle.setText(o[2]);
            b.tvDetail.setText(Data.statusLabel(o[4]));
            b.tvTrailing.setText(Data.money(Integer.parseInt(o[3])));
            b.getRoot().setOnClickListener(v -> startActivity(new Intent(this, ManagerOrderActivity.class)
                    .putExtra("order_id", o[0]).putExtra("customer", o[5])));
        });
        binding.rvOrders.setLayoutManager(new LinearLayoutManager(this));
        binding.rvOrders.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ArrayList<String[]> rows = Data.allOrders(this);
        adapter.setItems(rows);
        binding.viewEmpty.getRoot().setVisibility(rows.isEmpty() ? View.VISIBLE : View.GONE);
        binding.viewEmpty.tvEmpty.setText("No orders yet");
    }
}
