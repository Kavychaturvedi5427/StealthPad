package com.kavya.stealthpad.ui.Auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.kavya.stealthpad.R;
import com.kavya.stealthpad.ViewModel.AuthViewModel.AuthState;
import com.kavya.stealthpad.ViewModel.AuthViewModel.AuthViewModel;
import com.kavya.stealthpad.data.repository.Notes.NotesRepository;
import com.kavya.stealthpad.ui.dashboard.DashboardActivity;
import com.kavya.stealthpad.utils.AttachmentStorageManager;
import com.kavya.stealthpad.utils.SessionManager;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ManageAccountActivity extends AppCompatActivity {

    @Inject
    SessionManager sessionManager;

    @Inject
    NotesRepository notesRepository;

    @Inject
    AttachmentStorageManager storageManager;

    private AuthViewModel authViewModel;
    private android.view.View loadingIndicator;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Edge-to-edge handling
        androidx.activity.EdgeToEdge.enable(this);
        
        setContentView(R.layout.activity_manage_account);

        // Apply window insets to root view to respect status bar
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        loadingIndicator = findViewById(R.id.loading_indicator);

        // Account Information
        TextView nameText = findViewById(R.id.info_name);
        TextView emailText = findViewById(R.id.info_email);
        TextView headerName = findViewById(R.id.header_user_name);
        TextView headerEmail = findViewById(R.id.header_user_email);
        
        String name = sessionManager.getName();
        String email = sessionManager.getEmail();
        
        String displayName = (name != null && !name.trim().isEmpty()) ? name : "Not available";
        String displayEmail = (email != null && !email.trim().isEmpty()) ? email : "Not available";

        nameText.setText(displayName);
        emailText.setText(displayEmail);
        headerName.setText(displayName);
        headerEmail.setText(displayEmail);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        findViewById(R.id.btn_reset_password).setOnClickListener(v -> {
            if (email != null) {
                authViewModel.forgotPassword(email);
            }
        });

        findViewById(R.id.btn_delete_account).setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.delete_account_title)
                    .setMessage(R.string.delete_account_confirm_msg)
                    .setPositiveButton("Delete Account", (dialog, which) -> {
                        authViewModel.deleteAccount();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        findViewById(R.id.btn_logout).setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Logout", (dialog, which) -> {
                        performLogout();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        observeAuthState();
    }

    private void observeAuthState() {
        authViewModel.getAuthState().observe(this, state -> {
            if (state instanceof AuthState.Loading) {
                if (loadingIndicator != null) loadingIndicator.setVisibility(android.view.View.VISIBLE);
            } else if (state instanceof AuthState.ForgotPassSuccess) {
                if (loadingIndicator != null) loadingIndicator.setVisibility(android.view.View.GONE);
                String userEmail = sessionManager.getEmail();
                ResetPasswordDialog resetDialog = ResetPasswordDialog.newInstance(userEmail);
                resetDialog.show(getSupportFragmentManager(), "ResetPasswordDialog");
                authViewModel.resetState();
            } else if (state instanceof AuthState.DeleteAccountSuccess) {
                if (loadingIndicator != null) loadingIndicator.setVisibility(android.view.View.GONE);
                Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                
                String userEmail = sessionManager.getEmail();
                if (userEmail != null) {
                    notesRepository.deleteAllNotes(userEmail);
                }
                storageManager.deleteAllAttachments();

                performLogout();
                authViewModel.resetState();
            } else if (state instanceof AuthState.Error) {
                if (loadingIndicator != null) loadingIndicator.setVisibility(android.view.View.GONE);
                Toast.makeText(this, ((AuthState.Error) state).getError(), Toast.LENGTH_SHORT).show();
                authViewModel.resetState();
            } else if (state instanceof AuthState.LoggedOut) {
                if (loadingIndicator != null) loadingIndicator.setVisibility(android.view.View.GONE);
                performLogout();
            }
        });
    }

    private void performLogout() {
        sessionManager.logout();
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
