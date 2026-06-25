package com.kavya.stealthpad.data.Local.Dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.kavya.stealthpad.data.Local.model.NotesModel;
import com.kavya.stealthpad.ui.notes.Notes;

import java.util.List;

@Dao
public interface NotesDao {
    @Query("select * from notes where user_email = :email order by timestamp DESC")
    LiveData<List<NotesModel>> getAllNotes(String email);   // notes table will be observed automatically..

    // fetch recent 5 notes...
    @Query("select * from notes where user_email = :email order by timestamp DESC LIMIT 3")
    LiveData<List<NotesModel>> getRecentNotes(String email);

    @Query("select * from notes where user_email = :email order by timestamp")
    LiveData<List<NotesModel>> getNotesByEmail(String email);

    @Query("select * from notes where id = :id")
    LiveData<NotesModel> getNoteById(int id);

    @Insert
    void insert(NotesModel notesModel);

    @Update
    void update(NotesModel notesModel);

    @Query("delete from notes where user_email = :email")
    void deleteAllNotes(String email);

    // delete based on id...
    @Query("delete from notes where id = :id")
    void deleteNoteByid(int id);
}
