package com.example.recipebook;

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

import com.example.recipebook.databinding.FragmentAddRecipeBinding;

public class AddRecipeFragment extends Fragment {

    private FragmentAddRecipeBinding binding;
    private Uri recipeImageUri;

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

        binding.btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        binding.cvImageUpload.setOnClickListener(v -> {
            pickImageLauncher.launch("image/*");
        });

        binding.btnSubmitRecipe.setOnClickListener(v -> {
            String title = binding.etRecipeTitle.getText().toString().trim();
            String category = binding.etRecipeCategory.getText().toString().trim();
            String ingredients = binding.etRecipeIngredients.getText().toString().trim();
            String steps = binding.etRecipeSteps.getText().toString().trim();
            String videoUrl = binding.etRecipeVideoUrl.getText().toString().trim();

            if (recipeImageUri == null) {
                Toast.makeText(requireContext(), "Please select a recipe photo", Toast.LENGTH_SHORT).show();
                return;
            }

            if (title.isEmpty() || category.isEmpty() || ingredients.isEmpty() || steps.isEmpty() || videoUrl.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show();
                return;
            }

        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}