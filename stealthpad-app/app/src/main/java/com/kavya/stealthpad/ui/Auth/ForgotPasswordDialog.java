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
public class ForgotPasswordDialog extends DialogFragment {

    private static final String ARG_EMAIL = "email";
    private String email;
    private AuthViewModel authViewModel;
    private CircularProgressIndicator progressindi;
    private MaterialButton btnSendOtp;
    private TextInputEditText emailInput;
    private TextInputLayout emailLayout;

    public static ForgotPasswordDialog newInstance(String email) {
        ForgotPasswordDialog fragment = new ForgotPasswordDialog();
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
        View view = inflater.inflate(R.layout.dialog_forgot_password, container, false);

        btnSendOtp = view.findViewById(R.id.btn_send_otp);
        progressindi = view.findViewById(R.id.loading_indicator);
        emailInput = view.findViewById(R.id.email_input);
        emailLayout = view.findViewById(R.id.email_lay);

        if (email != null) {
            emailInput.setText(email);
            emailInput.setEnabled(false);
        }

        view.findViewById(R.id.btn_close_forgot).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.btn_back_to_login).setOnClickListener(v -> dismiss());

        btnSendOtp.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            if (email.isEmpty()) {
                emailLayout.setError("Email is required");
            } else {
                authViewModel.forgotPassword(email);
            }
        });

        observeAuthState();
        return view;
    }

    private void observeAuthState() {
        authViewModel.getAuthState().observe(getViewLifecycleOwner(), state -> {
            if (state instanceof AuthState.Loading) {
                showLoading();
            } else if (state instanceof AuthState.ForgotPassSuccess) {
                hideLoading();
                Toast.makeText(requireContext(), "OTP sent to your email", Toast.LENGTH_SHORT).show();
                
                String email = emailInput.getText().toString().trim();
                dismiss();
                
                ResetPasswordDialog resetDialog = ResetPasswordDialog.newInstance(email);
                resetDialog.show(getParentFragmentManager(), "ResetPasswordDialog");
                
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
        btnSendOtp.setEnabled(false);
    }

    private void hideLoading() {
        progressindi.setVisibility(View.GONE);
        btnSendOtp.setEnabled(true);
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
