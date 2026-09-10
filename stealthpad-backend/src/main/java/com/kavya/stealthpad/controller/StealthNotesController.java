package com.kavya.stealthpad.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kavya.stealthpad.Dto.NoteRequestDTO;
import com.kavya.stealthpad.Dto.NoteResponseDTO;
<<<<<<< Updated upstream
=======
import com.kavya.stealthpad.Dto.NoteSyncRequestDTO;
import com.kavya.stealthpad.Dto.NoteSyncResponseDTO;
>>>>>>> Stashed changes
import com.kavya.stealthpad.service.NotesService;

import lombok.RequiredArgsConstructor;

import java.util.List;

<<<<<<< Updated upstream
import org.apache.catalina.connector.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
=======
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
>>>>>>> Stashed changes
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;




@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notes")
public class StealthNotesController {

    private final NotesService notesService;
       
    // Create note mapping...
    @PostMapping()
    public ResponseEntity<NoteResponseDTO> createNote(@RequestBody NoteRequestDTO noteRequestDTO) {
        return ResponseEntity.ok(notesService.createNote(noteRequestDTO));
    }

    // Get all notes mapping...
    @GetMapping
    public ResponseEntity<List<NoteResponseDTO>> getAllNotes() {
        return ResponseEntity.ok(notesService.getAllNotes());
    }

<<<<<<< Updated upstream
=======
    @PostMapping("/sync")
    public ResponseEntity<NoteSyncResponseDTO> syncNotes(@RequestBody NoteSyncRequestDTO requestDTO) {
           return ResponseEntity.ok(notesService.syncNotes(requestDTO));
    }
    

>>>>>>> Stashed changes
    // update note...
    @PutMapping("/{id}")
    public ResponseEntity<NoteResponseDTO> updateNote(@PathVariable Long id, @RequestBody NoteRequestDTO noteRequestDTO) {
        return ResponseEntity.ok(notesService.updateNote(id, noteRequestDTO));
    }
    
    @DeleteMapping
    public ResponseEntity<String> deleteAll(){
        notesService.deleteAllByUser();
        return ResponseEntity.ok("All notes deleted.");
    }

    // delete note mapping...
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id){
        notesService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    
}
