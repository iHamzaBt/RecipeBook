package com.example.recipebook;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.recipebook.databinding.FragmentProfileBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileRecipeAdapter adapter;
    private List<Recipe> myRecipesList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentProfileImageUrl = "";

    private static final String SUPABASE_PROJECT_ID = "lyzqccbdaxjigdphapka";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imx5enFjY2JkYXhqaWdkcGhhcGthIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODM4ODM3NDEsImV4cCI6MjA5OTQ1OTc0MX0.M6fNO6QbCYENqyfIVP4m48rpKKKcFUA_ZQeRgORrra4";
    private static final String BUCKET_NAME = "profile_pics";

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    uploadProfileImageToSupabase(uri);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        myRecipesList = new ArrayList<>();

        setupProfileInfo();
        setupRecyclerView();
        fetchMyRecipes();

        binding.btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        binding.btnLogout.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Logout", (dialog, which) -> {
                        mAuth.signOut();
                        Intent intent = new Intent(getActivity(), WelcomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        binding.ivUserProfile.setOnClickListener(v -> showProfilePictureOptions());
    }

    private void showProfilePictureOptions() {
        String[] options = {"View Photo", "Change Photo"};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Profile Photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showFullProfileImage();
                    } else if (which == 1) {
                        pickImageLauncher.launch("image/*");
                    }
                })
                .show();
    }

    private void showFullProfileImage() {
        if (currentProfileImageUrl == null || currentProfileImageUrl.isEmpty()) {
            Toast.makeText(requireContext(), "No profile picture set", Toast.LENGTH_SHORT).show();
            return;
        }

        ProfilePictureDialogFragment dialogFragment = ProfilePictureDialogFragment.newInstance(currentProfileImageUrl);
        dialogFragment.show(getParentFragmentManager(), "ProfilePictureDialog");
    }

    private void uploadProfileImageToSupabase(Uri imageUri) {
        Toast.makeText(requireContext(), "Updating photo...", Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            try {
                InputStream is = requireContext().getContentResolver().openInputStream(imageUri);
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
                                updateUserProfileImageUrl(publicUrl);
                            } else {
                                Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            if (!isAdded() || getContext() == null) return;
                            Toast.makeText(requireContext(), "Network Error", Toast.LENGTH_SHORT).show();
                        }
                    });
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void updateUserProfileImageUrl(String newImageUrl) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String oldImageUrl = currentProfileImageUrl;
            db.collection("Users").document(currentUser.getUid())
                    .update("profileImageUrl", newImageUrl)
                    .addOnSuccessListener(aVoid -> {
                        deleteOldProfileImageFromSupabase(oldImageUrl);
                        currentProfileImageUrl = newImageUrl;
                        Glide.with(requireContext())
                                .load(newImageUrl)
                                .circleCrop()
                                .into(binding.ivUserProfile);
                        Toast.makeText(requireContext(), "Profile photo updated!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to save profile photo", Toast.LENGTH_SHORT).show());
        }
    }

    private void deleteOldProfileImageFromSupabase(String oldImageUrl) {
        if (oldImageUrl != null && oldImageUrl.contains(BUCKET_NAME + "/")) {
            String filePath = oldImageUrl.substring(oldImageUrl.indexOf(BUCKET_NAME + "/") + BUCKET_NAME.length() + 1);

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://" + SUPABASE_PROJECT_ID + ".supabase.co/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            SupabaseApi api = retrofit.create(SupabaseApi.class);
            String authHeader = "Bearer " + SUPABASE_KEY;

            api.deleteImage(authHeader, SUPABASE_KEY, BUCKET_NAME, filePath).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {}

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {}
            });
        }
    }

    private void setupProfileInfo() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();

            db.collection("Users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (!isAdded() || binding == null) return;

                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            if (user != null) {
                                binding.tvUserName.setText(user.getName() != null && !user.getName().isEmpty() ? user.getName() : "Chef");
                                binding.tvUserEmail.setText(user.getEmail());

                                if (user.getCountry() != null && !user.getCountry().isEmpty() && !user.getCountry().equalsIgnoreCase("Select Country")) {
                                    binding.tvUserCountry.setText("📍 " + user.getCountry());
                                    binding.tvUserCountry.setVisibility(View.VISIBLE);
                                } else {
                                    binding.tvUserCountry.setVisibility(View.GONE);
                                }

                                currentProfileImageUrl = user.getProfileImageUrl();
                                if (currentProfileImageUrl != null && !currentProfileImageUrl.isEmpty()) {
                                    Glide.with(requireContext())
                                            .load(currentProfileImageUrl)
                                            .circleCrop()
                                            .into(binding.ivUserProfile);
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (!isAdded() || binding == null) return;
                        String name = currentUser.getDisplayName();
                        binding.tvUserName.setText(name != null && !name.isEmpty() ? name : "Chef");
                        binding.tvUserEmail.setText(currentUser.getEmail());
                        binding.tvUserCountry.setVisibility(View.GONE);
                    });
        }
    }

    private void setupRecyclerView() {
        binding.rvMyRecipes.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ProfileRecipeAdapter(myRecipesList);
        binding.rvMyRecipes.setAdapter(adapter);
    }

    private void fetchMyRecipes() {
        String currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";

        if (!currentUserId.isEmpty()) {
            db.collection("Recipes")
                    .whereEqualTo("creatorId", currentUserId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (!isAdded() || binding == null) return;

                        if (task.isSuccessful()) {
                            myRecipesList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Recipe recipe = document.toObject(Recipe.class);
                                myRecipesList.add(recipe);
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(requireContext(), "Failed to load recipes", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}