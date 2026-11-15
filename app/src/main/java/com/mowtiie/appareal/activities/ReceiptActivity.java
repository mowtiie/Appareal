package com.mowtiie.appareal.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textview.MaterialTextView;
import com.mowtiie.appareal.data.Data;
import com.mowtiie.appareal.databinding.ActivityReceiptBinding;

public class ReceiptActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ActivityReceiptBinding binding = ActivityReceiptBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String orderId = getIntent().getStringExtra("order_id");
        // order row: {id, po_number, order_date, total, payment, address, status, eta}
        String[] o = Data.getOrder(this, orderId);
        binding.tvPo.setText(o[1]);
        binding.tvInfo.setText("Date: " + o[2] + "\nCustomer: " + Data.fullName() + "\nDeliver to: " + o[5]);
        for (String[] i : Data.orderItems(this, orderId)) {
            int price = Integer.parseInt(i[1]);
            int qty = Integer.parseInt(i[2]);
            MaterialTextView tv = new MaterialTextView(this);
            tv.setText(i[0] + "\n" + qty + " x " + Data.money(price) + " = " + Data.money(qty * price));
            tv.setPadding(0, 0, 0, 8);
            binding.llItems.addView(tv);
        }
        binding.tvPayment.setText("Payment: Cash on Delivery");
        binding.tvTotal.setText("Total amount due: " + Data.money(Integer.parseInt(o[3])));

        binding.btnTrack.setOnClickListener(v ->
                startActivity(new Intent(this, OrderTrackingActivity.class).putExtra("order_id", orderId)));
        binding.btnContinue.setOnClickListener(v -> startActivity(new Intent(this, CustomerHomeActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)));
    }
}
