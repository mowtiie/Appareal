package com.mowtiie.appareal.activities;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.color.MaterialColors;
import com.google.android.material.textview.MaterialTextView;
import com.mowtiie.appareal.data.Data;
import com.mowtiie.appareal.databinding.ActivityOrderTrackingBinding;

public class OrderTrackingActivity extends AppCompatActivity {

    private static final String[] STEPS = {"Placed", "Verified", "Packaged", "On shipment"};

    private ActivityOrderTrackingBinding binding;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityOrderTrackingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        orderId = getIntent().getStringExtra("order_id");
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // order row: {id, po_number, order_date, total, payment, address, status, eta}
        String[] o = Data.getOrder(this, orderId);
        binding.tvPo.setText(o[1]);
        binding.tvInfo.setText("Date: " + o[2] + "\nDeliver to: " + o[5] + "\nPayment: Cash on Delivery");
        binding.llItems.removeAllViews();
        for (String[] i : Data.orderItems(this, orderId)) {
            int price = Integer.parseInt(i[1]);
            int qty = Integer.parseInt(i[2]);
            MaterialTextView tv = new MaterialTextView(this);
            tv.setTextSize(16);
            tv.setPadding(0, 6, 0, 6);
            tv.setText(qty + " x " + i[0] + "  " + Data.money(qty * price));
            binding.llItems.addView(tv);
        }
        binding.tvTotal.setText("Total: " + Data.money(Integer.parseInt(o[3])));
        binding.tvStatus.setText(Data.statusLabel(o[6]));

        int current = 0;
        if (Data.STATUS_VERIFIED.equals(o[6])) current = 1;
        else if (Data.STATUS_PACKAGED.equals(o[6])) current = 2;
        else if (Data.STATUS_ON_SHIPMENT.equals(o[6])) current = 3;

        int done = MaterialColors.getColor(binding.getRoot(), attr("colorPrimary"));
        int muted = MaterialColors.getColor(binding.getRoot(), attr("colorOutline"));
        android.util.TypedValue tvl = new android.util.TypedValue();
        getTheme().resolveAttribute(attr("textAppearanceTitleMedium"), tvl, true);
        int titleStyle = tvl.resourceId;
        binding.llSteps.removeAllViews();
        for (int s = 0; s < STEPS.length; s++) {
            MaterialTextView tv = new MaterialTextView(this);
            tv.setTextSize(16);
            tv.setPadding(0, 6, 0, 6);
            tv.setTextAppearance(titleStyle);
            tv.setText((s <= current ? "●  " : "○  ") + STEPS[s]);
            tv.setTextColor(s <= current ? done : muted);
            tv.setPadding(0, 12, 0, 12);
            binding.llSteps.addView(tv);
        }

        boolean shipping = Data.STATUS_ON_SHIPMENT.equals(o[6]);
        binding.tvEta.setVisibility(shipping ? View.VISIBLE : View.GONE);
        binding.tvEta.setText("Estimated delivery: " + o[7]);
    }

    private int attr(String name) {
        return getResources().getIdentifier(name, "attr", getPackageName());
    }
}
