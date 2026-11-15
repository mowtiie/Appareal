package com.mowtiie.appareal.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.mowtiie.appareal.data.Data;
import com.mowtiie.appareal.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnGoSignUp.setOnClickListener(v -> startActivity(new Intent(this, SignUpActivity.class)));
        binding.btnLogin.setOnClickListener(v -> {
            binding.tilUsername.setError(null);
            binding.tilPassword.setError(null);
            String username = binding.etUsername.getText().toString().trim();
            String password = binding.etPassword.getText().toString();
            if (username.isEmpty()) {
                binding.tilUsername.setError("Enter your username");
                return;
            }
            if (password.isEmpty()) {
                binding.tilPassword.setError("Enter your password");
                return;
            }
            if (!Data.login(this, username, password)) {
                binding.tilPassword.setError("Wrong username or password");
                return;
            }
            Class<?> home = Data.ROLE_MANAGER.equals(Data.USER_ROLE)
                    ? ManagerHomeActivity.class : CustomerHomeActivity.class;
            startActivity(new Intent(this, home).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        });
    }
}
