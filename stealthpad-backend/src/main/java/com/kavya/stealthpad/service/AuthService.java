package com.kavya.stealthpad.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.kavya.stealthpad.Dto.AuthResponseDto;
import com.kavya.stealthpad.Dto.LoginRequestDto;
import com.kavya.stealthpad.Dto.RegisterDto;
import com.kavya.stealthpad.Entity.User;
import com.kavya.stealthpad.repository.UserRepository;
import com.kavya.stealthpad.security.AuthUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthUtils authUtils;
    private final AuthenticationManager authenticationManager;

    public AuthResponseDto registerUser(RegisterDto registerDto) {
        if (userRepository.existsByEmail(registerDto.getEmail())) {
            throw new RuntimeException("User already exist");
        }

        // creating new user and saving it to the database
        User user = new User();
        user.setName(registerDto.getName());
        user.setEmail(registerDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerDto.getPass()));

        userRepository.save(user);

        // generating jwt token for the user
        String jwtToken = authUtils.generateToken(user);

        return new AuthResponseDto(jwtToken, user.getName(), user.getEmail(), "Registration successful");
    }

    public AuthResponseDto login(LoginRequestDto loginRequestDto){
        try{
            Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequestDto.getEmail(), loginRequestDto.getPass()));

            // if auth successful then fetch the user...
             User user = (User) authentication.getPrincipal();

            // generate the token....
            String token = authUtils.generateToken(user);
            return new AuthResponseDto(token, user.getName(), user.getEmail(), "Login successful");
        }catch(Exception e){
            e.printStackTrace();
            throw e;
        }

       
    }

}
