package com.kavya.stealthpad.ui.Auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.kavya.stealthpad.R;
import com.kavya.stealthpad.ViewModel.AuthViewModel.AuthState;
import com.kavya.stealthpad.ViewModel.AuthViewModel.AuthViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ResetPasswordDialog extends DialogFragment {

    private static final String ARG_EMAIL = "email";
    private String email;
    private AuthViewModel authViewModel;
    private CircularProgressIndicator progressindi;
    private MaterialButton btnReset;
    private TextInputEditText otpInput, passInput, confirmPassInput, emailInput;
    private TextInputLayout otpLayout, passLayout, confirmPassLayout;

    public static ResetPasswordDialog newInstance(String email) {
        ResetPasswordDialog fragment = new ResetPasswordDialog();
        Bundle args = new Bundle();
        args.putString(ARG_EMAIL, email);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            email = getArguments().getString(ARG_EMAIL);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        View view = inflater.inflate(R.layout.dialog_reset_password, container, false);

        btnReset = view.findViewById(R.id.btn_reset_password);
        progressindi = view.findViewById(R.id.loading_indicator);
        emailInput = view.findViewById(R.id.email_input);
        otpInput = view.findViewById(R.id.otp_input);
        passInput = view.findViewById(R.id.password_input);
        confirmPassInput = view.findViewById(R.id.confirm_password_input);
        otpLayout = view.findViewById(R.id.otp_lay);
        passLayout = view.findViewById(R.id.pass_lay);
        confirmPassLayout = view.findViewById(R.id.confirm_pass_lay);

        if (email != null) {
            emailInput.setText(email);
            emailInput.setEnabled(false);
        }

        view.findViewById(R.id.btn_close_reset).setOnClickListener(v -> dismiss());

        btnReset.setOnClickListener(v -> {
            String enteredEmail = emailInput.getText().toString().trim();
            String otp = otpInput.getText().toString().trim();
            String newPass = passInput.getText().toString().trim();
            String confirmPass = confirmPassInput.getText().toString().trim();

            otpLayout.setError(null);
            passLayout.setError(null);
            confirmPassLayout.setError(null);

            if (otp.isEmpty()) {
                otpLayout.setError("OTP is required");
            } else if (newPass.isEmpty()) {
                passLayout.setError("New password is required");
            } else if (!newPass.equals(confirmPass)) {
                confirmPassLayout.setError("Passwords do not match");
            } else {
                authViewModel.resetPassword(enteredEmail, otp, newPass);
            }
        });

        observeAuthState();
        return view;
    }

    private void observeAuthState() {
        authViewModel.getAuthState().observe(getViewLifecycleOwner(), state -> {
            if (state instanceof AuthState.Loading) {
                showLoading();
            } else if (state instanceof AuthState.ResetPassSuccess) {
                hideLoading();
                Toast.makeText(requireContext(), "Password reset successful. Please login.", Toast.LENGTH_LONG).show();
                dismiss();
                authViewModel.resetState();
            } else if (state instanceof AuthState.Error) {
                hideLoading();
                Toast.makeText(requireContext(), ((AuthState.Error) state).getError(), Toast.LENGTH_SHORT).show();
                authViewModel.resetState();
            }
        });
    }

    private void showLoading() {
        progressindi.setVisibility(View.VISIBLE);
        btnReset.setEnabled(false);
    }

    private void hideLoading() {
        progressindi.setVisibility(View.GONE);
        btnReset.setEnabled(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            // Set window background to transparent to see rounded corners and margins
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            
            // Set width to 90% of screen width to ensure floating card look
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            getDialog().getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
            
            // Center in screen
            android.view.WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
            params.gravity = android.view.Gravity.CENTER;
            getDialog().getWindow().setAttributes(params);
        }
    }
}
