package com.kavya.stealthpad.ui.Auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.kavya.stealthpad.R;
import com.kavya.stealthpad.ViewModel.AuthViewModel.AuthState;
import com.kavya.stealthpad.ViewModel.AuthViewModel.AuthViewModel;
import com.kavya.stealthpad.data.repository.Notes.NotesRepository;
import com.kavya.stealthpad.ui.dashboard.DashboardActivity;
import com.kavya.stealthpad.utils.AttachmentStorageManager;
import com.kavya.stealthpad.utils.SessionManager;

import javax.inject.Inject;

import androidx.lifecycle.ViewModelProvider;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileDialog extends DialogFragment {

    @Inject
    SessionManager sessionManager;

    private AuthViewModel authViewModel;
    private View loadingIndicator;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.dialog_profile, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        loadingIndicator = view.findViewById(R.id.loading_indicator);
        TextView username = view.findViewById(R.id.text_full_name);
        TextView emailText = view.findViewById(R.id.text_email);

        String name = sessionManager.getName();
        String email = sessionManager.getEmail();

        username.setText(name != null && !name.isEmpty() ? name : "Not available");
        emailText.setText(email != null && !email.isEmpty() ? email : "Not available");
        
        view.findViewById(R.id.btn_close_profile).setOnClickListener(v -> dismiss());

        view.findViewById(R.id.btn_manage_account).setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ManageAccountActivity.class);
            startActivity(intent);
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            dismiss();
        });

        view.findViewById(R.id.btn_reset_password).setOnClickListener(v -> {
            if (email != null) {
                authViewModel.forgotPassword(email);
            }
        });

        view.findViewById(R.id.btn_delete_account).setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.delete_account_title)
                    .setMessage(R.string.delete_account_confirm_msg)
                    .setPositiveButton("Delete Account", (dialog, which) -> {
                        authViewModel.deleteAccount();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
        
        view.findViewById(R.id.btn_logout).setOnClickListener(v -> {

            // confirmation for logout...
            new MaterialAlertDialogBuilder(requireContext())
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
        authViewModel.getAuthState().observe(getViewLifecycleOwner(), state -> {
            if (state instanceof AuthState.Loading) {
                if (loadingIndicator != null) loadingIndicator.setVisibility(View.VISIBLE);
            } else if (state instanceof AuthState.ForgotPassSuccess) {
                if (loadingIndicator != null) loadingIndicator.setVisibility(View.GONE);
                String userEmail = sessionManager.getEmail();
                dismiss();
                ResetPasswordDialog resetDialog = ResetPasswordDialog.newInstance(userEmail);
                resetDialog.show(getParentFragmentManager(), "ResetPasswordDialog");
                authViewModel.resetState();
            } else if (state instanceof AuthState.DeleteAccountSuccess) {
                if (loadingIndicator != null) loadingIndicator.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Account deleted successfully", Toast.LENGTH_SHORT).show();
                performLogout();
                authViewModel.resetState();
            } else if (state instanceof AuthState.Error) {
                if (loadingIndicator != null) loadingIndicator.setVisibility(View.GONE);
                Toast.makeText(requireContext(), ((AuthState.Error) state).getError(), Toast.LENGTH_SHORT).show();
                authViewModel.resetState();
            }
        });
    }

    private void performLogout() {
        sessionManager.logout();
        // clear the activity stack and redirect to the dashboard...
        Intent intent = new Intent(requireContext(), DashboardActivity.class);
        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );
        startActivity(intent);
        requireActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        dismiss();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(getDialog() != null && getDialog().getWindow() != null){
            // Set window background to transparent to see rounded corners
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            
            // Set width to 90% of screen width with 24dp approx margins
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            getDialog().getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
            
            // Optional: Center vertically
            android.view.WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
            params.gravity = android.view.Gravity.CENTER;
            getDialog().getWindow().setAttributes(params);
        }
    }
}
