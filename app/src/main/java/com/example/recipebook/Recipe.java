package com.example.recipebook;

public class Recipe {
    private String name;
    private String category;
    private String imageUrl;

    public Recipe() {
    }

    public Recipe(String name, String category, String imageUrl) {
        this.name = name;
        this.category = category;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}