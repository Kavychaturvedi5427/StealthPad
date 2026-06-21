package com.kavya.stealthpad.data.api;

import com.kavya.stealthpad.data.DataModel.AuthResponseDto;
import com.kavya.stealthpad.data.DataModel.LoginRequestDTO;
import com.kavya.stealthpad.data.DataModel.RegisterRequestDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {

    @POST("/api/auth/login")
    Call<AuthResponseDto> login(@Body LoginRequestDTO loginRequestDTO);

    @POST("/api/auth/register")
    Call<AuthResponseDto> register(@Body RegisterRequestDTO registerRequestDTO);

}
