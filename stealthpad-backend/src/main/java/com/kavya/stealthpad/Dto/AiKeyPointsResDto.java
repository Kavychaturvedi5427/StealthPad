package com.kavya.stealthpad.Dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class AiKeyPointsResDto {

    private List<String> keyPoints;

    private LocalDateTime generatedAt;
}