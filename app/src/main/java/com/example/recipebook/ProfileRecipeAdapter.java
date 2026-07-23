package com.example.recipebook;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProfileRecipeAdapter extends RecyclerView.Adapter<ProfileRecipeAdapter.ProfileRecipeViewHolder> {

    private List<Recipe> recipeList;

    private static final String SUPABASE_PROJECT_ID = "lyzqccbdaxjigdphapka";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imx5enFjY2JkYXhqaWdkcGhhcGthIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODM4ODM3NDEsImV4cCI6MjA5OTQ1OTc0MX0.M6fNO6QbCYENqyfIVP4m48rpKKKcFUA_ZQeRgORrra4";

    public ProfileRecipeAdapter(List<Recipe> recipeList) {
        this.recipeList = recipeList;
    }

    @NonNull
    @Override
    public ProfileRecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe_profile, parent, false);
        return new ProfileRecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileRecipeViewHolder holder, int position) {
        Recipe recipe = recipeList.get(position);
        Context context = holder.itemView.getContext();

        holder.tvTitle.setText(recipe.getTitle());
        holder.tvCategory.setText(recipe.getCategory());

        Glide.with(context)
                .load(recipe.getImageUrl())
                .into(holder.ivImage);

        holder.itemView.setOnClickListener(v -> {
            RecipeDetailsFragment fragment = new RecipeDetailsFragment();
            Bundle bundle = createRecipeBundle(recipe);
            fragment.setArguments(bundle);

            ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        holder.btnEdit.setOnClickListener(v -> {
            AddRecipeFragment editFragment = new AddRecipeFragment();
            Bundle bundle = createRecipeBundle(recipe);
            editFragment.setArguments(bundle);

            ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, editFragment)
                    .addToBackStack(null)
                    .commit();
        });

        holder.btnDelete.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(context)
                    .setTitle("Delete Recipe")
                    .setMessage("Are you sure you want to delete this recipe?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        deleteRecipeWithImage(context, recipe, holder.getAdapterPosition());
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void deleteRecipeWithImage(Context context, Recipe recipe, int adapterPos) {
        String imageUrl = recipe.getImageUrl();
        if (imageUrl != null && imageUrl.contains("/object/public/")) {
            String fullPath = imageUrl.substring(imageUrl.indexOf("/object/public/") + 15);
            String[] parts = fullPath.split("/", 2);

            if (parts.length == 2) {
                String bucket = parts[0];
                String path = parts[1];

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("https://" + SUPABASE_PROJECT_ID + ".supabase.co/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                SupabaseApi api = retrofit.create(SupabaseApi.class);
                String authHeader = "Bearer " + SUPABASE_KEY;

                api.deleteImage(authHeader, SUPABASE_KEY, bucket, path).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        deleteFirestoreDocument(context, recipe.getRecipeId(), adapterPos);
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        deleteFirestoreDocument(context, recipe.getRecipeId(), adapterPos);
                    }
                });
                return;
            }
        }
        deleteFirestoreDocument(context, recipe.getRecipeId(), adapterPos);
    }

    private void deleteFirestoreDocument(Context context, String recipeId, int adapterPos) {
        if (recipeId != null && !recipeId.isEmpty()) {
            FirebaseFirestore.getInstance().collection("Recipes").document(recipeId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        if (adapterPos != RecyclerView.NO_POSITION && adapterPos < recipeList.size()) {
                            recipeList.remove(adapterPos);
                            notifyItemRemoved(adapterPos);
                        }
                        Toast.makeText(context, "Recipe deleted", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Error deleting recipe", Toast.LENGTH_SHORT).show());
        }
    }

    private Bundle createRecipeBundle(Recipe recipe) {
        Bundle bundle = new Bundle();
        bundle.putString("recipeId", recipe.getRecipeId());
        bundle.putString("title", recipe.getTitle());
        bundle.putString("category", recipe.getCategory());
        bundle.putString("ingredients", recipe.getIngredients());
        bundle.putString("steps", recipe.getSteps());
        bundle.putString("videoUrl", recipe.getVideoUrl());
        bundle.putString("imageUrl", recipe.getImageUrl());
        bundle.putString("creatorId", recipe.getCreatorId());
        return bundle;
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public static class ProfileRecipeViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvTitle, tvCategory;
        ImageButton btnEdit, btnDelete;

        public ProfileRecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivProfileRecipeImage);
            tvTitle = itemView.findViewById(R.id.tvProfileRecipeTitle);
            tvCategory = itemView.findViewById(R.id.tvProfileRecipeCategory);
            btnEdit = itemView.findViewById(R.id.btnProfileEdit);
            btnDelete = itemView.findViewById(R.id.btnProfileDelete);
        }
    }
}