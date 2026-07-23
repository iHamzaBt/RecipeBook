package com.example.recipebook;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.recipebook.databinding.FragmentRecipeDetailsBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RecipeDetailsFragment extends Fragment {

    private FragmentRecipeDetailsBinding binding;
    private String youtubeUrl = "";
    private String recipeId = "";
    private String imageUrl = "";

    private static final String SUPABASE_PROJECT_ID = "lyzqccbdaxjigdphapka";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imx5enFjY2JkYXhqaWdkcGhhcGthIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODM4ODM3NDEsImV4cCI6MjA5OTQ1OTc0MX0.M6fNO6QbCYENqyfIVP4m48rpKKKcFUA_ZQeRgORrra4";
    private static final String BUCKET_NAME = "recipe_pics";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRecipeDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        binding.btnWatchVideo.setOnClickListener(v -> {
            if (youtubeUrl != null && !youtubeUrl.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(youtubeUrl));
                startActivity(intent);
            }
        });

        if (getArguments() != null) {
            recipeId = getArguments().getString("recipeId", "");
            String title = getArguments().getString("title");
            String category = getArguments().getString("category");
            String ingredients = getArguments().getString("ingredients");
            String steps = getArguments().getString("steps");
            youtubeUrl = getArguments().getString("videoUrl", "");
            imageUrl = getArguments().getString("imageUrl", "");
            String creatorId = getArguments().getString("creatorId");

            binding.tvDetailTitle.setText(title);
            binding.tvDetailCategory.setText(category);
            binding.tvDetailIngredients.setText(ingredients);
            binding.tvDetailSteps.setText(steps);

            Glide.with(this)
                    .load(imageUrl)
                    .into(binding.ivRecipeDetailImage);

            String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

            if (creatorId != null && creatorId.equals(currentUserId) && !currentUserId.isEmpty()) {
                binding.llCreatorActions.setVisibility(View.VISIBLE);
            } else {
                binding.llCreatorActions.setVisibility(View.GONE);
            }
        }

        binding.btnEditRecipe.setOnClickListener(v -> {
            AddRecipeFragment editFragment = new AddRecipeFragment();
            editFragment.setArguments(getArguments());

            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, editFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        binding.btnDeleteRecipe.setOnClickListener(v -> {
            if (recipeId != null && !recipeId.isEmpty()) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Delete Recipe")
                        .setMessage("Are you sure you want to delete this recipe?")
                        .setPositiveButton("Delete", (dialog, which) -> deleteRecipeWithImage())
                        .setNegativeButton("Cancel", null)
                        .show();
            } else {
                Toast.makeText(requireContext(), "Invalid recipe ID", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteRecipeWithImage() {
        if (imageUrl != null && imageUrl.contains(BUCKET_NAME + "/")) {
            String filePath = imageUrl.substring(imageUrl.indexOf(BUCKET_NAME + "/") + BUCKET_NAME.length() + 1);

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://" + SUPABASE_PROJECT_ID + ".supabase.co/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            SupabaseApi api = retrofit.create(SupabaseApi.class);
            String authHeader = "Bearer " + SUPABASE_KEY;

            api.deleteImage(authHeader, SUPABASE_KEY, BUCKET_NAME, filePath).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    deleteFirestoreDocument();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    deleteFirestoreDocument();
                }
            });
        } else {
            deleteFirestoreDocument();
        }
    }

    private void deleteFirestoreDocument() {
        FirebaseFirestore.getInstance().collection("Recipes").document(recipeId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "Recipe deleted", Toast.LENGTH_SHORT).show();
                    if (getActivity() != null) {
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Error deleting recipe", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}