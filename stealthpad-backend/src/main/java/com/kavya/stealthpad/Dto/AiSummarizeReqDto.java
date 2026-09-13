package com.kavya.stealthpad.Dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data 
public class AiSummarizeReqDto {
    @NotBlank(message = "Text cannot be empty")
    @Size(max = 10000, message = "Text cannot exceed 10000 characters")
    private String text;
}
