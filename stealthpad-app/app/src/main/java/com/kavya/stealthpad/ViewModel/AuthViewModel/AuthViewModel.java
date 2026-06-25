package com.kavya.stealthpad.ViewModel.AuthViewModel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.kavya.stealthpad.data.DataModel.AuthResponseDto;
import com.kavya.stealthpad.data.DataModel.LoginRequestDTO;
import com.kavya.stealthpad.data.DataModel.RegisterRequestDTO;
import com.kavya.stealthpad.data.repository.Auth.AuthRepository;
import com.kavya.stealthpad.utils.SessionManager;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@HiltViewModel
public class AuthViewModel extends ViewModel {

    private final AuthRepository authRepository;

    private final MutableLiveData<AuthState> authState = new MutableLiveData<>();

    public LiveData<AuthState> getAuthState(){
        return authState;
    }

    @Inject
    public AuthViewModel(AuthRepository repo){
        this.authRepository = repo;
    }
    public void login(String em, String ps){
        authState.setValue(new AuthState.Loading());
        LoginRequestDTO loginRequest = new LoginRequestDTO(em, ps);
        authRepository.login(loginRequest).enqueue(new Callback<AuthResponseDto>() { // enqueue the login call in the task stack..
            @Override
            public void onResponse(Call<AuthResponseDto> call, Response<AuthResponseDto> response) {
                if(response.isSuccessful() && response.body() != null){
                    authState.setValue(new AuthState.Success(response.body()));
                }
                else{
                    authState.setValue(new AuthState.Error("Login failed"));
                }
            }

            @Override
            public void onFailure(Call<AuthResponseDto> call, Throwable throwable) {
                authState.setValue(new AuthState.Error(throwable.getMessage()));
            }
        });
    }
    public void register(String name, String em, String ps){
        authState.setValue(new AuthState.Loading());
        RegisterRequestDTO registerRequestDTO = new RegisterRequestDTO(name, em, ps);
        authRepository.register(registerRequestDTO).enqueue(new Callback<AuthResponseDto>() {
            @Override
            public void onResponse(Call<AuthResponseDto> call, Response<AuthResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    authState.setValue(new AuthState.Success(response.body()));
                } else {

                    authState.setValue(new AuthState.Error("Registration failed"));
                }
            }

            @Override
            public void onFailure(Call<AuthResponseDto> call, Throwable throwable) {
                authState.setValue(new AuthState.Error(throwable.getMessage()));
            }
        });
    }

    public void checkAuth(SessionManager sessionManager){
        if(sessionManager.isLoggedIn()){
            authState.setValue(new AuthState.LoggedIn());
        }
        else{
            authState.setValue(new AuthState.LoggedOut());
        }
    }

    public void resetState(){
        authState.setValue(new AuthState.Idle());
    }

}
