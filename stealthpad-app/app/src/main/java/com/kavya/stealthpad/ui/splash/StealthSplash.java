package com.kavya.stealthpad.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.kavya.stealthpad.R;
import com.kavya.stealthpad.ui.dashboard.DashboardActivity;

public class StealthSplash extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stealth_splash);

        // Delay to transition to DashboardActivity
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isFinishing() && !isDestroyed()) {
                Intent intent = new Intent(StealthSplash.this, DashboardActivity.class);
                startActivity(intent);
                
                // Use fade transition for a smooth entry to Dashboard
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                
                // Finish AFTER starting next activity to avoid black flicker
                finish();
            }
        }, 2000);
    }
}
