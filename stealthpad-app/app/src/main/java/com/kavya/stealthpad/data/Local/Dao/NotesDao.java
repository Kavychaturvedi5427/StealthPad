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
    @Query("select * from notes where user_email = :email and is_vault = 0 order by timestamp DESC")
    LiveData<List<NotesModel>> getAllNotes(String email);   // notes table will be observed automatically..

    @Query("select * from notes where user_email = :email and category = :category and is_vault = 0 order by timestamp DESC")
    LiveData<List<NotesModel>> getNotesByCategory(String email, String category);

    // fetch recent 5 notes...
    @Query("select * from notes where user_email = :email and is_vault = 0 order by timestamp DESC LIMIT 3")
    LiveData<List<NotesModel>> getRecentNotes(String email);

    @Query("select * from notes where user_email = :email and is_vault = 1 order by timestamp DESC")
    LiveData<List<NotesModel>> getVaultNotes(String email);

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


    /*
    * =======================================
    *       SYNC QUERIES
    * =======================================
    * */

    @Query("SELECT * FROM notes WHERE id = :id")
    NotesModel getNoteByIdSync(int id);

    // fetch the notes which have particular sync status..
    @Query("select * from notes where sync_status = :status")
    List<NotesModel> getPendingNotes(int status);

    // fetch all notes that need synchronization...
    @Query("select * from notes where sync_status in (0, 2, 3, 4, 5)")
    List<NotesModel> getNotesPendingSync();

    // Find a local note using the backend/server ID
    @Query("SELECT * FROM notes WHERE server_id = :serverId LIMIT 1")
    NotesModel getNoteByServerId(Long serverId);


    // Update sync status
    @Query("UPDATE notes SET sync_status = :status WHERE id = :id")
    void updateSyncStatus(int id, int status);


    // Update server ID after successful creation
    @Query("UPDATE notes SET server_id = :serverId, sync_status = :status WHERE id = :id")
    void updateServerIdAndSyncStatus(
            int id,
            Long serverId,
            int status
    );

    @Query("DELETE FROM notes WHERE id = :id")
    void deleteNoteAfterSync(int id);

}
