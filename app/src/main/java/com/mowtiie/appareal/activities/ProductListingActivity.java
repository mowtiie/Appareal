package com.mowtiie.appareal.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.mowtiie.appareal.R;
import com.mowtiie.appareal.adapters.GenericAdapter;
import com.mowtiie.appareal.data.Data;
import com.mowtiie.appareal.databinding.ActivityProductListingBinding;
import com.mowtiie.appareal.databinding.SheetProductBinding;

import java.util.ArrayList;

public class ProductListingActivity extends AppCompatActivity {

    private ActivityProductListingBinding binding;
    private GenericAdapter<String[]> adapter;
    private SheetProductBinding sheet;
    private String sheetImage = "";

    private final ActivityResultLauncher<String> pickImage = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null || sheet == null) return;
                String path = Data.saveImage(this, uri);
                if (path.isEmpty()) return;
                sheetImage = path;
                sheet.ivThumb.setVisibility(View.VISIBLE);
                Data.loadImage(sheet.ivThumb, sheetImage);
                sheet.tvImageError.setVisibility(View.GONE);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityProductListingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_add) {
                showSheet(null);
                return true;
            }
            return false;
        });

        // product row: {id, name, description, price, stock, image}
        adapter = new GenericAdapter<>((b, p, position) -> {
            b.ivImage.setVisibility(View.VISIBLE);
            Data.loadImage(b.ivImage, p[5]);
            b.tvTitle.setText(p[1]);
            b.tvSubtitle.setVisibility(View.GONE);
            b.tvDetail.setText("Stock: " + p[4]);
            b.tvTrailing.setText(Data.money(Integer.parseInt(p[3])));
            b.getRoot().setOnClickListener(v -> showSheet(p));
        });
        binding.rvProducts.setLayoutManager(new LinearLayoutManager(this));
        binding.rvProducts.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        ArrayList<String[]> rows = Data.allProducts(this);
        adapter.setItems(rows);
        binding.viewEmpty.getRoot().setVisibility(rows.isEmpty() ? View.VISIBLE : View.GONE);
        binding.viewEmpty.tvEmpty.setText("No products yet");
    }

    // product == null adds a new product, otherwise edits it
    private void showSheet(String[] product) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        sheet = SheetProductBinding.inflate(getLayoutInflater());
        dialog.setContentView(sheet.getRoot());
        sheetImage = "";
        sheet.tvSheetTitle.setText(product == null ? "Add product" : "Edit product");
        if (product != null) {
            sheetImage = product[5];
            sheet.ivThumb.setVisibility(View.VISIBLE);
            Data.loadImage(sheet.ivThumb, sheetImage);
            sheet.etName.setText(product[1]);
            sheet.etDescription.setText(product[2]);
            sheet.etPrice.setText(product[3]);
            sheet.etStock.setText(product[4]);
        }
        SheetProductBinding s = sheet;
        s.btnPickImage.setOnClickListener(v -> pickImage.launch("image/*"));
        s.btnSave.setOnClickListener(v -> {
            s.tilName.setError(null);
            s.tilDescription.setError(null);
            s.tilPrice.setError(null);
            s.tilStock.setError(null);
            s.tvImageError.setVisibility(View.GONE);
            String name = s.etName.getText().toString().trim();
            String desc = s.etDescription.getText().toString().trim();
            String priceText = s.etPrice.getText().toString().trim();
            String stockText = s.etStock.getText().toString().trim();
            boolean ok = true;
            if (sheetImage.isEmpty()) {
                s.tvImageError.setVisibility(View.VISIBLE);
                ok = false;
            }
            if (name.isEmpty()) {
                s.tilName.setError("Enter a name");
                ok = false;
            }
            if (desc.isEmpty()) {
                s.tilDescription.setError("Enter a description");
                ok = false;
            }
            if (!priceText.matches("\\d{1,9}") || Integer.parseInt(priceText) <= 0) {
                s.tilPrice.setError("Enter a whole number above 0");
                ok = false;
            }
            if (!stockText.matches("\\d{1,9}")) {
                s.tilStock.setError("Enter a whole number, 0 or more");
                ok = false;
            }
            if (!ok) return;
            int price = Integer.parseInt(priceText);
            int stock = Integer.parseInt(stockText);
            if (product == null) Data.insertProduct(this, name, desc, price, stock, sheetImage);
            else Data.updateProduct(this, product[0], name, desc, price, stock, sheetImage);
            dialog.dismiss();
            Toast.makeText(this, "Product saved", Toast.LENGTH_SHORT).show();
            load();
        });
        dialog.show();
    }
}
