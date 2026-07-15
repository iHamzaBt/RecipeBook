package com.example.recipebook;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.recipebook.databinding.ActivityWelcomeBinding;
import com.google.firebase.auth.FirebaseAuth;

public class WelcomeActivity extends AppCompatActivity {

    private ActivityWelcomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        boolean isRemembered = sharedPreferences.getBoolean("isRemembered", false);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        if (isRemembered && mAuth.getCurrentUser() != null) {
            startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
            finish();
            return;
        }

        binding = ActivityWelcomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnCreateAccount.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, AuthActivity.class);
            intent.putExtra("open_register", true);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_up, R.anim.stay);
        });

        binding.tvBottomSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, AuthActivity.class);
            intent.putExtra("open_register", true);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_up, R.anim.stay);
        });

        binding.btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, AuthActivity.class);
            intent.putExtra("open_register", false);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_up, R.anim.stay);
        });
    }
}