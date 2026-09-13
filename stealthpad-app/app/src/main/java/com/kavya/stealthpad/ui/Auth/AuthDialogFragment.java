package com.kavya.stealthpad.ui.Auth;

import android.content.Context;
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
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.kavya.stealthpad.R;
import com.kavya.stealthpad.ViewModel.AuthViewModel.AuthState;
import com.kavya.stealthpad.ViewModel.AuthViewModel.AuthViewModel;
import com.kavya.stealthpad.data.DataModel.AuthResponseDto;
import com.kavya.stealthpad.synchronization.SyncScheduler;
import com.kavya.stealthpad.utils.BiometricHelper;
import com.kavya.stealthpad.utils.EncryptionManager;
import com.kavya.stealthpad.utils.SessionManager;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AuthDialogFragment extends DialogFragment {

    @Inject
    EncryptionManager encryptionManager;
    
    private AuthViewModel authViewModel;
    private CircularProgressIndicator progressindi;
    private MaterialButton unlock;
    private SessionManager sessionManager;
    private String lastInputPass;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        sessionManager = new SessionManager(requireContext());
        View view = inflater.inflate(R.layout.dialog_auth, container, false);

        unlock = view.findViewById(R.id.btn_unlock);
        progressindi = view.findViewById(R.id.loading_indicator);
        TextInputEditText email_inp = view.findViewById(R.id.email_input);
        TextInputEditText pass_inp = view.findViewById(R.id.password_input);
        TextInputLayout email_lay = view.findViewById(R.id.email_lay);
        TextInputLayout pass_lay = view.findViewById(R.id.pass_lay);
        android.widget.CheckBox cbRemember = view.findViewById(R.id.cb_remember);
        View biometricBtn = view.findViewById(R.id.btn_biometric_auth);
        View biometricContainer = view.findViewById(R.id.biometric_container);

        // Pre-fill email if saved
        if (sessionManager.getSavedEmail() != null) {
            email_inp.setText(sessionManager.getSavedEmail());
        }

        view.findViewById(R.id.btn_close_auth).setOnClickListener(v -> dismiss());

        unlock.setOnClickListener(v1 -> {
            String email = email_inp.getText().toString().trim();
            String pass = pass_inp.getText().toString().trim();

            email_lay.setError(null);
            pass_lay.setError(null);

            if (email.isEmpty()) {
                email_lay.setError("Email is required");
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                email_lay.setError("Enter a valid email address");
            } else if (pass.isEmpty()) {
                pass_lay.setError("Password required");
            } else if (pass.length() < 6) {
                pass_lay.setError("Password must be at least 6 characters");
            } else {
                if (cbRemember.isChecked()) {
                    lastInputPass = pass;
                } else {
                    sessionManager.clearSavedCredentials();
                    lastInputPass = null;
                }
                authViewModel.login(email, pass);
            }
        });

        // Show biometric only if enabled AND we have saved credentials to use
        boolean canBiometric = BiometricHelper.isBiometricAvailable(requireContext()) 
                && sessionManager.isBiometricEnabled()
                && sessionManager.getSavedPassword() != null;

        biometricContainer.setVisibility(canBiometric ? View.VISIBLE : View.GONE);

        biometricBtn.setOnClickListener(v -> {
            startBiometricAuth();
        });

        view.findViewById(R.id.btn_forgot_password).setOnClickListener(v -> {
            dismiss();
            ForgotPasswordDialog forgotDialog = new ForgotPasswordDialog();
            forgotDialog.show(getParentFragmentManager(), "ForgotPasswordDialog");
        });

        MaterialButton registerBtn = view.findViewById(R.id.btn_create_account);
        registerBtn.setOnClickListener(v -> {
            dismiss();
            RegisterDialogFragment dia = new RegisterDialogFragment();
            dia.show(getParentFragmentManager(), "REGISTER_Dia");
        });

        // observing and updating the ui based on the changes in the states.....
        observeAuthState();
        return view;
    }

    private void startBiometricAuth() {
        BiometricHelper.showBiometricPrompt(
                this,
                "Biometric Login",
                "Log in to your StealthPad account",
                "Use Password",
                new BiometricHelper.BiometricCallback() {
                    @Override
                    public void onAuthenticationSuccess() {
                        String email = sessionManager.getSavedEmail();
                        String encryptedPass = sessionManager.getSavedPassword();
                        
                        if (email != null && encryptedPass != null) {
                            String password = encryptionManager.decrypt(encryptedPass);
                            if (password != null) {
                                authViewModel.login(email, password);
                            } else {
                                Toast.makeText(requireContext(), "Failed to decrypt credentials", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(requireContext(), "Please login with password once and check 'Remember me' to enable biometrics", Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );
    }

    private void observeAuthState() {

        authViewModel.getAuthState().observe(
                getViewLifecycleOwner(),
                state -> {

                    if(state instanceof AuthState.Loading){
                        showLoading();
                    }
                    else if(state instanceof AuthState.Success){
                        hideLoading();
                        Toast.makeText(requireContext(), "Login Successful", Toast.LENGTH_SHORT).show();

                        // storing the jwt in the sharedprefs so that user is logged in even after closing the app..
                        AuthResponseDto authResponseDto = ((AuthState.Success) state).getAuthResponseDto();
                        handleSuccess(authResponseDto);

                        // start sync of the notes on login... but we also need to sync if the user is already logged in....
                        Context appContext = requireContext().getApplicationContext();
                        SyncScheduler.syncNow(appContext, authResponseDto.getEmail());
                        SyncScheduler.schedulerPeriodicSync(appContext, authResponseDto.getEmail());

                        // Reset state and dismiss
                        authViewModel.resetState();
                        dismiss(); // close dialog

                    }
                    else if(state instanceof AuthState.Error){
                        hideLoading();
                        String error = ((AuthState.Error) state).getError();
                        Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
                        authViewModel.resetState();
                    }
                }
        );
    }

    private void handleSuccess(AuthResponseDto authResponseDto){
        SessionManager sessionManager = new SessionManager(requireContext());
        sessionManager.saveUser(authResponseDto.getJwt(),
                authResponseDto.getName(),
                authResponseDto.getEmail()
                );
        
        // Save encrypted password for biometric login if needed
        if (lastInputPass != null) {
            String encrypted = encryptionManager.encrypt(lastInputPass);
            sessionManager.saveCredentials(authResponseDto.getEmail(), encrypted);
            lastInputPass = null;
        }

        authViewModel.checkAuth(sessionManager);
    }

    private void showLoading() {
        progressindi.setVisibility(View.VISIBLE);
        unlock.setEnabled(false);
    }

    private void hideLoading() {
        progressindi.setVisibility(View.GONE);
        unlock.setEnabled(true);
    }


    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            getDialog().getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }
}
