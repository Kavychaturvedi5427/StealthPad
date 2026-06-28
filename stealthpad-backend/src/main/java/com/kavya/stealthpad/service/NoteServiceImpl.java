package com.kavya.stealthpad.service;
import com.kavya.stealthpad.config.AppConfig;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.kavya.stealthpad.Dto.NoteRequestDTO;
import com.kavya.stealthpad.Dto.NoteResponseDTO;
import com.kavya.stealthpad.Entity.Note;
import com.kavya.stealthpad.Entity.User;
import com.kavya.stealthpad.repository.NotesRepository;
import com.kavya.stealthpad.security.CurrentUserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NotesService {
    
    private final NotesRepository notesRepository;
    private final ModelMapper modelMapper;

    @Override
    public NoteResponseDTO createNote(NoteRequestDTO noteRequestDTO) {
    User user = CurrentUserService.getCurrentUser();

        Note note = new Note();
        // setting the content of the note...
        note.setTitle(noteRequestDTO.getTitle());
        note.setContent(noteRequestDTO.getContent());
        note.setCategory(noteRequestDTO.getCategory());
        note.setTimestamp(noteRequestDTO.getTimestamp());

        // setting the user who is storing the note...
        note.setUser(user);

        Note savedNote = notesRepository.save(note);

       NoteResponseDTO noteResponseDTO = modelMapper.map(savedNote, NoteResponseDTO.class);
       return noteResponseDTO;
    }

    @Override
    public List<NoteResponseDTO> getAllNotes() {
        // fetching the current user that is present in the security context holder...
        User user = CurrentUserService.getCurrentUser();

        // now fetching all the notes for the current user...
        List<Note> notes = notesRepository.findByUser(user);
        
        // mapping each notes into NoteReponseDTO class and returning them as List....
        return notes.stream().map(note -> modelMapper.map(note, NoteResponseDTO.class)).toList();

    }

    @Override
    public NoteResponseDTO updateNote(Long id, NoteRequestDTO noteRequestDTO) {     // update notes based on the server id....

        User user = CurrentUserService.getCurrentUser();

        // fetching the notes based on the id and user...
        Note note = notesRepository.findByIdAndUser(id, user).orElseThrow(()-> new RuntimeException("Note not found"));

        note.setTitle(noteRequestDTO.getTitle());
        note.setContent(noteRequestDTO.getContent());
        note.setCategory(noteRequestDTO.getCategory());
        note.setTimestamp(noteRequestDTO.getTimestamp());
        
        Note updatedNote = notesRepository.save(note);

        return modelMapper.map(updatedNote, NoteResponseDTO.class);

    }

    @Override
    public void delete(Long id) {

        User user = CurrentUserService.getCurrentUser();

        Note note = notesRepository.findByIdAndUser(id, user).orElseThrow(()-> new RuntimeException("Note not found"));

        notesRepository.delete(note);
    }

    @Override
    public void deleteAllByUser() {
        User user = CurrentUserService.getCurrentUser();
        notesRepository.deleteByUser(user);
    }


}
