package com.kavya.stealthpad.data.repository.Auth;

import com.kavya.stealthpad.data.DataModel.AuthResponseDto;
import com.kavya.stealthpad.data.DataModel.ForgotPassRequest;
import com.kavya.stealthpad.data.DataModel.LoginRequestDTO;
import com.kavya.stealthpad.data.DataModel.RegisterRequestDTO;
import com.kavya.stealthpad.data.DataModel.ResetPasswordRequest;
import com.kavya.stealthpad.data.api.AuthApi;

import javax.inject.Inject;

import retrofit2.Call;

public class AuthRepository {

    private final AuthApi authApi;
    @Inject
    public AuthRepository(AuthApi api) {
        this.authApi = api;
    }

    public Call<AuthResponseDto> login(LoginRequestDTO loginRequestDTO) {
        return authApi.login(loginRequestDTO);
    }

    public Call<AuthResponseDto> register(RegisterRequestDTO registerRequestDTO) {
        return authApi.register(registerRequestDTO);
    }

    public Call<String> forgotPassword(ForgotPassRequest request) {
        return authApi.forgotPassword(request);
    }

    public Call<String> resetPassword(ResetPasswordRequest request) {
        return authApi.resetPassword(request);
    }

    public Call<String> deleteAccount() {
        return authApi.deleteAccount();
    }

}
