package com.kavya.stealthpad.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kavya.stealthpad.security.CurrentUserService;
import com.kavya.stealthpad.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final AuthService authService;

    @DeleteMapping("/account")
    public ResponseEntity<String> deleteAccount() {

        String email = CurrentUserService.getCurrentUser().getEmail();

        return ResponseEntity.ok(
                authService.deleteAccount(email)
        );
    }
}