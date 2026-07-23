package com.example.recipebook;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.recipebook.databinding.FragmentAddRecipeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AddRecipeFragment extends Fragment {

    private FragmentAddRecipeBinding binding;
    private Uri recipeImageUri;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private static final String SUPABASE_PROJECT_ID = "lyzqccbdaxjigdphapka";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imx5enFjY2JkYXhqaWdkcGhhcGthIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODM4ODM3NDEsImV4cCI6MjA5OTQ1OTc0MX0.M6fNO6QbCYENqyfIVP4m48rpKKKcFUA_ZQeRgORrra4";
    private static final String BUCKET_NAME = "recipe_pics";

    private String editingRecipeId = null;
    private String existingImageUrl = null;

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    recipeImageUri = uri;
                    binding.ivRecipePhoto.setImageURI(uri);
                    binding.ivUploadPlaceholder.setVisibility(View.GONE);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAddRecipeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        String[] categories = {"Breakfast", "Lunch", "Dinner", "Desserts", "Other"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories);
        binding.spinnerCategory.setAdapter(categoryAdapter);

        if (getArguments() != null && getArguments().getString("recipeId") != null) {
            editingRecipeId = getArguments().getString("recipeId");
            existingImageUrl = getArguments().getString("imageUrl");

            binding.etRecipeTitle.setText(getArguments().getString("title"));
            binding.etRecipeIngredients.setText(getArguments().getString("ingredients"));
            binding.etRecipeSteps.setText(getArguments().getString("steps"));
            binding.etRecipeVideoUrl.setText(getArguments().getString("videoUrl"));

            String existingCategory = getArguments().getString("category");
            if (existingCategory != null) {
                for (int i = 0; i < categories.length; i++) {
                    if (categories[i].equalsIgnoreCase(existingCategory)) {
                        binding.spinnerCategory.setSelection(i);
                        break;
                    }
                }
            }

            binding.btnSubmitRecipe.setText("Update Recipe");
            binding.tvScreenTitle.setText("Edit Recipe");
            binding.ivUploadPlaceholder.setVisibility(View.GONE);

            if (existingImageUrl != null && !existingImageUrl.isEmpty()) {
                Glide.with(requireContext()).load(existingImageUrl).into(binding.ivRecipePhoto);
            }
        }

        binding.btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        binding.cvImageUpload.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        binding.btnSubmitRecipe.setOnClickListener(v -> {
            String title = binding.etRecipeTitle.getText().toString().trim();
            String category = binding.spinnerCategory.getSelectedItem().toString();
            String ingredients = binding.etRecipeIngredients.getText().toString().trim();
            String steps = binding.etRecipeSteps.getText().toString().trim();
            String videoUrl = binding.etRecipeVideoUrl.getText().toString().trim();

            if (recipeImageUri == null && existingImageUrl == null) {
                Toast.makeText(requireContext(), "Please select a recipe photo", Toast.LENGTH_SHORT).show();
                return;
            }

            if (title.isEmpty() || ingredients.isEmpty() || steps.isEmpty() || videoUrl.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            binding.btnSubmitRecipe.setEnabled(false);
            binding.btnSubmitRecipe.setText("Saving...");

            if (recipeImageUri != null) {
                uploadImageToSupabase(title, category, ingredients, steps, videoUrl);
            } else {
                saveRecipeToFirestore(title, category, ingredients, steps, videoUrl, existingImageUrl);
            }
        });
    }

    private void uploadImageToSupabase(String title, String category, String ingredients, String steps, String videoUrl) {
        new Thread(() -> {
            try {
                InputStream is = requireContext().getContentResolver().openInputStream(recipeImageUri);
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[1024];
                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
                byte[] imageBytes = buffer.toByteArray();

                if (!isAdded() || getActivity() == null) return;

                requireActivity().runOnUiThread(() -> {
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl("https://" + SUPABASE_PROJECT_ID + ".supabase.co/")
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();

                    SupabaseApi api = retrofit.create(SupabaseApi.class);
                    RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), imageBytes);
                    String authHeader = "Bearer " + SUPABASE_KEY;
                    String filePath = UUID.randomUUID().toString() + ".jpg";

                    api.uploadImage(authHeader, SUPABASE_KEY, BUCKET_NAME, filePath, requestFile).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (!isAdded() || getContext() == null) return;

                            if (response.isSuccessful()) {
                                String publicUrl = "https://" + SUPABASE_PROJECT_ID +
                                        ".supabase.co/storage/v1/object/public/" + BUCKET_NAME + "/" + filePath;

                                deleteOldImageFromSupabase(existingImageUrl, api);
                                saveRecipeToFirestore(title, category, ingredients, steps, videoUrl, publicUrl);
                            } else {
                                Toast.makeText(requireContext(), "Image upload failed", Toast.LENGTH_SHORT).show();
                                resetSubmitButton();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            if (!isAdded() || getContext() == null) return;
                            Toast.makeText(requireContext(), "Network Error", Toast.LENGTH_SHORT).show();
                            resetSubmitButton();
                        }
                    });
                });

            } catch (Exception e) {
                e.printStackTrace();
                if (isAdded() && getActivity() != null) {
                    requireActivity().runOnUiThread(this::resetSubmitButton);
                }
            }
        }).start();
    }

    private void deleteOldImageFromSupabase(String oldUrl, SupabaseApi api) {
        if (oldUrl != null && oldUrl.contains("/object/public/")) {
            String fullPath = oldUrl.substring(oldUrl.indexOf("/object/public/") + 15);
            String[] parts = fullPath.split("/", 2);

            if (parts.length == 2) {
                String authHeader = "Bearer " + SUPABASE_KEY;
                api.deleteImage(authHeader, SUPABASE_KEY, parts[0], parts[1]).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {}

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {}
                });
            }
        }
    }

    private void saveRecipeToFirestore(String title, String category, String ingredients, String steps, String videoUrl, String imageUrl) {
        String creatorId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";

        String recipeId = editingRecipeId != null ? editingRecipeId : db.collection("Recipes").document().getId();
        long timestamp = System.currentTimeMillis();

        Recipe recipe = new Recipe(recipeId, title, category, ingredients, steps, videoUrl, imageUrl, creatorId, timestamp);

        db.collection("Recipes").document(recipeId).set(recipe)
                .addOnSuccessListener(aVoid -> {
                    if (!isAdded() || getContext() == null) return;
                    Toast.makeText(requireContext(), editingRecipeId != null ? "Recipe updated!" : "Recipe added successfully!", Toast.LENGTH_SHORT).show();
                    if (getActivity() != null) {
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || getContext() == null) return;
                    Toast.makeText(requireContext(), "Failed to save recipe: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    resetSubmitButton();
                });
    }

    private void resetSubmitButton() {
        if (binding != null) {
            binding.btnSubmitRecipe.setEnabled(true);
            binding.btnSubmitRecipe.setText(editingRecipeId != null ? "Update Recipe" : "Share Recipe");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}