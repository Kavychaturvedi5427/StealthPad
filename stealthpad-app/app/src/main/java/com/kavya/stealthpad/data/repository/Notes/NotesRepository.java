package com.kavya.stealthpad.data.repository.Notes;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.kavya.stealthpad.data.Local.Dao.NoteAttachmentDao;
import com.kavya.stealthpad.data.Local.Dao.NotesDao;
import com.kavya.stealthpad.data.Local.model.NoteAttachment;
import com.kavya.stealthpad.data.Local.model.NoteWithAttachments;
import com.kavya.stealthpad.data.Local.model.NotesModel;
import com.kavya.stealthpad.utils.AttachmentStorageManager;
import com.kavya.stealthpad.utils.EncryptionManager;
import com.kavya.stealthpad.utils.SyncStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

public class NotesRepository {

    private final EncryptionManager encryptionManager;
    private final NoteAttachmentDao attachmentDao;
    private final AttachmentStorageManager storageManager;
    private final com.kavya.stealthpad.synchronization.SyncManager syncManager;
    private final com.kavya.stealthpad.utils.SessionManager sessionManager;


    public interface SavenotesCallback {
        void onSuccess();

        void onError(Exception e);
    }

    private final NotesDao dao;
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Inject
    public NotesRepository(NotesDao notesDao, NoteAttachmentDao attachmentDao, AttachmentStorageManager storageManager, EncryptionManager encryptionManager, com.kavya.stealthpad.synchronization.SyncManager syncManager, com.kavya.stealthpad.utils.SessionManager sessionManager) {
        this.dao = notesDao;
        this.attachmentDao = attachmentDao;
        this.storageManager = storageManager;
        this.encryptionManager = encryptionManager;
        this.syncManager = syncManager;
        this.sessionManager = sessionManager;
    }

