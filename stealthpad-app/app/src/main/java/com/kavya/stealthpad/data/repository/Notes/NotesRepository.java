package com.kavya.stealthpad.data.repository.Notes;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.kavya.stealthpad.data.Local.Dao.NotesDao;
import com.kavya.stealthpad.data.Local.model.NotesModel;
import com.kavya.stealthpad.utils.EncryptionManager;
import com.kavya.stealthpad.utils.SyncStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

public class NotesRepository {

    private final EncryptionManager encryptionManager;


    public interface SavenotesCallback {
        void onSuccess();

        void onError(Exception e);
    }

    private final NotesDao dao;
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Inject
    public NotesRepository(NotesDao notesDao, EncryptionManager encryptionManager) {
        this.dao = notesDao;
        this.encryptionManager = encryptionManager;
    }

    // storing the notes in the room db...
    public void saveNote(NotesModel model, SavenotesCallback callback) {
        executor.execute(() -> {
            try {
                model.setSyncStatus(SyncStatus.PENDING_CREATE);
                NotesModel encrypted = encrypt(model);
                dao.insert(encrypted);
                callback.onSuccess();
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    // ------------------------------------------- methods for fetching the notes -------------------------------------------
    public LiveData<List<NotesModel>> getAllNotes(String email) {
        // here instead of using livedata, we'll use MediatorLiveData so that we can decrypt the note before exposing the LiveData, as it's a read only class used for registering the updates....
        MediatorLiveData<List<NotesModel>> res = new MediatorLiveData<>();

        // this LiveData will hold the encrypted data...
        LiveData<List<NotesModel>> source = dao.getAllNotes(email);

        // now decrypting the notes...
        res.addSource(source, notes -> {
            // null value check...
            if (notes == null) {
                res.postValue(new ArrayList<>());
                return;
            }
            executor.execute(() -> {
                res.postValue(decryptNotes(notes));
            });
        });

        return res;
    }

    public LiveData<List<NotesModel>> getNotesByCategory(String email, String category) {
        MediatorLiveData<List<NotesModel>> res = new MediatorLiveData<>();
        LiveData<List<NotesModel>> source = dao.getNotesByCategory(email, category);
        res.addSource(source, notes -> {
            if (notes == null) {
                res.postValue(new ArrayList<>());
                return;
            }
            executor.execute(() -> {
                res.postValue(decryptNotes(notes));
            });
        });
        return res;
    }

    public LiveData<List<NotesModel>> getRecentNotes(String email) {
        MediatorLiveData<List<NotesModel>> res = new MediatorLiveData<>();

        LiveData<List<NotesModel>> source = dao.getRecentNotes(email);

        res.addSource(source, notes -> {
            if (notes == null) {
                res.postValue(new ArrayList<>());
                return;
            }
            executor.execute(() -> {
                res.postValue(decryptNotes(notes));
            });
        });
        return res;
    }

    public LiveData<List<NotesModel>> getVaultNotes(String email) {
        MediatorLiveData<List<NotesModel>> res = new MediatorLiveData<>();
        LiveData<List<NotesModel>> source = dao.getVaultNotes(email);
        res.addSource(source, notes -> {
            if (notes == null) {
                res.postValue(new ArrayList<>());
                return;
            }
            executor.execute(() -> {
                res.postValue(decryptNotes(notes));
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

    public void deleteById(int id, SavenotesCallback savenotesCallback) {
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
                    dao.deleteNoteByid(note.getId());
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
