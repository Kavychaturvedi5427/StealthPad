package com.kavya.stealthpad.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.kavya.stealthpad.R;
import com.kavya.stealthpad.ViewModel.AuthViewModel.AuthState;
import com.kavya.stealthpad.ViewModel.AuthViewModel.AuthViewModel;
import com.kavya.stealthpad.ui.Auth.ManageAccountActivity;
import com.kavya.stealthpad.data.repository.Notes.NotesRepository;
import com.kavya.stealthpad.synchronization.SyncScheduler;
import com.kavya.stealthpad.utils.AttachmentStorageManager;
import com.kavya.stealthpad.utils.BiometricHelper;
import com.kavya.stealthpad.utils.SessionManager;

import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsActivity extends AppCompatActivity {

    @Inject
    SessionManager sessionManager;

    @Inject
    NotesRepository notesRepository;

    @Inject
    AttachmentStorageManager storageManager;

    private AuthViewModel authViewModel;
    private TextView currentThemeText, currentAutoLockText, currentSortText, currentCategoryText;
    private MaterialSwitch switchBiometric;
    private boolean isLoggingOut = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
        updateSettingsUI();
        observeAuthState();
    }

    private void observeAuthState() {
        authViewModel.getAuthState().observe(this, state -> {
            if (state instanceof AuthState.LoggedOut) {
                performLogout();
            }
        });
    }

    private void initViews() {
        currentThemeText = findViewById(R.id.current_theme_text);
        currentAutoLockText = findViewById(R.id.current_auto_lock_text);
        currentSortText = findViewById(R.id.current_sort_text);
        currentCategoryText = findViewById(R.id.current_category_text);
        switchBiometric = findViewById(R.id.switch_biometric);
    }

    private void setupListeners() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        findViewById(R.id.btn_manage_account).setOnClickListener(v -> {
            if (!sessionManager.isLoggedIn()) {
                Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, ManageAccountActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        findViewById(R.id.btn_logout).setOnClickListener(v -> {
            checkPendingSyncAndLogout();
        });

        findViewById(R.id.btn_change_theme).setOnClickListener(v -> showThemeDialog());

        switchBiometric.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !BiometricHelper.isBiometricAvailable(this)) {
                Toast.makeText(this, "Biometric authentication is not available on this device", Toast.LENGTH_SHORT).show();
                switchBiometric.setChecked(false);
                return;
            }
            sessionManager.setBiometricEnabled(isChecked);
        });

        findViewById(R.id.btn_auto_lock).setOnClickListener(v -> showAutoLockDialog());

        findViewById(R.id.btn_sort_notes).setOnClickListener(v -> showSortDialog());

        findViewById(R.id.btn_default_category).setOnClickListener(v -> showCategoryDialog());

        findViewById(R.id.btn_clear_storage).setOnClickListener(v -> showClearStorageDialog());

        findViewById(R.id.btn_about).setOnClickListener(v -> showAboutDialog());

        findViewById(R.id.btn_privacy_policy).setOnClickListener(v -> {
            openUrl("https://kavychaturvedi5427.github.io/StealthPad/stealthpad-privacy.html");
        });

        findViewById(R.id.btn_terms).setOnClickListener(v -> {
            openUrl("https://kavychaturvedi5427.github.io/StealthPad/stealthpad-terms.html");
        });

        findViewById(R.id.btn_faq).setOnClickListener(v -> {
            openUrl("https://kavychaturvedi5427.github.io/StealthPad/stealthpad-faq.html");
        });
    }

    private void openUrl(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No browser found to open link", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateSettingsUI() {
        // Theme
        String[] themes = {getString(R.string.theme_system), getString(R.string.theme_light), getString(R.string.theme_dark)};
        currentThemeText.setText(themes[sessionManager.getThemeMode()]);

        // Biometric
        switchBiometric.setChecked(sessionManager.isBiometricEnabled());
        if (!BiometricHelper.isBiometricAvailable(this)) {
            switchBiometric.setEnabled(false);
        }

        // Auto Lock
        int minutes = sessionManager.getAutoLockMinutes();
        if (minutes == 0) {
            currentAutoLockText.setText(R.string.lock_immediately);
        } else {
            String timeText = getString(minutes == 1 ? R.string.lock_after_1_min : 
                             (minutes == 5 ? R.string.lock_after_5_min : R.string.lock_after_15_min));
            currentAutoLockText.setText(timeText);
        }

        // Sorting
        String sortOrder = sessionManager.getSortOrder();
        currentSortText.setText(formatSortOrder(sortOrder));

        // Category
        currentCategoryText.setText(sessionManager.getDefaultCategory());
    }

    private String formatSortOrder(String order) {
        if (order == null) return getString(R.string.sort_recently_updated);
        switch (order) {
            case "NEWEST_CREATED": return getString(R.string.sort_newest);
            case "OLDEST_CREATED": return getString(R.string.sort_oldest);
            case "ALPHABETICAL": return getString(R.string.sort_alphabetical);
            default: return getString(R.string.sort_recently_updated);
        }
    }

    private void showThemeDialog() {
        String[] items = {"System Default", "Light", "Dark"};
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.settings_change_theme)
                .setSingleChoiceItems(items, sessionManager.getThemeMode(), (dialog, which) -> {
                    sessionManager.setThemeMode(which);
                    applyTheme(which);
                    updateSettingsUI();
                    dialog.dismiss();
                })
                .show();
    }

    private void applyTheme(int mode) {
        switch (mode) {
            case 0: AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM); break;
            case 1: AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); break;
            case 2: AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES); break;
        }
    }

    private void showAutoLockDialog() {
        String[] items = {"Immediately", "After 1 minute", "After 5 minutes", "After 15 minutes"};
        int[] values = {0, 1, 5, 15};
        int currentMinutes = sessionManager.getAutoLockMinutes();
        int selection = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i] == currentMinutes) {
                selection = i;
                break;
            }
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.settings_auto_lock)
                .setSingleChoiceItems(items, selection, (dialog, which) -> {
                    sessionManager.setAutoLockMinutes(values[which]);
                    updateSettingsUI();
                    dialog.dismiss();
                })
                .show();
    }

    private void showSortDialog() {
        String[] items = {"Recently Updated", "Newest Created", "Oldest Created", "Alphabetical"};
        String[] values = {"RECENTLY_UPDATED", "NEWEST_CREATED", "OLDEST_CREATED", "ALPHABETICAL"};
        
        String currentOrder = sessionManager.getSortOrder();
        int selection = 0;
        for (int i = 0; i < values.length; i++) {
            if (Objects.equals(values[i], currentOrder)) {
                selection = i;
                break;
            }
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.settings_sort_notes)
                .setSingleChoiceItems(items, selection, (dialog, which) -> {
                    sessionManager.setSortOrder(values[which]);
                    updateSettingsUI();
                    dialog.dismiss();
                })
                .show();
    }

    private void showCategoryDialog() {
        String[] items = {"Personal", "Work", "Ideas", "Important", "Secure"};
        String currentCategory = sessionManager.getDefaultCategory();
        int selection = 0;
        for (int i = 0; i < items.length; i++) {
            if (Objects.equals(items[i], currentCategory)) {
                selection = i;
                break;
            }
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.settings_default_category)
                .setSingleChoiceItems(items, selection, (dialog, which) -> {
                    sessionManager.setDefaultCategory(items[which]);
                    updateSettingsUI();
                    dialog.dismiss();
                })
                .show();
    }

    private void showClearStorageDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.settings_clear_storage_confirm_title)
                .setMessage(R.string.settings_clear_storage_confirm_msg)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.settings_clear_storage, (dialog, which) -> {
                    String email = sessionManager.getEmail();
                    if (email != null) executeSecureWipe(email);
                })
                .show();
    }

    private void showAboutDialog() {
        AboutDialog aboutDialog = new AboutDialog();
        aboutDialog.show(getSupportFragmentManager(), "AboutDialog");
    }

    private void checkPendingSyncAndLogout() {
        if (isLoggingOut) return;
        
        String email = sessionManager.getEmail();
        if (email == null) {
            performLogout();
            return;
        }

        isLoggingOut = true;
        sessionManager.setLoggingOut(true);
        
        // Show "Securing data" dialog
        com.google.android.material.dialog.MaterialAlertDialogBuilder progressBuilder = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Securing your data")
                .setMessage("Please wait while we synchronize your notes...")
                .setCancelable(false);
        
        // Add a progress bar manually
        android.widget.ProgressBar progressBar = new android.widget.ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setIndeterminate(true);
        progressBar.setPadding(60, 20, 60, 20);
        progressBuilder.setView(progressBar);
        
        androidx.appcompat.app.AlertDialog progressDialog = progressBuilder.show();

        // Perform final sync on a background thread
        new Thread(() -> {
            try {
                boolean hasPending = notesRepository.hasPendingSyncSync(email);
                
                if (hasPending) {
                    // Attempt to push changes
                    boolean syncSuccess = notesRepository.pushPendingChangesSync(email);
                    
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        if (syncSuccess) {
                            // Success! Proceed with wipe
                            executeSecureWipe(email);
                        } else {
                            // Failed to sync (e.g. offline)
                            isLoggingOut = false;
                            sessionManager.setLoggingOut(false);
                            showUnsyncedWarningDialog(email);
                        }
                    });
                } else {
                    // No pending changes, proceed to wipe
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        executeSecureWipe(email);
                    });
                }
            } catch (Exception e) {
                    runOnUiThread(() -> {
                        if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
                        isLoggingOut = false;
                        sessionManager.setLoggingOut(false);
                        Toast.makeText(this, "Logout failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();
        }

        private void showUnsyncedWarningDialog(String email) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Unsynced Notes")
                    .setMessage("Some notes couldn't be saved to the cloud. Logging out and clearing this device will permanently delete those unsynchronized notes.")
                    .setPositiveButton("Logout & Delete", (dialog, which) -> {
                        isLoggingOut = true;
                        sessionManager.setLoggingOut(true);
                        executeSecureWipe(email);
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        isLoggingOut = false;
                        sessionManager.setLoggingOut(false);
                    })
                    .setCancelable(false)
                    .show();
        }

    private void executeSecureWipe(String email) {
        // This MUST be run on a background thread as it performs DB operations
        new Thread(() -> {
            // Stop any ongoing sync work
            SyncScheduler.stopAllSync(this);
            
            // Clear local notes (synchronously)
            notesRepository.deleteAllNotesSync(email);
            
            // Clear attachments files
            storageManager.deleteAllAttachments();
            
            runOnUiThread(this::performLogout);
        }).start();
    }

    private void performLogout() {
        sessionManager.logout();
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
