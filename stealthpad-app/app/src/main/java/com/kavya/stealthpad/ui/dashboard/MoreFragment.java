package com.kavya.stealthpad.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.kavya.stealthpad.R;
import com.kavya.stealthpad.ViewModel.AuthViewModel.AuthState;
import com.kavya.stealthpad.ViewModel.AuthViewModel.AuthViewModel;
import com.kavya.stealthpad.ui.Auth.ManageAccountActivity;
import com.kavya.stealthpad.utils.SessionManager;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MoreFragment extends Fragment {

    @Inject
    SessionManager sessionManager;

    private AuthViewModel authViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_more, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        view.findViewById(R.id.btn_more_manage_account).setOnClickListener(v -> {
            if (!sessionManager.isLoggedIn()) {
                Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(requireContext(), ManageAccountActivity.class);
            startActivity(intent);
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        view.findViewById(R.id.btn_more_settings).setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), SettingsActivity.class);
            startActivity(intent);
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        view.findViewById(R.id.btn_logout).setOnClickListener(v -> {
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Logout", (dialog, which) -> performLogout())
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        observeAuthState();
    }

    private void observeAuthState() {
        authViewModel.getAuthState().observe(getViewLifecycleOwner(), state -> {
            if (state instanceof AuthState.DeleteAccountSuccess) {
                Toast.makeText(requireContext(), "Account deleted successfully", Toast.LENGTH_SHORT).show();
                performLogout();
                authViewModel.resetState();
            } else if (state instanceof AuthState.Error) {
                // Check if it's currently showing MoreFragment before Toasting to avoid multiple toasts
                if (isVisible()) {
                    Toast.makeText(requireContext(), ((AuthState.Error) state).getError(), Toast.LENGTH_SHORT).show();
                    authViewModel.resetState();
                }
            }
        });
    }

    private void performLogout() {
        sessionManager.logout();
        Intent intent = new Intent(requireContext(), DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
