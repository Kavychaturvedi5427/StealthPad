package com.kavya.stealthpad.controller;

import org.springframework.web.bind.annotation.RestController;

import com.kavya.stealthpad.Dto.AuthResponseDto;
import com.kavya.stealthpad.Dto.ForgotPassDto;
import com.kavya.stealthpad.Dto.LoginRequestDto;
import com.kavya.stealthpad.Dto.RegisterDto;
import com.kavya.stealthpad.Dto.ResetPasswordDto;
import com.kavya.stealthpad.service.AuthService;

import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class StealthAuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterDto registerDto) {
        return ResponseEntity.ok(authService.registerUser(registerDto));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        return ResponseEntity.ok(authService.login(loginRequestDto));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(
            @Valid @RequestBody ForgotPassDto dto) {

        return ResponseEntity.ok(
                authService.forgotPassword(dto));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @Valid @RequestBody ResetPasswordDto dto) {

        return ResponseEntity.ok(
                authService.resetPassword(dto));
    }

}
