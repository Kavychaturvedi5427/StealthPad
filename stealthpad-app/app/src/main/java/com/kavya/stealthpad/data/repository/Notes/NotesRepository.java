package com.kavya.stealthpad.data.repository.Notes;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.kavya.stealthpad.EncryptionModule.AES_EncryptDecrypt;
import com.kavya.stealthpad.data.DataModel.NoteVmDao;
import com.kavya.stealthpad.data.Local.Dao.NotesDao;
import com.kavya.stealthpad.data.Local.model.NotesModel;
import com.kavya.stealthpad.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

public class NotesRepository {

    private final AES_EncryptDecrypt helper = new AES_EncryptDecrypt();


    public interface SavenotesCallback {
        void onSuccess();

        void onError(Exception e);
    }

    private final NotesDao dao;
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Inject
    public NotesRepository(NotesDao notesDao) {
        this.dao = notesDao;
    }

    // storing the notes in the room db...
    public void saveNote(NotesModel model, SavenotesCallback callback) {
        executor.execute(()->{
            try {
                NotesModel encrypted = encrypt(model);
                dao.insert(encrypted);
                callback.onSuccess();
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    // ------------------------------------------- methods for fetching the notes -------------------------------------------
    public LiveData<List<NotesModel>> getAllNotes(String email){
        // here instead of using livedata, we'll use MediatorLiveData so that we can decrypt the note before exposing the LiveData, as it's a read only class used for registering the updates....
        MediatorLiveData<List<NotesModel>> res = new MediatorLiveData<>();

        // this LiveData will hold the encrypted data...
        LiveData<List<NotesModel>> source = dao.getAllNotes(email);

        // now decrypting the notes...
        res.addSource(source, notes ->{
            // null value check...
            if (notes == null) {
                res.postValue(new ArrayList<>());
                return;
            }
            executor.execute(()->{
                res.postValue(decryptNotes(notes));
            });
        });

        return res;
    }

    public LiveData<List<NotesModel>> getRecentNotes(String email){
        MediatorLiveData<List<NotesModel>> res = new MediatorLiveData<>();

        LiveData<List<NotesModel>> source = dao.getRecentNotes(email);

        res.addSource(source, notes ->{
            if(notes == null){
                res.postValue(new ArrayList<>());
                return;
            }
            executor.execute(() -> {
                res.postValue(decryptNotes(notes));
            });
        });
        return res;
    }

    public LiveData<NotesModel> getNoteById(int id){
        MediatorLiveData<NotesModel> res = new MediatorLiveData<>();
        res.addSource(dao.getNoteById(id), note->{
            if(note == null){
                res.postValue(null);
                return;
            }
            executor.execute(()->{
                try {
                    res.postValue(decrypt(note));
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            });

        });
        return res;
    }

    // ------------------------------------------- method for updating the notes -------------------------------------------
    public void updateNote(NotesModel current, SavenotesCallback savenotesCallback) {
        executor.execute(() -> {
            try{
                NotesModel model = encrypt(current);
                dao.update(model);
                savenotesCallback.onSuccess();
            }
            catch(Exception e){
                savenotesCallback.onError(e);
            }
        });
    }


    // ------------------------------------------- method for deleting the notes -------------------------------------------
    public void deleteAllNotes(String email){
        executor.execute(()->{
            dao.deleteAllNotes(email);
        });
    }

    public void deleteById(int id, SavenotesCallback savenotesCallback){
        executor.execute(()->{
            try {
                dao.deleteNoteByid(id);
                savenotesCallback.onSuccess();
            }
            catch (Exception e){
                savenotesCallback.onError(e);
            }
        });
    }

    private NotesModel encrypt(NotesModel current) throws Exception {
        NotesModel model = new NotesModel();
        model.setId(current.getId());
        model.setTitle(helper.encrypt(current.getTitle()));
        model.setContent(helper.encrypt(current.getContent()));
        model.setCategory(current.getCategory());
        model.setUserEmail(current.getUserEmail());
        model.setTimestamp(current.getTimestamp());
        return model;
    }
    private NotesModel decrypt(NotesModel current) throws Exception {
        NotesModel model = new NotesModel();
        model.setId(current.getId());
        model.setTitle(helper.decrypt(current.getTitle()));
        model.setContent(helper.decrypt(current.getContent()));
        model.setCategory(current.getCategory());
        model.setUserEmail(current.getUserEmail());
        model.setTimestamp(current.getTimestamp());

        return model;
    }
    private List<NotesModel> decryptNotes(List<NotesModel> notes){
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
