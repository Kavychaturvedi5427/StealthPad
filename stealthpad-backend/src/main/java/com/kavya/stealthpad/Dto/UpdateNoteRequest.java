package com.kavya.stealthpad.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateNoteRequest {
    
    private String title;
    private String content;
    private String category;
}
