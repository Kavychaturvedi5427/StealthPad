package com.kavya.stealthpad.service;

import java.util.Date;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.kavya.stealthpad.Dto.AuthResponseDto;
import com.kavya.stealthpad.Dto.ForgotPassDto;
import com.kavya.stealthpad.Dto.LoginRequestDto;
import com.kavya.stealthpad.Dto.RegisterDto;
import com.kavya.stealthpad.Dto.ResetPasswordDto;
import com.kavya.stealthpad.Entity.User;
import com.kavya.stealthpad.repository.NotesRepository;
import com.kavya.stealthpad.repository.UserRepository;
import com.kavya.stealthpad.security.AuthUtils;

import jakarta.transaction.Transactional;

import com.kavya.stealthpad.exception.BadRequestException;
import com.kavya.stealthpad.exception.ConflictException;
import com.kavya.stealthpad.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthUtils authUtils;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final NotesRepository notesRepository;

    public AuthResponseDto registerUser(RegisterDto registerDto) {
        if (userRepository.existsByEmail(registerDto.getEmail())) {
            throw new ConflictException("An account with that email already exists");
        }

        // creating new user and saving it to the database
        User user = new User();
        user.setName(registerDto.getName());
        user.setEmail(registerDto.getEmail());
        String encodedPassword = passwordEncoder.encode(registerDto.getPassword());
        user.setPassword(encodedPassword);

        userRepository.save(user);

        // generating jwt token for the user
        String jwtToken = authUtils.generateToken(user);

        return new AuthResponseDto(jwtToken, user.getName(), user.getEmail(), "Registration successful");
    }

    public AuthResponseDto login(LoginRequestDto loginRequestDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDto.getEmail(), loginRequestDto.getPass()));

        User user = (User) authentication.getPrincipal();
        String token = authUtils.generateToken(user);
        return new AuthResponseDto(token, user.getName(), user.getEmail(), "Login successful");
    }

    public String forgotPassword(ForgotPassDto dto) {

        User user = userRepository.findByEmail(dto.getEmail());

        if (user == null) {
            return "If an account exists with this email, an OTP has been sent.";
        }

        String otp = String.format(
                "%06d",
                new java.security.SecureRandom().nextInt(1_000_000));

        Date expiry = new Date(
                System.currentTimeMillis() + 10 * 60 * 1000);

        user.setResetOtp(otp);
        user.setResetOtpExpiry(expiry);

        userRepository.save(user);

        emailService.sendPasswordResetOtp(
                user.getEmail(),
                otp);

        return "If an account exists with this email, an OTP has been sent.";
    }

    public String resetPassword(ResetPasswordDto dto) {

        User user = userRepository.findByEmail(dto.getEmail());

        if (user == null) {
            throw new BadRequestException("Invalid OTP");
        }

        if (user.getResetOtp() == null ||
                !user.getResetOtp().equals(dto.getOtp())) {
            throw new BadRequestException("Invalid OTP");
        }

        if (user.getResetOtpExpiry() == null ||
                user.getResetOtpExpiry().before(new Date())) {
            throw new BadRequestException("OTP expired");
        }

        user.setPassword(
                passwordEncoder.encode(dto.getNewPassword()));

        // Invalidate OTP after successful reset
        user.setResetOtp(null);
        user.setResetOtpExpiry(null);

        userRepository.save(user);

        return "Password reset successful";
    }
    
    @Transactional
    public String deleteAccount(String email) {

    User user = userRepository.findByEmail(email);

    if (user == null) {
        throw new ResourceNotFoundException("User not found");
    }

    // Delete only this user's notes
    notesRepository.deleteByUser(user);

    // Then delete the user
    userRepository.delete(user);

    return "Account deleted successfully";
}
}
