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
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.kavya.stealthpad.R;
import com.kavya.stealthpad.ViewModel.AuthViewModel.AuthState;
import com.kavya.stealthpad.ViewModel.AuthViewModel.AuthViewModel;
import com.kavya.stealthpad.data.DataModel.AuthResponseDto;
import com.kavya.stealthpad.utils.SessionManager;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AuthDialogFragment extends DialogFragment {

    private AuthViewModel authViewModel;
    private CircularProgressIndicator progressindi;
    private MaterialButton unlock;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        View view = inflater.inflate(R.layout.dialog_auth, container, false);

        unlock = view.findViewById(R.id.btn_unlock);
        progressindi = view.findViewById(R.id.loading_indicator);
        TextInputEditText email_inp = view.findViewById(R.id.email_input);
        TextInputEditText pass_inp = view.findViewById(R.id.password_input);
        TextInputLayout email_lay = view.findViewById(R.id.email_lay);
        TextInputLayout pass_lay = view.findViewById(R.id.pass_lay);

        view.findViewById(R.id.btn_close_auth).setOnClickListener(v -> dismiss());

        unlock.setOnClickListener(v1 -> {
            String email = email_inp.getText().toString().trim();
            String pass = pass_inp.getText().toString().trim();

            email_lay.setError(null);
            pass_lay.setError(null);

            if (email.isEmpty()) {
                email_lay.setError("Email is required");
            } else if (pass.isEmpty()) {
                pass_lay.setError("Password required");
            } else {
                authViewModel.login(email, pass);
            }
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
                        // recreate the dashboard for the user
                        requireActivity().recreate();
                        authViewModel.resetState();
                        dismiss(); // close dialog

                    }
                    else if(state instanceof AuthState.Error){
                        hideLoading();
                        String error =
                                ((AuthState.Error) state)
                                        .getError();
                        Toast.makeText(requireContext(), "Login Failed", Toast.LENGTH_SHORT).show();
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
