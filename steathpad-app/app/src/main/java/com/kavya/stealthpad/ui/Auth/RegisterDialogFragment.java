package com.kavya.stealthpad.ui.Auth;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.kavya.stealthpad.R;

import com.kavya.stealthpad.ViewModel.AuthViewModel.AuthState;
import com.kavya.stealthpad.ViewModel.AuthViewModel.AuthViewModel;
import com.kavya.stealthpad.data.DataModel.AuthResponseDto;
import com.kavya.stealthpad.utils.SessionManager;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RegisterDialogFragment extends DialogFragment {

    private AuthViewModel authViewModel;
    private MaterialButton register_btn;
    private View view;

    private CircularProgressIndicator progessIndi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.dialog_register, container, false);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        progessIndi = view.findViewById(R.id.loading_indicator);

        register_btn = view.findViewById(R.id.btn_register);
        register_btn.setOnClickListener(v->{
            Log.d("REGISTER", "Button clicked");

            TextInputEditText fullname = view.findViewById(R.id.name_input);
            TextInputEditText email = view.findViewById(R.id.email_input);
            TextInputEditText password = view.findViewById(R.id.password_input);
            TextInputEditText confirmPass = view.findViewById(R.id.confirm_password_input);

            String name = fullname.getText().toString().trim();
            String em = email.getText().toString().trim();
            String ps = password.getText().toString().trim();
            String cps = confirmPass.getText().toString().trim();

            if(name.isEmpty()){
                fullname.setError("Can't leave the name empty");
            } else if(em.isEmpty()){
                email.setError("Email required");
            } else if(ps.isEmpty()){
                password.setError("Password required");
            } else if(cps.isEmpty()){
                confirmPass.setError("Re-enter password to confirm");
            } else if(!ps.equals(cps)){
                confirmPass.setError("Passwords do not match");
            } else {
                authViewModel.register(name, em, ps);
            }
        });

        TextView signin = view.findViewById(R.id.btn_sign_in);
        signin.setOnClickListener(v->{
            dismiss();
            AuthDialogFragment authDia = new AuthDialogFragment();
            authDia.show(getParentFragmentManager(), "AUTH_Dia");
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        observeAuthState();
    }

    private void observeAuthState(){
        authViewModel.getAuthState().observe(getViewLifecycleOwner(), state->{
            if(state instanceof AuthState.Loading){
                showLoading();
            }
            else if(state instanceof AuthState.Success){
                AuthResponseDto authResponseDto = ((AuthState.Success) state).getAuthResponseDto();
                handleSuccess(authResponseDto);
                Toast.makeText(requireContext(), "Registration Successful", Toast.LENGTH_SHORT).show();
                hideLoading();
                dismiss();
            } else if (state instanceof AuthState.Error) {
                hideLoading();
                String mess = ((AuthState.Error) state).getError();
                Toast.makeText(requireContext(), mess, Toast.LENGTH_SHORT).show();
                String message = ((AuthState.Error) state).getError();
                Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void handleSuccess(AuthResponseDto authResponseDto) {
        SessionManager sessionManager = new SessionManager(requireContext());
        sessionManager.saveUser(authResponseDto.getJwt(),
                authResponseDto.getName(),
                authResponseDto.getEmail()
                );
        authViewModel.checkAuth(sessionManager);
    }

    private void showLoading() {
        progessIndi.setVisibility(View.VISIBLE);
        register_btn.setEnabled(false);
    }

    private void hideLoading() {
        progessIndi.setVisibility(View.GONE);
        register_btn.setEnabled(true);
    }

    // this is to make the container that is holding the dialog box transparent
    @Override
    public void onStart() {
        super.onStart();
        if(getDialog() != null && getDialog().getWindow() != null){
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            getDialog().getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
