package com.kavya.stealthpad.Dto;

import lombok.Data;

@Data
public class NoteRequestDTO {

    private String title;

    private String content;

    private String category;

    private long timestamp;
    
    private boolean isVault;
}