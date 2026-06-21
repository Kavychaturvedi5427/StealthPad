package com.kavya.stealthpad.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDto {

    private String jwt;
    private String name;
    private String email;
    private String message;

}
