package com.kavya.stealthpad.data.Local.Dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.kavya.stealthpad.data.Local.model.NotesModel;

import java.util.List;

@Dao
public interface NotesDao {
    @Query("select * from notes order by timestamp DESC")
    LiveData<List<NotesModel>> getAllNotes();   // notes table will be observed automatically..

    @Insert
    void insert(NotesModel notesModel);

    @Update
    void update(NotesModel notesModel);

    @Query("delete from notes")
    void deleteAllNotes();

    // delete based on id...
    @Query("delete from notes where id = :id")
    void deleteNoteByid(int id);
}
