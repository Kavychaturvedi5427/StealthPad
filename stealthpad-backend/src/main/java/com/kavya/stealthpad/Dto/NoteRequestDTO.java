package com.kavya.stealthpad.Dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class NoteRequestDTO {

    private String title;

    private String content;

    private String category;

    private long timestamp;
}