package com.kavya.stealthpad.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.kavya.stealthpad.R;
import com.kavya.stealthpad.databinding.DashboardBinding;
import com.kavya.stealthpad.ui.notes.AddNotes;

public class DashboardActivity extends AppCompatActivity {

    private MaterialCardView add;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DashboardBinding binding = DashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        add = binding.btnAddNewNote;
        add.setOnClickListener(v -> {
            startActivity(new Intent(this, AddNotes.class));
        });

        
        // Transparent status bar for modern look
        getWindow().setStatusBarColor(getResources().getColor(R.color.dash_bg, getTheme()));
    }
}
