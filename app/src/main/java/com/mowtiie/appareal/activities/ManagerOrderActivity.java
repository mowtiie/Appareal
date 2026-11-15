package com.mowtiie.appareal.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textview.MaterialTextView;
import com.mowtiie.appareal.data.Data;
import com.mowtiie.appareal.databinding.ActivityManagerOrderBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ManagerOrderActivity extends AppCompatActivity {

    private static final String[] LABELS = {"Verified", "Packaged", "On shipment"};
    private static final String[] VALUES = {Data.STATUS_VERIFIED, Data.STATUS_PACKAGED, Data.STATUS_ON_SHIPMENT};

    private ActivityManagerOrderBinding binding;
    private String orderId;
    private String po;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityManagerOrderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        orderId = getIntent().getStringExtra("order_id");

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        String[] o = Data.getOrder(this, orderId);
        po = o[1];
        binding.tvPo.setText(po);
        binding.tvInfo.setText("Customer: " + getIntent().getStringExtra("customer") + "\nDate: " + o[2]
                + "\nDeliver to: " + o[5] + "\nPayment: Cash on Delivery\nStatus: " + Data.statusLabel(o[6]));
        for (String[] i : Data.orderItems(this, orderId)) {
            int price = Integer.parseInt(i[1]);
            int qty = Integer.parseInt(i[2]);
            MaterialTextView tv = new MaterialTextView(this);
            tv.setText(qty + " x " + i[0] + "  " + Data.money(qty * price));
            binding.llItems.addView(tv);
        }
        binding.tvTotal.setText("Total: " + Data.money(Integer.parseInt(o[3])));

        binding.ddStatus.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, LABELS));
        for (int i = 0; i < VALUES.length; i++) {
            if (VALUES[i].equals(o[6])) binding.ddStatus.setText(LABELS[i], false);
        }
        binding.etEta.setText(o[7]);
        updateEtaVisibility();
        binding.ddStatus.setOnItemClickListener((p, v, pos, id) -> updateEtaVisibility());

        binding.etEta.setOnClickListener(v -> {
            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Estimated delivery date")
                    .setCalendarConstraints(new CalendarConstraints.Builder()
                            .setValidator(DateValidatorPointForward.from(MaterialDatePicker.todayInUtcMilliseconds()))
                            .build())
                    .build();
            picker.addOnPositiveButtonClickListener(sel -> {
                SimpleDateFormat f = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
                f.setTimeZone(TimeZone.getTimeZone("UTC"));
                binding.etEta.setText(f.format(new Date(sel)));
                binding.tilEta.setError(null);
            });
            picker.show(getSupportFragmentManager(), "eta");
        });

        binding.btnSave.setOnClickListener(v -> save());
    }

    private int selected() {
        String text = binding.ddStatus.getText().toString();
        for (int i = 0; i < LABELS.length; i++) if (LABELS[i].equals(text)) return i;
        return -1;
    }

    private void updateEtaVisibility() {
        binding.tilEta.setVisibility(selected() == 2 ? View.VISIBLE : View.GONE);
    }

    private void save() {
        binding.tilStatus.setError(null);
        binding.tilEta.setError(null);
        int s = selected();
        if (s < 0) {
            binding.tilStatus.setError("Choose a status");
            return;
        }
        String eta = binding.etEta.getText().toString().trim();
        if (s == 2 && eta.isEmpty()) {
            binding.tilEta.setError("Pick an estimated delivery date");
            return;
        }
        Data.setOrderStatus(this, orderId, VALUES[s], eta);
        Toast.makeText(this, "Order " + po + " set to " + LABELS[s], Toast.LENGTH_SHORT).show();
        finish();
    }
}
