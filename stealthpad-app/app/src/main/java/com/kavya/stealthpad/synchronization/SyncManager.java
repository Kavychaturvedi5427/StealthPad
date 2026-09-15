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
import java.util.Objects;
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
            Log.d(TAG, "pullRemoteNotes: Received " + (remoteNotes != null ? remoteNotes.size() : 0) + " notes from server.");

            if (remoteNotes == null) return;

            Set<Long> remoteIds = new HashSet<>();
            for (NoteResponseDTO remoteNote : remoteNotes) {
                if (remoteNote != null && remoteNote.getId() != null) {
                    remoteIds.add(remoteNote.getId());
                }
            }

            List<NotesModel> localSyncedNotes = notesDao.getAllNotesWithServerIdSync(userEmail);
            Log.d(TAG, "pullRemoteNotes: Found " + localSyncedNotes.size() + " local synced notes.");

            for (NotesModel localNote : localSyncedNotes) {
                if (!remoteIds.contains(localNote.getServerId()) &&
                        localNote.getSyncStatus() != SyncStatus.PENDING_DELETE &&
                        localNote.getSyncStatus() != SyncStatus.PENDING_UPDATE) {
                    
                    Log.d(TAG, "pullRemoteNotes: Deleting local note not found on server: ID=" + localNote.getId() + " ServerID=" + localNote.getServerId());
                    cleanupLocalNote(localNote.getId());
                }
            }

            for (NoteResponseDTO remoteNote : remoteNotes) {
                if (remoteNote == null || remoteNote.getId() == null) {
                    continue;
                }

                NotesModel localNote = notesDao.getNoteByServerId(remoteNote.getId(), userEmail);

                if (localNote == null) {
                    Log.d(TAG, "pullRemoteNotes: Creating new local note for ServerID=" + remoteNote.getId());
                    NotesModel newNote = convertToLocalNote(remoteNote, userEmail);
                    notesDao.insert(newNote);
                } else {
                    if (localNote.getSyncStatus() == SyncStatus.PENDING ||
                        localNote.getSyncStatus() == SyncStatus.PENDING_CREATE ||
                        localNote.getSyncStatus() == SyncStatus.PENDING_UPDATE ||
                        localNote.getSyncStatus() == SyncStatus.PENDING_DELETE) {
                        Log.d(TAG, "pullRemoteNotes: Skipping update for local note with pending changes: ID=" + localNote.getId());
                        continue;
                    }

                    boolean changed = updateLocalNote(localNote, remoteNote);
                    if (changed) {
                        Log.d(TAG, "pullRemoteNotes: Updating local note with server changes: ID=" + localNote.getId());
                        notesDao.update(localNote);
                    }
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
        request.setLastUpdated(note.getLastUpdated());

        request.setVault(note.isVault());
        return request;
    }

    private NotesModel convertToLocalNote(NoteResponseDTO remoteNote, String userEmail) {
        NotesModel localNote = new NotesModel();
        // Server already sends encrypted data
        localNote.setTitle(remoteNote.getTitle() != null ? remoteNote.getTitle() : "Untitled Note");
        localNote.setContent(remoteNote.getContent() != null ? remoteNote.getContent() : "");
        localNote.setCategory(remoteNote.getCategory() != null ? remoteNote.getCategory() : "General");
        
        long ts = (remoteNote.getTimestamp() != null && remoteNote.getTimestamp() != 0) 
                ? normalizeTimestamp(remoteNote.getTimestamp()) 
                : System.currentTimeMillis();
        localNote.setTimestamp(ts);
        
        long updatedTs = (remoteNote.getLastUpdated() != null && remoteNote.getLastUpdated() != 0) 
                ? normalizeTimestamp(remoteNote.getLastUpdated()) 
                : ts;
        localNote.setLastUpdated(updatedTs);
        
        // Only set vault status if the server explicitly provides it
        if (remoteNote.getVault() != null) {
            localNote.setVault(remoteNote.isVault());
        } else {
            localNote.setVault(false);
        }
        
        localNote.setUserEmail(userEmail);
        localNote.setServerId(remoteNote.getId());
        localNote.setSyncStatus(SyncStatus.SUCCESS);
        return localNote;
    }

    private boolean updateLocalNote(NotesModel localNote, NoteResponseDTO remoteNote) {
        boolean changed = false;

        try {
            if (remoteNote.getTitle() != null && !Objects.equals(remoteNote.getTitle(), localNote.getTitle())) {
                localNote.setTitle(remoteNote.getTitle());
                changed = true;
            }
            if (remoteNote.getContent() != null && !Objects.equals(remoteNote.getContent(), localNote.getContent())) {
                localNote.setContent(remoteNote.getContent());
                changed = true;
            }
            if (remoteNote.getCategory() != null && !Objects.equals(remoteNote.getCategory(), localNote.getCategory())) {
                localNote.setCategory(remoteNote.getCategory());
                changed = true;
            }
            
            if (remoteNote.getTimestamp() != null && remoteNote.getTimestamp() != 0) {
                long remoteTs = normalizeTimestamp(remoteNote.getTimestamp());
                // Only update if difference is more than 1 second to account for precision loss
                if (Math.abs(localNote.getTimestamp() - remoteTs) > 1000) {
                    localNote.setTimestamp(remoteTs);
                    changed = true;
                }
            }
            
            if (remoteNote.getLastUpdated() != null && remoteNote.getLastUpdated() != 0) {
                long remoteUpdated = normalizeTimestamp(remoteNote.getLastUpdated());
                if (Math.abs(localNote.getLastUpdated() - remoteUpdated) > 1000) {
                    localNote.setLastUpdated(remoteUpdated);
                    changed = true;
                }
            }
            
            if (remoteNote.getVault() != null && remoteNote.isVault() != localNote.isVault()) {
                localNote.setVault(remoteNote.isVault());
                changed = true;
            }

            if (changed) {
                localNote.setSyncStatus(SyncStatus.SUCCESS);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during updateLocalNote: " + e.getMessage());
            return false;
        }
        return changed;
    }

    private long normalizeTimestamp(long timestamp) {
        // If it's in seconds (e.g. < 10^12), convert to milliseconds
        return (timestamp < 1000000000000L) ? timestamp * 1000 : timestamp;
    }
}
