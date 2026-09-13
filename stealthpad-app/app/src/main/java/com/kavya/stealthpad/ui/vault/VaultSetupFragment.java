package com.kavya.stealthpad.ui.vault;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.kavya.stealthpad.R;
import com.kavya.stealthpad.ui.dashboard.DashboardActivity;
import com.kavya.stealthpad.utils.BiometricHelper;
import com.kavya.stealthpad.utils.SessionManager;

import java.util.concurrent.Executor;

public class VaultSetupFragment extends Fragment {

    private SessionManager sessionManager;
    private TextInputLayout pinLayout, confirmPinLayout;
    private TextInputEditText pinInput, confirmPinInput;
    private MaterialButton setupBtn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_vault_setup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        sessionManager = new SessionManager(requireContext());
        pinLayout = view.findViewById(R.id.pin_layout);
        confirmPinLayout = view.findViewById(R.id.confirm_pin_layout);
        pinInput = view.findViewById(R.id.pin_input);
        confirmPinInput = view.findViewById(R.id.confirm_pin_input);
        setupBtn = view.findViewById(R.id.btn_setup_vault);

        setupTextWatchers();

        // Auto focus first field
        pinInput.requestFocus();

        setupBtn.setOnClickListener(v -> {
            String pin = pinInput.getText().toString();
            String confirmPin = confirmPinInput.getText().toString();

            if (pin.length() != 4) {
                pinLayout.setError("PIN must be 4 digits");
                return;
            }

            if (!pin.equals(confirmPin)) {
                confirmPinLayout.setError("PINs do not match");
                return;
            }

            sessionManager.setVaultPin(pin);
            sessionManager.setVaultSetup(true);
            
            checkBiometricAvailabilityAndAsk();
        });
    }

    private void setupTextWatchers() {
        TextWatcher commonWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateInputs();
            }
        };

        pinInput.addTextChangedListener(commonWatcher);
        confirmPinInput.addTextChangedListener(commonWatcher);
    }

    private void validateInputs() {
        String pin = pinInput.getText().toString();
        String confirmPin = confirmPinInput.getText().toString();

        boolean isPinValid = pin.length() == 4;
        boolean isConfirmValid = confirmPin.length() == 4;
        boolean isMatch = pin.equals(confirmPin);

        // Subtle error handling: Clear error if user starts typing again
        if (!pin.isEmpty()) pinLayout.setError(null);
        if (!confirmPin.isEmpty()) confirmPinLayout.setError(null);

        if (isConfirmValid && !isMatch) {
            confirmPinLayout.setError("PINs do not match");
        } else {
            confirmPinLayout.setError(null);
        }

        setupBtn.setEnabled(isPinValid && isConfirmValid && isMatch);
    }

    private void checkBiometricAvailabilityAndAsk() {
        if (BiometricHelper.isBiometricAvailable(requireContext())) {
            showBiometricEnableDialog();
        } else {
            finishSetup();
        }
    }

    private void showBiometricEnableDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Enable Biometric Unlock")
                .setMessage("Use your fingerprint or face to unlock your private vault faster.")
                .setPositiveButton("Enable", (dialog, which) -> {
                    launchBiometricPrompt();
                })
                .setNegativeButton("Not Now", (dialog, which) -> {
                    sessionManager.setBiometricEnabled(false);
                    finishSetup();
                })
                .setCancelable(false)
                .show();
    }

    private void launchBiometricPrompt() {
        BiometricHelper.showBiometricPrompt(
                this,
                "Unlock Vault",
                "Authenticate to access your private vault",
                "Use PIN",
                new BiometricHelper.BiometricCallback() {
                    @Override
                    public void onAuthenticationSuccess() {
                        sessionManager.setBiometricEnabled(true);
                        finishSetup();
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        sessionManager.setBiometricEnabled(false);
                        finishSetup();
                    }
                }
        );
    }

    private void finishSetup() {
        Toast.makeText(requireContext(), "Vault Setup Successful", Toast.LENGTH_SHORT).show();
        
        if (getActivity() instanceof DashboardActivity) {
            DashboardActivity dashboard = (DashboardActivity) getActivity();
            dashboard.setVaultAuthenticated(true);
            dashboard.navigateTo(R.id.nav_vault);
        }
    }
}
