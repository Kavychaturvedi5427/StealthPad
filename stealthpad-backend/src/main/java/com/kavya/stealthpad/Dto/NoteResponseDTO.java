package com.kavya.stealthpad.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteResponseDTO {

    private Long id; // server id helps us to find the notes at the backend side instead of using the
                     // room id....
    private String title;
    private String content;
    private String category;
    private Long updatedAt;
    private boolean isVault;

}