    // storing the notes in the room db...
    public void saveNote(NotesModel model, List<NoteAttachment> attachments, SavenotesCallback callback) {
        if (sessionManager.isLoggingOut()) {
            callback.onError(new Exception("Logout in progress."));
            return;
        }
        executor.execute(() -> {
            try {
                model.setSyncStatus(SyncStatus.PENDING_CREATE);
                NotesModel encrypted = encrypt(model);
                long noteId = dao.insert(encrypted);
                
                if (attachments != null) {
                    for (NoteAttachment attachment : attachments) {
                        attachment.setNoteId((int) noteId);
                        attachmentDao.insert(attachment);
                    }
                }
                
                callback.onSuccess();
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public LiveData<List<NoteAttachment>> getAttachmentsForNote(int noteId) {
        return attachmentDao.getAttachmentsForNote(noteId);
    }

    public void addAttachment(NoteAttachment attachment) {
        executor.execute(() -> attachmentDao.insert(attachment));
    }

    public void deleteAttachment(NoteAttachment attachment) {
        executor.execute(() -> {
            attachmentDao.delete(attachment);
            storageManager.deleteAttachment(attachment.getLocalPath());
        });
    }

    // ------------------------------------------- methods for fetching the notes -------------------------------------------
    public LiveData<List<NoteWithAttachments>> getAllNotes(String email, String sortOrder) {
        MediatorLiveData<List<NoteWithAttachments>> res = new MediatorLiveData<>();
        
        LiveData<List<NoteWithAttachments>> source;
        switch (sortOrder) {
            case "NEWEST_CREATED": source = dao.getAllNotesNewest(email); break;
            case "OLDEST_CREATED": source = dao.getAllNotesOldest(email); break;
            case "ALPHABETICAL": source = dao.getAllNotesAlphabetical(email); break;
            default: source = dao.getAllNotesWithAttachments(email); break;
        }

        res.addSource(source, notes -> {
            if (notes == null) {
                res.postValue(new ArrayList<>());
                return;
            }
            executor.execute(() -> {
                res.postValue(decryptNotesWithAttachments(notes));
            });
        });

        return res;
    }

    public LiveData<List<NoteWithAttachments>> getNotesByCategory(String email, String category, String sortOrder) {
        MediatorLiveData<List<NoteWithAttachments>> res = new MediatorLiveData<>();
        
        LiveData<List<NoteWithAttachments>> source;
        switch (sortOrder) {
            case "NEWEST_CREATED": source = dao.getNotesByCategoryNewest(email, category); break;
            case "OLDEST_CREATED": source = dao.getNotesByCategoryOldest(email, category); break;
            case "ALPHABETICAL": source = dao.getNotesByCategoryAlphabetical(email, category); break;
            default: source = dao.getNotesByCategoryWithAttachments(email, category); break;
        }

        res.addSource(source, notes -> {
            if (notes == null) {
                res.postValue(new ArrayList<>());
                return;
            }
            executor.execute(() -> {
                res.postValue(decryptNotesWithAttachments(notes));
            });
        });
        return res;
    }

    public LiveData<List<NoteWithAttachments>> getRecentNotes(String email) {
        MediatorLiveData<List<NoteWithAttachments>> res = new MediatorLiveData<>();
        LiveData<List<NoteWithAttachments>> source = dao.getRecentNotesWithAttachments(email);

        res.addSource(source, notes -> {
            if (notes == null) {
                res.postValue(new ArrayList<>());
                return;
            }
            executor.execute(() -> {
                res.postValue(decryptNotesWithAttachments(notes));
            });
        });
        return res;
    }

    public LiveData<List<NoteWithAttachments>> getVaultNotes(String email) {
        MediatorLiveData<List<NoteWithAttachments>> res = new MediatorLiveData<>();
        LiveData<List<NoteWithAttachments>> source = dao.getVaultNotesWithAttachments(email);
        res.addSource(source, notes -> {
            if (notes == null) {
                res.postValue(new ArrayList<>());
                return;
            }
            executor.execute(() -> {
                res.postValue(decryptNotesWithAttachments(notes));
            });
        });
        return res;
    }

    public LiveData<NotesModel> getNoteById(int id) {
        MediatorLiveData<NotesModel> res = new MediatorLiveData<>();
        res.addSource(dao.getNoteById(id), note -> {
            if (note == null) {
                res.postValue(null);
                return;
            }
            executor.execute(() -> {
                try {
                    res.postValue(decrypt(note));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        });
        return res;
    }

    // ------------------------------------------- method for updating the notes -------------------------------------------
    public void updateNote(NotesModel current, SavenotesCallback savenotesCallback) {
        if (sessionManager.isLoggingOut()) {
            savenotesCallback.onError(new Exception("Logout in progress."));
            return;
        }
        executor.execute(() -> {
            try {
                /*
                 * If the note has already been created on the server,
                 * this is an UPDATE.
                 *
                 * If serverId is null, the note hasn't reached the
                 * backend yet, so we keep it as CREATE.
                 */
                if (current.getServerId() == null) {
                    current.setSyncStatus(SyncStatus.PENDING_CREATE);
                } else {
                    current.setSyncStatus(SyncStatus.PENDING_UPDATE);
                }

                NotesModel model = encrypt(current);
                dao.update(model);
                savenotesCallback.onSuccess();
            } catch (Exception e) {
                savenotesCallback.onError(e);
            }
        });
    }


    // ------------------------------------------- method for deleting the notes -------------------------------------------
    public void deleteAllNotes(String email) {
        executor.execute(() -> {
            dao.deleteAllNotes(email);
        });
    }

    public void deleteAllNotesSync(String email) {
        dao.deleteAllNotes(email);
    }

    public boolean hasPendingSyncSync(String email) {
        List<NotesModel> pending = dao.getNotesPendingSync(email);
        return pending != null && !pending.isEmpty();
    }

    public boolean pushPendingChangesSync(String email) {
        return syncManager.pushLocalChanges(email);
    }

    public void deleteById(int id, SavenotesCallback savenotesCallback) {
        if (sessionManager.isLoggingOut()) {
            savenotesCallback.onError(new Exception("Logout in progress."));
            return;
        }
        executor.execute(() -> {
            try {
                NotesModel note = dao.getNoteByIdSync(id);
                if (note == null) {
                    savenotesCallback.onError(new Exception("Note not found."));
                    return;
                }

                /*
                 * If the note has never been synchronized,
                 * there is nothing to delete from the server.
                 *
                 * We can safely remove it locally.
                 */
                if (note.getServerId() == null) {
                    List<NoteAttachment> attachments = attachmentDao.getAttachmentsForNoteSync(id);
                    dao.deleteNoteByid(note.getId());
                    // Cleanup files
                    for (NoteAttachment attachment : attachments) {
                        storageManager.deleteAttachment(attachment.getLocalPath());
                    }
                } else {
                    /*
                     * The server already knows about this note.
                     *
                     * Keep it in Room until the backend confirms
                     * the deletion.
                     */
                    dao.updateSyncStatus(id, SyncStatus.PENDING_DELETE);
                }

                savenotesCallback.onSuccess();
            } catch (Exception e) {
                savenotesCallback.onError(e);
            }
        });
    }

    public LiveData<List<NoteWithAttachments>> searchNotes(String email, String query) {
        MediatorLiveData<List<NoteWithAttachments>> res = new MediatorLiveData<>();
        // Room LIKE query needs wildcards
        String searchQuery = "%" + query + "%";
        
        LiveData<List<NoteWithAttachments>> source = dao.searchNotes(email, searchQuery);
        res.addSource(source, notes -> {
            if (notes == null) {
                res.postValue(new ArrayList<>());
                return;
            }
            executor.execute(() -> {
                res.postValue(decryptNotesWithAttachments(notes));
            });
        });
        return res;
    }

    private NotesModel encrypt(NotesModel current) throws Exception {

        NotesModel model = new NotesModel();

        // Local Room ID
        model.setId(current.getId());

        // Encrypt sensitive fields
        model.setTitle(encryptionManager.encrypt(current.getTitle()));
        model.setContent(encryptionManager.encrypt(current.getContent()));

        // Non-sensitive fields
        model.setCategory(current.getCategory());
        model.setUserEmail(current.getUserEmail());
        model.setTimestamp(current.getTimestamp());
        model.setVault(current.isVault());

        // IMPORTANT: preserve sync information
        model.setSyncStatus(current.getSyncStatus());
        model.setServerId(current.getServerId());

        return model;
    }

    private NotesModel decrypt(NotesModel current) throws Exception {

        NotesModel model = new NotesModel();

        // Local Room ID
        model.setId(current.getId());

        // Decrypt sensitive fields
        model.setTitle(encryptionManager.decrypt(current.getTitle()));
        model.setContent(encryptionManager.decrypt(current.getContent()));

        // Non-sensitive fields
        model.setCategory(current.getCategory());
        model.setUserEmail(current.getUserEmail());
        model.setTimestamp(current.getTimestamp());
        model.setVault(current.isVault());

        // IMPORTANT: preserve sync information
        model.setSyncStatus(current.getSyncStatus());
        model.setServerId(current.getServerId());

        return model;
    }

    private List<NoteWithAttachments> decryptNotesWithAttachments(List<NoteWithAttachments> notes) {
        List<NoteWithAttachments> decrypted = new ArrayList<>();
        for (NoteWithAttachments item : notes) {
            try {
                NoteWithAttachments decryptedItem = new NoteWithAttachments();
                decryptedItem.note = decrypt(item.note);
                decryptedItem.attachments = item.attachments;
                decrypted.add(decryptedItem);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return decrypted;
    }

    private List<NotesModel> decryptNotes(List<NotesModel> notes) {
        List<NotesModel> decryptedRecentNote = new ArrayList<>();
        for (NotesModel note : notes) {
            try {
                decryptedRecentNote.add(decrypt(note));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return decryptedRecentNote;
    }

}
