package com.kavya.stealthpad.utils;

import org.springframework.stereotype.Component;

import com.kavya.stealthpad.Dto.NoteResponseDTO;
import com.kavya.stealthpad.Dto.NoteSyncDTO;
import com.kavya.stealthpad.Entity.Note;

@Component
public class NotesMapper {
    public static NoteResponseDTO tNoteResponseDTO(Note note){
        return NoteResponseDTO.builder()
            .title(note.getTitle())
            .content(note.getContent())
            .category(note.getCategory())
            .updatedAt(note.getUpdatedAt())
            .isVault(note.isVault())
            .build();
    }

    public NoteSyncDTO convertToSyncDto(Note note){
        return NoteSyncDTO.builder()
                .title(note.getTitle())
                .content(note.getContent())
                .category(note.getCategory())
                .timestamp(note.getTimestamp())
                .updatedAt(note.getUpdatedAt())
                .version(note.getVersion())
                .deleted(note.isDeleted())
                .isVault(note.isVault())
                .build();
    }
    
}
