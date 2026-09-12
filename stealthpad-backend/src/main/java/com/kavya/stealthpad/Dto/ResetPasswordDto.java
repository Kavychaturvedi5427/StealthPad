package com.kavya.stealthpad.Dto;

import lombok.Data;

@Data 
public class ResetPasswordDto {
    private String email;
    private String otp;
    private String newPassword;
}