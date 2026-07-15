package com.example.recipebook;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.recipebook.databinding.ActivityLoginAfterSignupBinding;
import com.google.firebase.auth.FirebaseAuth;

public class LoginAfterSignupActivity extends AppCompatActivity {

    private ActivityLoginAfterSignupBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginAfterSignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        String registeredEmail = getIntent().getStringExtra("registered_email");
        if (registeredEmail != null) {
            binding.etLoginEmail.setText(registeredEmail);
        }

        binding.btnSubmitLogin.setOnClickListener(v -> {
            String email = binding.etLoginEmail.getText().toString().trim();
            String password = binding.etLoginPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tlLoginEmail.setError("Invalid email format. Make sure it contains '@' and a domain.");
                return;
            } else {
                binding.tlLoginEmail.setError(null);
            }

            binding.btnSubmitLogin.setEnabled(false);

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(LoginAfterSignupActivity.this, MainActivity.class));
                            finish();
                        } else {
                            String errorMsg = task.getException() != null ? task.getException().getMessage() : "Authentication failed.";
                            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                            binding.btnSubmitLogin.setEnabled(true);
                        }
                    });
        });
    }
}