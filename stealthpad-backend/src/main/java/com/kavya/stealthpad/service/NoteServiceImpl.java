package com.kavya.stealthpad.service;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.kavya.stealthpad.Dto.NoteRequestDTO;
import com.kavya.stealthpad.Dto.NoteResponseDTO;
import com.kavya.stealthpad.Dto.NoteSyncDTO;
import com.kavya.stealthpad.Dto.NoteSyncRequestDTO;
import com.kavya.stealthpad.Dto.NoteSyncResponseDTO;
import com.kavya.stealthpad.Entity.Note;
import com.kavya.stealthpad.Entity.User;
import com.kavya.stealthpad.repository.NotesRepository;
import com.kavya.stealthpad.security.CurrentUserService;
import com.kavya.stealthpad.utils.NotesMapper;
import com.kavya.stealthpad.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NotesService {

    private final NotesRepository notesRepository;
    private final ModelMapper modelMapper;
    private final NotesMapper notesMapper;


    @Override
    public NoteResponseDTO createNote(NoteRequestDTO noteRequestDTO) {
        User user = CurrentUserService.getCurrentUser();

        Note note = new Note();
        // setting the content of the note...
        note.setTitle(noteRequestDTO.getTitle());
        note.setContent(noteRequestDTO.getContent());
        note.setCategory(noteRequestDTO.getCategory());
        note.setTimestamp(noteRequestDTO.getTimestamp());
        note.setVault(noteRequestDTO.isVault());

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
        List<Note> notes = notesRepository.findByUserAndDeletedFalse(user);

        // mapping each notes into NoteReponseDTO class and returning them as List....
        return notes.stream().map(note -> modelMapper.map(note, NoteResponseDTO.class)).toList();

    }

    @Override
    public NoteResponseDTO updateNote(Long id, NoteRequestDTO noteRequestDTO) { // update notes based on the server
                                                                                // id....

        User user = CurrentUserService.getCurrentUser();

        // fetching the notes based on the id and user...
        Note note = notesRepository.findByIdAndUser(id, user)
            .orElseThrow(() -> new ResourceNotFoundException("Note not found"));

        note.setTitle(noteRequestDTO.getTitle());
        note.setContent(noteRequestDTO.getContent());
        note.setCategory(noteRequestDTO.getCategory());
        note.setTimestamp(noteRequestDTO.getTimestamp());
        note.setVault(noteRequestDTO.isVault());

        Note updatedNote = notesRepository.save(note);

        return modelMapper.map(updatedNote, NoteResponseDTO.class);

    }

    @Override
    public void delete(Long id) {

        User user = CurrentUserService.getCurrentUser();

        Note note = notesRepository.findByIdAndUser(id, user)
            .orElseThrow(() -> new ResourceNotFoundException("Note not found"));

        notesRepository.delete(note);
    }

    @Override
    public void deleteAllByUser() {
        User user = CurrentUserService.getCurrentUser();
        notesRepository.deleteByUser(user);
    }

    @Override
    public NoteSyncResponseDTO syncNotes(NoteSyncRequestDTO request) {

        User user = CurrentUserService.getCurrentUser();

        List<NoteSyncDTO> serverChanges = new ArrayList<>();

        /*
         * Process changes coming from the client.
         */
        if (request.getChanges() != null) {

            for (NoteSyncDTO clientNote : request.getChanges()) {

                /*
                 * ----------------------------------------
                 * NEW NOTE
                 * ----------------------------------------
                 */
                if (clientNote.getId() == null) {

                    Note note = new Note();

                    note.setTitle(clientNote.getTitle());
                    note.setContent(clientNote.getContent());
                    note.setCategory(clientNote.getCategory());
                    note.setTimestamp(clientNote.getTimestamp());
                    note.setUser(user);
                    note.setDeleted(clientNote.isDeleted());
                    note.setVault(clientNote.isVault());
                    

                    /*
                     * @PrePersist will set:
                     * version = 1
                     * updatedAt = server time
                     */
                    Note savedNote = notesRepository.save(note);

                    serverChanges.add(notesMapper.convertToSyncDto(savedNote));

                    continue;
                }

                /*
                 * ----------------------------------------
                 * EXISTING NOTE
                 * ----------------------------------------
                 */
                Note serverNote = notesRepository
                        .findByIdAndUser(clientNote.getId(), user)
                        .orElse(null);

                /*
                 * The note doesn't exist on the server.
                 *
                 * Don't create a new note with a new ID because
                 * the client may have sent an invalid/stale ID.
                 */
                if (serverNote == null) {
                    continue;
                }

                /*
                 * ----------------------------------------
                 * VERSION CHECK
                 * ----------------------------------------
                 */

                if (clientNote.getVersion() >= serverNote.getVersion()) {

                    /*
                     * Client has the current version.
                     * Accept its changes.
                     */
                    serverNote.setTitle(clientNote.getTitle());
                    serverNote.setContent(clientNote.getContent());
                    serverNote.setCategory(clientNote.getCategory());
                    serverNote.setTimestamp(clientNote.getTimestamp());
                    serverNote.setDeleted(clientNote.isDeleted());
                    serverNote.setVault(clientNote.isVault());

                    /*
                     * @PreUpdate automatically updates:
                     *
                     * updatedAt
                     * version
                     */
                    Note savedNote = notesRepository.save(serverNote);

                    serverChanges.add(notesMapper.convertToSyncDto(savedNote));

                } else {

                    /*
                     * Client has an older version.
                     *
                     * Server wins.
                     */
                    serverChanges.add(notesMapper.convertToSyncDto(serverNote));
                }
            }
        }

        /*
         * ----------------------------------------
         * Pull server changes.
         * ----------------------------------------
         */

        List<Note> changedNotes = notesRepository.findByUserAndUpdatedAtGreaterThan(
                user,
                request.getLastSyncAt());

        /*
         * Avoid returning duplicate notes.
         */
        for (Note note : changedNotes) {

            boolean alreadyAdded = serverChanges.stream()
                    .anyMatch(change -> change.getId() != null &&
                            change.getId().equals(note.getId()));

            if (!alreadyAdded) {
                serverChanges.add(notesMapper.convertToSyncDto(note));
            }
        }

        /*
         * ----------------------------------------
         * Build response.
         * ----------------------------------------
         */

        return NoteSyncResponseDTO.builder()
                .serverTime(System.currentTimeMillis())
                .changes(serverChanges)
                .build();
    }

}
