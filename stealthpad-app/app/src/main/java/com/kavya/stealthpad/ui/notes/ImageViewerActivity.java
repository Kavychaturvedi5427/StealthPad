package com.kavya.stealthpad.ui.notes;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.kavya.stealthpad.R;

import java.io.File;

public class ImageViewerActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGE_PATH = "extra_image_path";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        String imagePath = getIntent().getStringExtra(EXTRA_IMAGE_PATH);
        ImageView imageView = findViewById(R.id.full_image_view);

        if (imagePath != null && new File(imagePath).exists()) {
            Glide.with(this)
                    .load(new File(imagePath))
                    .into(imageView);
        } else {
            Toast.makeText(this, R.string.image_not_found, Toast.LENGTH_SHORT).show();
            finish();
        }

        findViewById(R.id.btn_close_viewer).setOnClickListener(v -> finish());
        
        // Use transparent status bar for full screen feel
        getWindow().setStatusBarColor(getResources().getColor(android.R.color.transparent, getTheme()));
    }
}
