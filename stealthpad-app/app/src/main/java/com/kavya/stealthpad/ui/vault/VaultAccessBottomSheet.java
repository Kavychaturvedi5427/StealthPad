package com.kavya.stealthpad.ui.vault;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.kavya.stealthpad.R;
import com.kavya.stealthpad.utils.BiometricHelper;
import com.kavya.stealthpad.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class VaultAccessBottomSheet extends BottomSheetDialogFragment {

    public interface VaultAuthListener {
        void onVaultAuthenticated();
        default void onVaultAuthCancelled() {}
        default void onVaultReset() {}
    }

    private VaultAuthListener listener;
    private SessionManager sessionManager;
    private List<ImageView> pinDots = new ArrayList<>();
    private StringBuilder enteredPin = new StringBuilder();
    private String savedPin;
    
    private TextView errorText;
    private View pinContainer;

    public void setVaultAuthListener(VaultAuthListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.AppBottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_vault_access_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        savedPin = sessionManager.getVaultPin();

        initViews(view);
        setupKeypad(view);
        checkBiometric(view);
    }

    private void initViews(View view) {
        errorText = view.findViewById(R.id.error_text);
        pinContainer = view.findViewById(R.id.pin_indicator_container);

        pinDots.add(view.findViewById(R.id.pin_dot_1));
        pinDots.add(view.findViewById(R.id.pin_dot_2));
        pinDots.add(view.findViewById(R.id.pin_dot_3));
        pinDots.add(view.findViewById(R.id.pin_dot_4));

        view.findViewById(R.id.btn_forgot_pin).setOnClickListener(v -> showForgotPinDialog());
    }

    private void setupKeypad(View view) {
        View.OnClickListener numberListener = v -> {
            if (enteredPin.length() < 4) {
                String digit = ((MaterialButton) v).getText().toString();
                enteredPin.append(digit);
                updatePinIndicators();
                
                if (enteredPin.length() == 4) {
                    verifyPin();
                }
            }
        };

        android.widget.GridLayout grid = view.findViewById(R.id.keypad_grid);
        for (int i = 0; i < grid.getChildCount(); i++) {
            View child = grid.getChildAt(i);
            if (child instanceof MaterialButton) {
                MaterialButton btn = (MaterialButton) child;
                if (btn.getText() != null && !btn.getText().toString().isEmpty()) {
                    btn.setOnClickListener(numberListener);
                }
            }
        }

        view.findViewById(R.id.btn_delete).setOnClickListener(v -> {
            if (enteredPin.length() > 0) {
                enteredPin.deleteCharAt(enteredPin.length() - 1);
                updatePinIndicators();
                errorText.setVisibility(View.INVISIBLE);
            }
        });

        view.findViewById(R.id.btn_biometric).setOnClickListener(v -> startBiometricAuth());
    }

    private void updatePinIndicators() {
        for (int i = 0; i < 4; i++) {
            if (i < enteredPin.length()) {
                pinDots.get(i).setImageResource(R.drawable.pin_dot_filled);
            } else {
                pinDots.get(i).setImageResource(R.drawable.pin_dot_empty);
            }
        }
    }

    private void verifyPin() {
        if (enteredPin.toString().equals(savedPin)) {
            authenticateSuccess();
        } else {
            handleIncorrectPin();
        }
    }

    private void handleIncorrectPin() {
        errorText.setVisibility(View.VISIBLE);
        Animation shake = AnimationUtils.loadAnimation(requireContext(), android.R.anim.fade_in); 
        pinContainer.startAnimation(shake);
        
        pinContainer.postDelayed(() -> {
            enteredPin.setLength(0);
            updatePinIndicators();
        }, 300);
    }

    private void checkBiometric(View view) {
        boolean available = BiometricHelper.isBiometricAvailable(requireContext()) && sessionManager.isBiometricEnabled();
        view.findViewById(R.id.btn_biometric).setVisibility(available ? View.VISIBLE : View.INVISIBLE);
        
        if (available) {
            startBiometricAuth();
        }
    }

    private void startBiometricAuth() {
        BiometricHelper.showBiometricPrompt(
                this,
                "Unlock Vault",
                "Authenticate to access your private vault",
                "Use PIN",
                new BiometricHelper.BiometricCallback() {
                    @Override
                    public void onAuthenticationSuccess() {
                        authenticateSuccess();
                    }
                }
        );
    }

    private void authenticateSuccess() {
        if (listener != null) {
            listener.onVaultAuthenticated();
        }
        dismiss();
    }

    private void showForgotPinDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.vault_reset_confirm_title)
                .setMessage(R.string.vault_reset_confirm_msg)
                .setPositiveButton("Reset Vault", (dialog, which) -> {
                    sessionManager.setVaultSetup(false);
                    sessionManager.setVaultPin(null);
                    Toast.makeText(requireContext(), R.string.vault_reset_success, Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onVaultReset();
                    }
                    dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onCancel(@NonNull android.content.DialogInterface dialog) {
        super.onCancel(dialog);
        if (listener != null) {
            listener.onVaultAuthCancelled();
        }
    }
}
