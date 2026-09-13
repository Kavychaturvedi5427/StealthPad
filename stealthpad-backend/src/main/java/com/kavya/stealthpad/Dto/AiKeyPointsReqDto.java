package com.kavya.stealthpad.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AiKeyPointsReqDto {

    @NotBlank(message = "Text cannot be empty")
    @Size(max = 10000, message = "Text cannot exceed 10000 characters")
    private String text;
}