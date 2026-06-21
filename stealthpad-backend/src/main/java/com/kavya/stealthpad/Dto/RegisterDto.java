package com.kavya.stealthpad.Dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class RegisterDto {
    
    private String name;
    private String email;
    private String pass;
}
