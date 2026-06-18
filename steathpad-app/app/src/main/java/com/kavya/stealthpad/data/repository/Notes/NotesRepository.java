package com.kavya.stealthpad.data.repository.Notes;

import androidx.lifecycle.LiveData;

import com.kavya.stealthpad.data.DataModel.NoteVmDao;
import com.kavya.stealthpad.data.Local.Dao.NotesDao;
import com.kavya.stealthpad.data.Local.model.NotesModel;

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
    public void saveNote(String title, String content, String category, SavenotesCallback callback) {
        NotesModel model = new NotesModel(title, content, System.currentTimeMillis(), category);
        executor.execute(()->{
            try {
                dao.insert(model);
                callback.onSuccess();
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public LiveData<List<NotesModel>> getAllNotes(){
        return dao.getAllNotes();
    }

    public LiveData<List<NotesModel>> getRecentNotes(){
        return dao.getRecentNotes();
    }


}
