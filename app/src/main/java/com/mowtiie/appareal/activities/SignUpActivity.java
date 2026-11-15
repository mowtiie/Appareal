package com.mowtiie.appareal.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.mowtiie.appareal.data.Data;
import com.mowtiie.appareal.databinding.ActivitySignUpBinding;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private String idImagePath = "";

    private final ActivityResultLauncher<String> pickImage = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null) return;
                String path = Data.saveImage(this, uri);
                if (path.isEmpty()) return;
                idImagePath = path;
                binding.ivIdThumb.setVisibility(View.VISIBLE);
                Data.loadImage(binding.ivIdThumb, idImagePath);
                binding.tvIdError.setVisibility(View.GONE);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.ddIdType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Data.ID_TYPES));
        binding.btnPickId.setOnClickListener(v -> pickImage.launch("image/*"));
        binding.btnGoLogin.setOnClickListener(v -> finish());
        binding.btnSignUp.setOnClickListener(v -> signUp());
    }

    private void signUp() {
        binding.tilLastName.setError(null);
        binding.tilFirstName.setError(null);
        binding.tilMiddleInitial.setError(null);
        binding.tilAddress.setError(null);
        binding.tilContact.setError(null);
        binding.tilIdType.setError(null);
        binding.tilUsername.setError(null);
        binding.tilPassword.setError(null);
        binding.tilConfirm.setError(null);
        binding.tvIdError.setVisibility(View.GONE);

        String last = binding.etLastName.getText().toString().trim();
        String first = binding.etFirstName.getText().toString().trim();
        String mi = binding.etMiddleInitial.getText().toString().trim();
        String address = binding.etAddress.getText().toString().trim();
        String contact = binding.etContact.getText().toString().trim();
        String idType = binding.ddIdType.getText().toString().trim();
        String username = binding.etUsername.getText().toString().trim();
        String password = binding.etPassword.getText().toString();
        String confirm = binding.etConfirm.getText().toString();

        boolean ok = true;
        if (!last.matches("[\\p{L} '\\-]+")) {
            binding.tilLastName.setError("Required. Letters, spaces, hyphens and apostrophes only");
            ok = false;
        }
        if (!first.matches("[\\p{L} '\\-]+")) {
            binding.tilFirstName.setError("Required. Letters, spaces, hyphens and apostrophes only");
            ok = false;
        }
        if (!mi.isEmpty() && !mi.matches("\\p{L}")) {
            binding.tilMiddleInitial.setError("Enter exactly one letter");
            ok = false;
        }
        if (address.length() < 10) {
            binding.tilAddress.setError("Enter your full address (10+ characters)");
            ok = false;
        }
        if (!contact.matches("09\\d{9}|\\+639\\d{9}")) {
            binding.tilContact.setError("Use 09XXXXXXXXX or +639XXXXXXXXX");
            ok = false;
        }
        if (idType.isEmpty()) {
            binding.tilIdType.setError("Choose a valid ID type");
            ok = false;
        }
        if (idImagePath.isEmpty()) {
            binding.tvIdError.setText("Upload an image of your valid ID");
            binding.tvIdError.setVisibility(View.VISIBLE);
            ok = false;
        }
        if (!username.matches("[A-Za-z0-9_]{4,}")) {
            binding.tilUsername.setError("4+ characters: letters, digits and underscore only");
            ok = false;
        } else if (Data.usernameTaken(this, username)) {
            binding.tilUsername.setError("Username is already taken");
            ok = false;
        }
        if (password.length() < 6) {
            binding.tilPassword.setError("At least 6 characters");
            ok = false;
        }
        if (!password.equals(confirm)) {
            binding.tilConfirm.setError("Passwords do not match");
            ok = false;
        }
        if (!ok) return;

        Data.insertUser(this, username, password, last, first, mi.toUpperCase(), address, contact, idType, idImagePath);
        Toast.makeText(this, "Account created. Please log in.", Toast.LENGTH_SHORT).show();
        finish();
    }
}
