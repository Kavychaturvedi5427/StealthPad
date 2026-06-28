package com.kavya.stealthpad.service;

import org.springframework.stereotype.Service;

import com.kavya.stealthpad.Dto.NoteRequestDTO;
import com.kavya.stealthpad.Dto.NoteResponseDTO;
import java.util.*;

/**
 * NotesService
 */
@Service
public interface NotesService {

    NoteResponseDTO createNote(NoteRequestDTO noteRequestDTO);

    List<NoteResponseDTO> getAllNotes();

    NoteResponseDTO updateNote(Long id, NoteRequestDTO noteRequestDTO);

    void deleteAllByUser();

    void delete(Long id);

}