package com.kavya.stealthpad.ViewModel.AuthViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.kavya.stealthpad.data.DataModel.AuthResponseDto;
import com.kavya.stealthpad.data.DataModel.ForgotPassRequest;
import com.kavya.stealthpad.data.DataModel.LoginRequestDTO;
import com.kavya.stealthpad.data.DataModel.RegisterRequestDTO;
import com.kavya.stealthpad.data.DataModel.ResetPasswordRequest;
import com.kavya.stealthpad.data.DataModel.ApiError;
import com.kavya.stealthpad.data.repository.Auth.AuthRepository;
import com.kavya.stealthpad.utils.ApiErrorHandler;
import com.kavya.stealthpad.utils.SessionManager;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@HiltViewModel
public class AuthViewModel extends ViewModel {

    private final AuthRepository authRepository;
    private final ApiErrorHandler errorHandler;

    private final MutableLiveData<AuthState> authState = new MutableLiveData<>();

    public LiveData<AuthState> getAuthState(){
        return authState;
    }

    @Inject
    public AuthViewModel(AuthRepository repo, ApiErrorHandler errorHandler){
        this.authRepository = repo;
        this.errorHandler = errorHandler;
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
                    ApiError apiError = errorHandler.handleError(response);
                    // Special handling for login: Even if 401, we just want to show error
                    if (apiError.getStatusCode() == 401) {
                        authState.setValue(new AuthState.Error("Invalid email or password"));
                    } else {
                        authState.setValue(new AuthState.Error(apiError.getMessage()));
                    }
                }
            }

            @Override
            public void onFailure(Call<AuthResponseDto> call, Throwable throwable) {
                ApiError apiError = errorHandler.handleException(throwable);
                authState.setValue(new AuthState.Error(apiError.getMessage()));
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
                    ApiError apiError = errorHandler.handleError(response);
                    authState.setValue(new AuthState.Error(apiError.getMessage()));
                }
            }

            @Override
            public void onFailure(Call<AuthResponseDto> call, Throwable throwable) {
                ApiError apiError = errorHandler.handleException(throwable);
                authState.setValue(new AuthState.Error(apiError.getMessage()));
            }
        });
    }

    public void forgotPassword(String email) {
        authState.setValue(new AuthState.Loading());
        ForgotPassRequest request = new ForgotPassRequest(email);
        authRepository.forgotPassword(request).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    authState.setValue(new AuthState.ForgotPassSuccess(response.body() != null ? response.body() : "OTP sent successfully"));
                } else {
                    ApiError apiError = errorHandler.handleError(response);
                    if (apiError.isShouldLogout()) {
                        authState.setValue(new AuthState.LoggedOut());
                    } else {
                        authState.setValue(new AuthState.Error(apiError.getMessage()));
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable throwable) {
                ApiError apiError = errorHandler.handleException(throwable);
                authState.setValue(new AuthState.Error(apiError.getMessage()));
            }
        });
    }

    public void resetPassword(String email, String otp, String newPassword) {
        authState.setValue(new AuthState.Loading());
        ResetPasswordRequest request = new ResetPasswordRequest(email, otp, newPassword);
        authRepository.resetPassword(request).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    authState.setValue(new AuthState.ResetPassSuccess(response.body() != null ? response.body() : "Password reset successful"));
                } else {
                    ApiError apiError = errorHandler.handleError(response);
                    if (apiError.isShouldLogout()) {
                        authState.setValue(new AuthState.LoggedOut());
                    } else {
                        authState.setValue(new AuthState.Error(apiError.getMessage()));
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable throwable) {
                ApiError apiError = errorHandler.handleException(throwable);
                authState.setValue(new AuthState.Error(apiError.getMessage()));
            }
        });
    }

    public void deleteAccount() {
        authState.setValue(new AuthState.Loading());
        authRepository.deleteAccount().enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    authState.setValue(new AuthState.DeleteAccountSuccess(response.body() != null ? response.body() : "Account deleted successfully"));
                } else {
                    ApiError apiError = errorHandler.handleError(response);
                    if (apiError.isShouldLogout()) {
                        authState.setValue(new AuthState.LoggedOut());
                    } else {
                        authState.setValue(new AuthState.Error(apiError.getMessage()));
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable throwable) {
                ApiError apiError = errorHandler.handleException(throwable);
                authState.setValue(new AuthState.Error(apiError.getMessage()));
            }
        });
    }

    public void checkAuth(SessionManager sessionManager){
        if(sessionManager.isLoggedIn()){
            authState.postValue(new AuthState.LoggedIn());
        }
        else{
            authState.postValue(new AuthState.LoggedOut());
        }
    }

    public void resetState(){
        authState.setValue(new AuthState.Idle());
    }

}
