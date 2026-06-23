package com.kavya.stealthpad.data.repository.Notes;

import androidx.lifecycle.LiveData;

import com.kavya.stealthpad.data.DataModel.NoteVmDao;
import com.kavya.stealthpad.data.Local.Dao.NotesDao;
import com.kavya.stealthpad.data.Local.model.NotesModel;
import com.kavya.stealthpad.utils.SessionManager;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

public class NotesRepository {



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
    public void saveNote(String title, String content, String category, String email, SavenotesCallback callback) {
        NotesModel model = new NotesModel(title, content, System.currentTimeMillis(), category, email);
        executor.execute(()->{
            try {
                dao.insert(model);
                callback.onSuccess();
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    // ------------------------------------------- methods for fetching the notes -------------------------------------------
    public LiveData<List<NotesModel>> getAllNotes(String email){
        return dao.getAllNotes(email);
    }

    public LiveData<List<NotesModel>> getRecentNotes(String email){
        return dao.getRecentNotes(email);
    }

    public LiveData<NotesModel> getNoteById(int id){
        return dao.getNoteById(id);
    }

    // ------------------------------------------- method for updating the notes -------------------------------------------
    public void updateNote(NotesModel current, SavenotesCallback savenotesCallback) {
        executor.execute(() -> {
            try{
                dao.update(current);
                savenotesCallback.onSuccess();
            }
            catch(Exception e){
                savenotesCallback.onError(e);
            }
        });
    }


    // ------------------------------------------- method for deleting the notes -------------------------------------------
    public void deleteAllNotes(String email){
        dao.deleteAllNotes(email);
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


}
