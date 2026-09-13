package com.kavya.stealthpad.Dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class AiGenerateResDto {

    private String generatedNote;

    private LocalDateTime generatedAt;

}
