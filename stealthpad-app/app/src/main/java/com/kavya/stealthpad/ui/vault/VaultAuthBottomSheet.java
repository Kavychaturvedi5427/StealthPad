package com.kavya.stealthpad.ui.vault;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.kavya.stealthpad.R;
import com.kavya.stealthpad.utils.SessionManager;

import java.util.concurrent.Executor;

public class VaultAuthBottomSheet extends BottomSheetDialogFragment {

    public interface VaultAuthListener {
        void onVaultAuthenticated();
        default void onVaultAuthCancelled() {}
    }

    private VaultAuthListener listener;
    private SessionManager sessionManager;
    private TextInputLayout pinLayout;
    private TextInputEditText pinInput;
    private MaterialCardView biometricBtn;
    private View loginBtn;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    public void setVaultAuthListener(VaultAuthListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.AppBottomSheetDialogTheme);
    }

    @Override
    public void onCancel(@NonNull android.content.DialogInterface dialog) {
        super.onCancel(dialog);
        if (listener != null) {
            listener.onVaultAuthCancelled();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_vault_auth_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        pinLayout = view.findViewById(R.id.auth_pin_layout);
        pinInput = view.findViewById(R.id.auth_pin_input);
        biometricBtn = view.findViewById(R.id.btn_biometric_unlock);
        loginBtn = view.findViewById(R.id.btn_vault_login);

        setupBiometric();

        loginBtn.setOnClickListener(v -> {
            String enteredPin = pinInput.getText().toString();
            String savedPin = sessionManager.getVaultPin();

            if (enteredPin.isEmpty()) {
                pinLayout.setError("Please enter PIN");
                return;
            }

            if (enteredPin.equals(savedPin)) {
                authenticateSuccess();
            } else {
                pinLayout.setError("Incorrect PIN");
            }
        });

        biometricBtn.setOnClickListener(v -> {
            if (isBiometricAvailable()) {
                biometricPrompt.authenticate(promptInfo);
            } else {
                Toast.makeText(requireContext(), "Biometric not available", Toast.LENGTH_SHORT).show();
            }
        });

        // Automatically show biometric prompt if available
        if (isBiometricAvailable()) {
            biometricPrompt.authenticate(promptInfo);
        }
    }

    private void setupBiometric() {
        executor = ContextCompat.getMainExecutor(requireContext());
        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                // Handled by standard BiometricPrompt UI or just ignored if user cancels
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                authenticateSuccess();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(requireContext(), "Biometric authentication failed", Toast.LENGTH_SHORT).show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Vault Authentication")
                .setSubtitle("Use your biometric to unlock the vault")
                .setNegativeButtonText("Use PIN")
                .build();
    }

    private boolean isBiometricAvailable() {
        BiometricManager biometricManager = BiometricManager.from(requireContext());
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS;
    }

    private void authenticateSuccess() {
        if (listener != null) {
            listener.onVaultAuthenticated();
        }
        dismiss();
    }
}
