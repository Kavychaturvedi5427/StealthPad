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
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.kavya.stealthpad.R;
import com.kavya.stealthpad.ViewModel.AuthViewModel.AuthState;
import com.kavya.stealthpad.ViewModel.AuthViewModel.AuthViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AuthDialogFragment extends DialogFragment {

    private AuthViewModel authViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        View view = inflater.inflate(R.layout.dialog_auth, container, false);

        TextInputEditText email_inp = view.findViewById(R.id.email_input);
        TextInputEditText pass_inp = view.findViewById(R.id.password_input);
        TextInputLayout email_lay = view.findViewById(R.id.email_lay);
        TextInputLayout pass_lay = view.findViewById(R.id.pass_lay);

        view.findViewById(R.id.btn_unlock).setOnClickListener(v1 -> {
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

        authViewModel.getAuthState().observe(getViewLifecycleOwner(), state -> {
            if (state instanceof AuthState.Success) {

                dismiss();
            } else if (state instanceof AuthState.Error) {
                Toast.makeText(getContext(), ((AuthState.Error) state).getError(), Toast.LENGTH_SHORT).show();
            }
        });

        observeAuthState();
        return view;
    }

    private void observeAuthState() {

        authViewModel.getAuthState().observe(
                getViewLifecycleOwner(),
                state -> {

                    if(state instanceof AuthState.Loading){

                    }
                    else if(state instanceof AuthState.Success){

                        dismiss(); // close dialog

                    }
                    else if(state instanceof AuthState.Error){

                        dismiss();
                        String error =
                                ((AuthState.Error) state)
                                        .getError();

                        Snackbar.make(
                                requireView(),
                                error,
                                Snackbar.LENGTH_SHORT
                        ).show();
                    }
                }
        );
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
