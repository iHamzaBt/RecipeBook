package com.example.recipebook;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.recipebook.databinding.FragmentProfilePictureDialogBinding;

public class ProfilePictureDialogFragment extends DialogFragment {

    private FragmentProfilePictureDialogBinding binding;

    public static ProfilePictureDialogFragment newInstance(String imageUrl) {
        ProfilePictureDialogFragment fragment = new ProfilePictureDialogFragment();
        Bundle args = new Bundle();
        args.putString("imageUrl", imageUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfilePictureDialogBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getDialog() != null && getDialog().getWindow() != null) {
            Window window = getDialog().getWindow();
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        }

        if (getArguments() != null) {
            String imageUrl = getArguments().getString("imageUrl", "");
            Glide.with(this)
                    .load(imageUrl)
                    .into(binding.ivFocusedProfilePicture);
        }

        binding.getRoot().setOnClickListener(v -> dismiss());
        binding.btnCloseOverlay.setOnClickListener(v -> dismiss());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}