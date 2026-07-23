package com.example.recipebook;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private List<Recipe> recipeList;

    public RecipeAdapter(List<Recipe> recipeList) {
        this.recipeList = recipeList;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipeList.get(position);

        holder.tvRecipeTitle.setText(recipe.getTitle());

        Glide.with(holder.itemView.getContext())
                .load(recipe.getImageUrl())
                .into(holder.ivRecipeImage);

        holder.itemView.setOnClickListener(v -> {
            RecipeDetailsFragment targetFragment = new RecipeDetailsFragment();

            Bundle bundle = new Bundle();
            bundle.putString("recipeId", recipe.getRecipeId());
            bundle.putString("title", recipe.getTitle());
            bundle.putString("category", recipe.getCategory());
            bundle.putString("ingredients", recipe.getIngredients());
            bundle.putString("steps", recipe.getSteps());
            bundle.putString("videoUrl", recipe.getVideoUrl());
            bundle.putString("imageUrl", recipe.getImageUrl());
            bundle.putString("creatorId", recipe.getCreatorId());
            targetFragment.setArguments(bundle);

            ((AppCompatActivity) holder.itemView.getContext()).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, targetFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        ImageView ivRecipeImage;
        TextView tvRecipeTitle;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            ivRecipeImage = itemView.findViewById(R.id.ivRecipeImage);
            tvRecipeTitle = itemView.findViewById(R.id.tvRecipeTitle);
        }
    }
}