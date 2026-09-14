package com.kavya.stealthpad.data.Local.Dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.kavya.stealthpad.data.Local.model.NoteWithAttachments;
import com.kavya.stealthpad.data.Local.model.NotesModel;

import java.util.List;

@Dao
public interface NotesDao {
    @androidx.room.Transaction
    @Query("select * from notes where user_email = :email and is_vault = 0 and sync_status != 5 order by last_updated DESC, id DESC")
    LiveData<List<NoteWithAttachments>> getAllNotesWithAttachments(String email);

    @androidx.room.Transaction
    @Query("select * from notes where user_email = :email and is_vault = 0 and sync_status != 5 order by timestamp DESC, id DESC")
    LiveData<List<NoteWithAttachments>> getAllNotesNewest(String email);

    @androidx.room.Transaction
    @Query("select * from notes where user_email = :email and is_vault = 0 and sync_status != 5 order by timestamp ASC, id ASC")
    LiveData<List<NoteWithAttachments>> getAllNotesOldest(String email);

    @androidx.room.Transaction
    @Query("select * from notes where user_email = :email and is_vault = 0 and sync_status != 5 order by title COLLATE NOCASE ASC, id DESC")
    LiveData<List<NoteWithAttachments>> getAllNotesAlphabetical(String email);

    @androidx.room.Transaction
    @Query("select * from notes where user_email = :email and is_vault = 0 and sync_status != 5 order by last_updated DESC, id DESC LIMIT 5")
    LiveData<List<NoteWithAttachments>> getRecentNotesWithAttachments(String email);

    @androidx.room.Transaction
    @Query("select * from notes where user_email = :email and is_vault = 0 and sync_status != 5 order by timestamp DESC, id DESC LIMIT 5")
    LiveData<List<NoteWithAttachments>> getNewestNotesWithAttachments(String email);

    @androidx.room.Transaction
    @Query("select * from notes where user_email = :email and is_vault = 0 and sync_status != 5 order by timestamp ASC, id ASC LIMIT 5")
    LiveData<List<NoteWithAttachments>> getOldestNotesWithAttachments(String email);

    @androidx.room.Transaction
    @Query("select * from notes where user_email = :email and is_vault = 0 and sync_status != 5 order by title COLLATE NOCASE ASC, id DESC LIMIT 5")
    LiveData<List<NoteWithAttachments>> getAlphabeticalNotesWithAttachments(String email);

    @androidx.room.Transaction
    @Query("select * from notes where user_email = :email and category = :category and is_vault = 0 and sync_status != 5 order by last_updated DESC")
    LiveData<List<NoteWithAttachments>> getNotesByCategoryWithAttachments(String email, String category);

    @androidx.room.Transaction
    @Query("select * from notes where user_email = :email and category = :category and is_vault = 0 and sync_status != 5 order by timestamp DESC")
    LiveData<List<NoteWithAttachments>> getNotesByCategoryNewest(String email, String category);

    @androidx.room.Transaction
    @Query("select * from notes where user_email = :email and category = :category and is_vault = 0 and sync_status != 5 order by timestamp ASC")
    LiveData<List<NoteWithAttachments>> getNotesByCategoryOldest(String email, String category);

    @androidx.room.Transaction
    @Query("select * from notes where user_email = :email and category = :category and is_vault = 0 and sync_status != 5 order by title COLLATE NOCASE ASC")
    LiveData<List<NoteWithAttachments>> getNotesByCategoryAlphabetical(String email, String category);

    @androidx.room.Transaction
    @Query("select * from notes where user_email = :email and is_vault = 1 and sync_status != 5 order by timestamp DESC")
    LiveData<List<NoteWithAttachments>> getVaultNotesWithAttachments(String email);

    @Query("select * from notes where user_email = :email and sync_status != 5 order by timestamp")
    LiveData<List<NotesModel>> getNotesByEmail(String email);

    @Query("select * from notes where id = :id")
    LiveData<NotesModel> getNoteById(int id);

    @Insert
    long insert(NotesModel notesModel);

    @Update
    void update(NotesModel notesModel);

    @Query("delete from notes where user_email = :email")
    void deleteAllNotes(String email);

    // delete based on id...
    @Query("delete from notes where id = :id")
    void deleteNoteByid(int id);

    @androidx.room.Transaction
    @Query("SELECT * FROM notes WHERE user_email = :email AND is_vault = 0 AND sync_status != 5 AND (title LIKE :query OR content LIKE :query) ORDER BY last_updated DESC")
    LiveData<List<NoteWithAttachments>> searchNotes(String email, String query);


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
    @Query("select * from notes where user_email = :email and sync_status in (0, 2, 3, 4, 5)")
    List<NotesModel> getNotesPendingSync(String email);

    // Find a local note using the backend/server ID
    @Query("SELECT * FROM notes WHERE server_id = :serverId AND user_email = :email LIMIT 1")
    NotesModel getNoteByServerId(Long serverId, String email);


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

    @Query("SELECT * FROM notes WHERE user_email = :email AND server_id IS NOT NULL")
    List<NotesModel> getAllNotesWithServerIdSync(String email);

}
