package com.kavya.stealthpad.Dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class NoteResponseDTO {
    
    private Long id;        // server id helps us to find the notes at the backend side instead of using the room id....
    private String title;
    private String content;
    private String category;
    private Long updatedAt;

}
