package com.kavya.stealthpad.synchronization;

import android.util.Log;

import com.kavya.stealthpad.data.DataModel.ApiError;
import com.kavya.stealthpad.data.DataModel.NoteRequestDTO;
import com.kavya.stealthpad.data.DataModel.NoteResponseDTO;
import com.kavya.stealthpad.data.Local.Dao.NoteAttachmentDao;
import com.kavya.stealthpad.data.Local.Dao.NotesDao;
import com.kavya.stealthpad.data.Local.model.NoteAttachment;
import com.kavya.stealthpad.data.Local.model.NotesModel;
import com.kavya.stealthpad.data.api.NotesApi;
import com.kavya.stealthpad.utils.ApiErrorHandler;
import com.kavya.stealthpad.utils.AttachmentStorageManager;
import com.kavya.stealthpad.utils.EncryptionManager;
import com.kavya.stealthpad.utils.SyncStatus;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Response;

@Singleton
public class SyncManager {

    private static final String TAG = "SyncManager";

    private final NotesDao notesDao;
    private final NoteAttachmentDao attachmentDao;
    private final NotesApi notesApi;
    private final EncryptionManager encryptionManager;
    private final AttachmentStorageManager storageManager;
    private final ApiErrorHandler errorHandler;

    @Inject
    public SyncManager(
            NotesDao notesDao,
            NoteAttachmentDao attachmentDao,
            NotesApi notesApi,
            EncryptionManager encryptionManager,
            AttachmentStorageManager storageManager,
            ApiErrorHandler errorHandler
    ) {
        this.notesDao = notesDao;
        this.attachmentDao = attachmentDao;
        this.notesApi = notesApi;
        this.encryptionManager = encryptionManager;
        this.storageManager = storageManager;
        this.errorHandler = errorHandler;
    }

    public synchronized void sync(String userEmail) {
        pushLocalChanges(userEmail);
        pullRemoteNotes(userEmail);
    }

    public synchronized boolean pushLocalChanges(String userEmail) {
        List<NotesModel> pendingNotes = notesDao.getNotesPendingSync(userEmail);

        if (pendingNotes == null || pendingNotes.isEmpty()) {
            return true;
        }

        boolean allSuccessful = true;
        for (NotesModel note : pendingNotes) {
            try {
                boolean success = false;
                switch (note.getSyncStatus()) {
                    case SyncStatus.PENDING:
                    case SyncStatus.PENDING_CREATE:
                        success = createNote(note);
                        break;
                    case SyncStatus.PENDING_UPDATE:
                        success = updateNote(note);
                        break;
                    case SyncStatus.PENDING_DELETE:
                        success = deleteNote(note);
                        break;
                    case SyncStatus.FAILED:
                        if (note.getServerId() == null) {
                            success = createNote(note);
                        } else {
                            success = updateNote(note);
                        }
                        break;
                }
                if (!success) allSuccessful = false;
            } catch (Exception e) {
                Log.e(TAG, "Error syncing note: " + note.getId(), e);
                allSuccessful = false;
            }
        }
        return allSuccessful;
    }

    private boolean createNote(NotesModel localNote) {
        try {
            NoteRequestDTO request = createRequest(localNote);
            Response<NoteResponseDTO> response = notesApi.createNote(request).execute();

            if (!response.isSuccessful()) {
                ApiError apiError = errorHandler.handleError(response);
                Log.e(TAG, "Create failed. HTTP: " + response.code() + " Message: " + apiError.getMessage());
                return false;
            }

            if (response.body() == null) {
                Log.e(TAG, "Create failed. Response body is null");
                return false;
            }

            NoteResponseDTO serverNote = response.body();
            Long serverId = serverNote.getId();

            if (serverId == null) {
                Log.e(TAG, "Server did not return server ID");
                return false;
            }

            notesDao.updateServerIdAndSyncStatus(
                    localNote.getId(),
                    serverId,
                    SyncStatus.SUCCESS
            );
            return true;
        } catch (Exception e) {
            ApiError apiError = errorHandler.handleException(e);
            Log.e(TAG, "Exception during createNote: " + apiError.getMessage());
            return false;
        }
    }

    private boolean updateNote(NotesModel localNote) {
        try {
            Long serverId = localNote.getServerId();

            if (serverId == null) {
                return createNote(localNote);
            }

            NoteRequestDTO request = createRequest(localNote);
            Response<NoteResponseDTO> response = notesApi.updateNote(serverId, request).execute();

            if (response.isSuccessful()) {
                notesDao.updateSyncStatus(
                        localNote.getId(),
                        SyncStatus.SUCCESS
                );
                return true;
            } else if (response.code() == 404) {
                Log.w(TAG, "updateNote: Note not found on server (404). Re-creating as new note. serverId=" + serverId);
                return createNote(localNote);
            } else {
                ApiError apiError = errorHandler.handleError(response);
                Log.e(TAG, "Update failed. HTTP: " + response.code() + " Message: " + apiError.getMessage());
                return false;
            }
        } catch (Exception e) {
            ApiError apiError = errorHandler.handleException(e);
            Log.e(TAG, "Exception during updateNote: " + apiError.getMessage());
            return false;
        }
    }

