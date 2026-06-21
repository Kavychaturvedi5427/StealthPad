package com.kavya.stealthpad.controller;

import org.springframework.web.bind.annotation.RestController;

import com.kavya.stealthpad.Dto.AuthResponseDto;
import com.kavya.stealthpad.Dto.LoginRequestDto;
import com.kavya.stealthpad.Dto.RegisterDto;
import com.kavya.stealthpad.service.AuthService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class StealthAuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@RequestBody RegisterDto registerDto) {
        return ResponseEntity.ok(authService.registerUser(registerDto));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody LoginRequestDto loginRequestDto) {
        return ResponseEntity.ok(authService.login(loginRequestDto));
    }
    
}
