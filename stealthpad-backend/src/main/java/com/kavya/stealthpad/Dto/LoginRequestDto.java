package com.kavya.stealthpad.Dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class LoginRequestDto {

    private String email;
    private String pass;
    
}
