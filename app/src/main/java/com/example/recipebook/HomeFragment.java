package com.example.recipebook;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.recipebook.databinding.FragmentHomeBinding;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private List<Recipe> allRecipesList;
    private List<Recipe> filteredList;
    private RecipeAdapter adapter;
    private RecipeAdapter searchAdapter;

    private String currentCategory = "All";
    private String currentQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        allRecipesList = new ArrayList<>();
        filteredList = new ArrayList<>();

        setupRecyclerViews();
        setupTabLayout();
        setupSearchLogic();
        setupProfileIcon();

        binding.fabAddRecipe.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new AddRecipeFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    private void setupRecyclerViews() {
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        binding.rvRecipes.setLayoutManager(layoutManager);
        adapter = new RecipeAdapter(filteredList);
        binding.rvRecipes.setAdapter(adapter);

        StaggeredGridLayoutManager searchLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        binding.rvSearchResults.setLayoutManager(searchLayoutManager);
        searchAdapter = new RecipeAdapter(filteredList);
        binding.rvSearchResults.setAdapter(searchAdapter);
    }

    private void setupTabLayout() {
        binding.tabLayoutCategories.addTab(binding.tabLayoutCategories.newTab().setText("All"));
        binding.tabLayoutCategories.addTab(binding.tabLayoutCategories.newTab().setText("Breakfast"));
        binding.tabLayoutCategories.addTab(binding.tabLayoutCategories.newTab().setText("Lunch"));
        binding.tabLayoutCategories.addTab(binding.tabLayoutCategories.newTab().setText("Dinner"));
        binding.tabLayoutCategories.addTab(binding.tabLayoutCategories.newTab().setText("Desserts"));

        binding.tabLayoutCategories.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentCategory = tab.getText().toString();
                applyFilters();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupSearchLogic() {
        binding.searchView.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentQuery = s.toString().trim().toLowerCase();
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.searchBar.setOnClickListener(v -> binding.searchView.show());
    }

    private void setupProfileIcon() {
        binding.searchBar.inflateMenu(R.menu.menu_search_bar);
        binding.searchBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_profile) {
                return true;
            }
            return false;
        });
    }

    private void applyFilters() {
        filteredList.clear();

        for (Recipe recipe : allRecipesList) {
            boolean matchesCategory = currentCategory.equals("All") || recipe.getCategory().equalsIgnoreCase(currentCategory);
            boolean matchesQuery = currentQuery.isEmpty() || recipe.getName().toLowerCase().contains(currentQuery);

            if (matchesCategory && matchesQuery) {
                filteredList.add(recipe);
            }
        }

        adapter.notifyDataSetChanged();
        searchAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}