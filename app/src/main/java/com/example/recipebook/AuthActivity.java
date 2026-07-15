package com.example.recipebook;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import com.example.recipebook.databinding.ActivityAuthBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AuthActivity extends AppCompatActivity {

    private ActivityAuthBinding binding;
    private boolean isLogin = true;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Uri imageUri;

    private static final String SUPABASE_PROJECT_ID = "lyzqccbdaxjigdphapka";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imx5enFjY2JkYXhqaWdkcGhhcGthIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODM4ODM3NDEsImV4cCI6MjA5OTQ1OTc0MX0.M6fNO6QbCYENqyfIVP4m48rpKKKcFUA_ZQeRgORrra4";
    private static final String BUCKET_NAME = "profile_pics";

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    imageUri = uri;
                    binding.ivProfilePhoto.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        String[] countries = {"Select Country", "Palestine", "Jordan", "Egypt", "Lebanon", "Syria"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, countries);
        binding.spinnerCountry.setAdapter(adapter);

        boolean openRegister = getIntent().getBooleanExtra("open_register", false);
        if (openRegister) {
            binding.tvTabSignUp.post(this::switchToRegister);
        }

        binding.tvTabSignUp.setOnClickListener(v -> {
            if (isLogin) switchToRegister();
        });

        binding.tvTabLogin.setOnClickListener(v -> {
            if (!isLogin) switchToLogin();
        });

        binding.ivProfilePhoto.setOnClickListener(v -> {
            pickImageLauncher.launch("image/*");
        });

        binding.btnSubmitLogin.setOnClickListener(v -> {
            String email = binding.etLoginEmail.getText().toString().trim();
            String password = binding.etLoginPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(AuthActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
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
                            if (binding.cbRememberMe.isChecked()) {
                                SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean("isRemembered", true);
                                editor.apply();
                            }
                            startActivity(new Intent(AuthActivity.this, MainActivity.class));
                            finish();
                        } else {
                            String errorMsg = task.getException() != null ? task.getException().getMessage() : "Authentication failed.";
                            Toast.makeText(AuthActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                            binding.btnSubmitLogin.setEnabled(true);
                        }
                    });
        });

        binding.btnSubmitRegister.setOnClickListener(v -> {
            String name = binding.etRegName.getText().toString().trim();
            String email = binding.etRegEmail.getText().toString().trim();
            String password = binding.etRegPassword.getText().toString().trim();
            String country = binding.spinnerCountry.getSelectedItem().toString();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || country.equals("Select Country")) {
                Toast.makeText(AuthActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tlRegEmail.setError("Invalid email format. Make sure it contains '@' and a domain.");
                return;
            } else {
                binding.tlRegEmail.setError(null);
            }

            if (password.length() < 6) {
                binding.tlRegPassword.setError("Password must be at least 6 characters.");
                return;
            } else {
                binding.tlRegPassword.setError(null);
            }

            registerUser(name, email, password, country);
        });
    }

    private void registerUser(String name, String email, String password, String country) {
        binding.btnSubmitRegister.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();

                        if (imageUri != null) {
                            uploadImageToSupabase(uid, name, email, country);
                        } else {
                            saveUserDataToFirestore(uid, name, email, country, "");
                        }
                    } else {
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Registration failed.";
                        Toast.makeText(AuthActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                        binding.btnSubmitRegister.setEnabled(true);
                    }
                });
    }

    private void uploadImageToSupabase(String uid, String name, String email, String country) {
        new Thread(() -> {
            try {
                InputStream is = getContentResolver().openInputStream(imageUri);
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[1024];
                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
                byte[] imageBytes = buffer.toByteArray();

                runOnUiThread(() -> {
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl("https://" + SUPABASE_PROJECT_ID + ".supabase.co/")
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();

                    SupabaseApi api = retrofit.create(SupabaseApi.class);

                    RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), imageBytes);

                    String authHeader = "Bearer " + SUPABASE_KEY;
                    String filePath = uid + ".jpg";

                    api.uploadImage(authHeader, SUPABASE_KEY, BUCKET_NAME, filePath, requestFile).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                String publicUrl = "https://" + SUPABASE_PROJECT_ID +
                                        ".supabase.co/storage/v1/object/public/" + BUCKET_NAME + "/" + filePath;

                                saveUserDataToFirestore(uid, name, email, country, publicUrl);
                            } else {
                                Toast.makeText(AuthActivity.this, "Image upload failed, saving data without image", Toast.LENGTH_SHORT).show();
                                saveUserDataToFirestore(uid, name, email, country, "");
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Toast.makeText(AuthActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                            saveUserDataToFirestore(uid, name, email, country, "");
                        }
                    });
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> saveUserDataToFirestore(uid, name, email, country, ""));
            }
        }).start();
    }

    private void saveUserDataToFirestore(String uid, String name, String email, String country, String imageUrl) {
        User user = new User(uid, name, email, country, imageUrl);

        db.collection("Users").document(uid).set(user)
                .addOnSuccessListener(aVoid -> {
                    mAuth.signOut();
                    clearRegistrationFields();

                    Intent intent = new Intent(AuthActivity.this, LoginAfterSignupActivity.class);
                    intent.putExtra("registered_email", email);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AuthActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    binding.btnSubmitRegister.setEnabled(true);
                });
    }

    private void clearRegistrationFields() {
        binding.etRegName.setText("");
        binding.etRegEmail.setText("");
        binding.etRegPassword.setText("");
        binding.spinnerCountry.setSelection(0);
        imageUri = null;
    }

    private void switchToRegister() {
        isLogin = false;
        float distance = binding.tvTabSignUp.getX();
        binding.tabIndicator.animate().translationX(distance).setDuration(300).start();
        binding.tvTabLogin.setTextColor(ContextCompat.getColor(this, R.color.light_bronze));
        binding.tvTabSignUp.setTextColor(ContextCompat.getColor(this, R.color.white));
        TransitionManager.beginDelayedTransition(binding.rootLayout, new AutoTransition().setDuration(300));
        binding.clLoginForm.setVisibility(View.GONE);
        binding.clRegisterForm.setVisibility(View.VISIBLE);
    }

    private void switchToLogin() {
        isLogin = true;
        binding.tabIndicator.animate().translationX(0).setDuration(300).start();
        binding.tvTabLogin.setTextColor(ContextCompat.getColor(this, R.color.white));
        binding.tvTabSignUp.setTextColor(ContextCompat.getColor(this, R.color.light_bronze));
        TransitionManager.beginDelayedTransition(binding.rootLayout, new AutoTransition().setDuration(300));
        binding.clRegisterForm.setVisibility(View.GONE);
        binding.clLoginForm.setVisibility(View.VISIBLE);
    }
}