package com.kavya.stealthpad.Dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data 
public class AiSummarizeResDto {
    
    String summary;
    LocalDateTime generatedAt;

}
