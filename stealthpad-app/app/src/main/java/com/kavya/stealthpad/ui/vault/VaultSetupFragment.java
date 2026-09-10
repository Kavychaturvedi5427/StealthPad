package com.kavya.stealthpad.ui.vault;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.kavya.stealthpad.R;
import com.kavya.stealthpad.ui.dashboard.DashboardActivity;
import com.kavya.stealthpad.utils.SessionManager;

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
            
            Toast.makeText(requireContext(), "Vault Setup Successful", Toast.LENGTH_SHORT).show();
            
            if (getActivity() instanceof DashboardActivity) {
                DashboardActivity dashboard = (DashboardActivity) getActivity();
                dashboard.setVaultAuthenticated(true);
                dashboard.navigateTo(R.id.nav_vault);
            }
        });
    }
}