    private boolean deleteNote(NotesModel localNote) {
        try {
            Long serverId = localNote.getServerId();

            if (serverId == null) {
                Log.d(TAG, "deleteNote: Local-only note, deleting from Room. ID=" + localNote.getId());
                cleanupLocalNote(localNote.getId());
                return true;
            }

            Log.d(TAG, "deleteNote: Calling backend delete for serverId=" + serverId);
            Response<Void> response = notesApi.deleteById(serverId).execute();

            if (response.isSuccessful() || response.code() == 404) {
                if (response.code() == 404) {
                    Log.w(TAG, "deleteNote: Note not found on server (404). Proceeding with local deletion. serverId=" + serverId);
                } else {
                    Log.d(TAG, "deleteNote: Backend delete successful for serverId=" + serverId);
                }
                cleanupLocalNote(localNote.getId());
                return true;
            } else {
                ApiError apiError = errorHandler.handleError(response);
                Log.e(TAG, "deleteNote: Failed. HTTP: " + response.code() + " Message: " + apiError.getMessage());
                return false;
            }
        } catch (Exception e) {
            ApiError apiError = errorHandler.handleException(e);
            Log.e(TAG, "deleteNote: Exception: " + apiError.getMessage());
            return false;
        }
    }

    private void cleanupLocalNote(int localId) {
        List<NoteAttachment> attachments = attachmentDao.getAttachmentsForNoteSync(localId);
        notesDao.deleteNoteAfterSync(localId);
        for (NoteAttachment attachment : attachments) {
            storageManager.deleteAttachment(attachment.getLocalPath());
        }
        Log.d(TAG, "cleanupLocalNote: Successfully removed note and attachments from local storage. ID=" + localId);
    }

    private void pullRemoteNotes(String userEmail) {
        try {
            Response<List<NoteResponseDTO>> response = notesApi.getAllNotes().execute();

            if (!response.isSuccessful()) {
                ApiError apiError = errorHandler.handleError(response);
                Log.e(TAG, "Failed to fetch server notes. HTTP: " + response.code() + " Message: " + apiError.getMessage());
                return;
            }

            if (response.body() == null) {
                Log.e(TAG, "Failed to fetch server notes. Response body is null.");
                return;
            }

            List<NoteResponseDTO> remoteNotes = response.body();

            Set<Long> remoteIds = new HashSet<>();
            for (NoteResponseDTO remoteNote : remoteNotes) {
                if (remoteNote.getId() != null) {
                    remoteIds.add(remoteNote.getId());
                }
            }

            List<NotesModel> localSyncedNotes = notesDao.getAllNotesWithServerIdSync(userEmail);
            for (NotesModel localNote : localSyncedNotes) {
                if (!remoteIds.contains(localNote.getServerId()) &&
                        localNote.getSyncStatus() != SyncStatus.PENDING_DELETE &&
                        localNote.getSyncStatus() != SyncStatus.PENDING_UPDATE) {
                    
                    List<NoteAttachment> attachments = attachmentDao.getAttachmentsForNoteSync(localNote.getId());
                    notesDao.deleteNoteAfterSync(localNote.getId());
                    for (NoteAttachment attachment : attachments) {
                        storageManager.deleteAttachment(attachment.getLocalPath());
                    }
                }
            }

            for (NoteResponseDTO remoteNote : remoteNotes) {
                if (remoteNote.getId() == null) {
                    continue;
                }

                NotesModel localNote = notesDao.getNoteByServerId(remoteNote.getId(), userEmail);

                if (localNote == null) {
                    NotesModel newNote = convertToLocalNote(remoteNote, userEmail);
                    notesDao.insert(newNote);
                } else {
                    if (localNote.getSyncStatus() == SyncStatus.PENDING ||
                        localNote.getSyncStatus() == SyncStatus.PENDING_CREATE ||
                        localNote.getSyncStatus() == SyncStatus.PENDING_UPDATE ||
                        localNote.getSyncStatus() == SyncStatus.PENDING_DELETE) {
                        continue;
                    }

                    updateLocalNote(localNote, remoteNote);
                    notesDao.update(localNote);
                }
            }
        } catch (Exception e) {
            ApiError apiError = errorHandler.handleException(e);
            Log.e(TAG, "Exception during pullRemoteNotes: " + apiError.getMessage());
        }
    }

    private NoteRequestDTO createRequest(NotesModel note) {
        NoteRequestDTO request = new NoteRequestDTO();
        // Send encrypted data directly to the server
        request.setTitle(note.getTitle());
        request.setContent(note.getContent());
        request.setCategory(note.getCategory());
        request.setTimestamp(note.getTimestamp());
        request.setVault(note.isVault());
        return request;
    }

    private NotesModel convertToLocalNote(NoteResponseDTO remoteNote, String userEmail) {
        NotesModel localNote = new NotesModel();
        // Server already sends encrypted data
        localNote.setTitle(remoteNote.getTitle());
        localNote.setContent(remoteNote.getContent());
        localNote.setCategory(remoteNote.getCategory());
        localNote.setTimestamp(remoteNote.getTimestamp());
        localNote.setVault(remoteNote.isVault());
        localNote.setUserEmail(userEmail);
        localNote.setServerId(remoteNote.getId());
        localNote.setSyncStatus(SyncStatus.SUCCESS);
        return localNote;
    }

    private void updateLocalNote(NotesModel localNote, NoteResponseDTO remoteNote) {
        // Server already sends encrypted data
        localNote.setTitle(remoteNote.getTitle());
        localNote.setContent(remoteNote.getContent());
        localNote.setCategory(remoteNote.getCategory());
        localNote.setTimestamp(remoteNote.getTimestamp());
        localNote.setVault(remoteNote.isVault());
        localNote.setSyncStatus(SyncStatus.SUCCESS);
    }
}
