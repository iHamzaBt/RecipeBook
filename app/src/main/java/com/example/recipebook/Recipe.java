package com.example.recipebook;

public class Recipe {
    private String recipeId;
    private String title;
    private String category;
    private String ingredients;
    private String steps;
    private String videoUrl;
    private String imageUrl;
    private String creatorId;
    private long timestamp;

    public Recipe() {
    }

    public Recipe(String recipeId, String title, String category, String ingredients, String steps, String videoUrl, String imageUrl, String creatorId, long timestamp) {
        this.recipeId = recipeId;
        this.title = title;
        this.category = category;
        this.ingredients = ingredients;
        this.steps = steps;
        this.videoUrl = videoUrl;
        this.imageUrl = imageUrl;
        this.creatorId = creatorId;
        this.timestamp = timestamp;
    }

    public String getRecipeId() { return recipeId; }
    public void setRecipeId(String recipeId) { this.recipeId = recipeId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }

    public String getSteps() { return steps; }
    public void setSteps(String steps) { this.steps = steps; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getCreatorId() { return creatorId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}