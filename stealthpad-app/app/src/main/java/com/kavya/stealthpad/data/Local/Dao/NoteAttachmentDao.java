package com.kavya.stealthpad.data.Local.Dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.kavya.stealthpad.data.Local.model.NoteAttachment;

import java.util.List;

@Dao
public interface NoteAttachmentDao {

    @Insert
    long insert(NoteAttachment attachment);

    @Query("SELECT * FROM note_attachments WHERE note_id = :noteId")
    LiveData<List<NoteAttachment>> getAttachmentsForNote(int noteId);

    @Query("SELECT * FROM note_attachments WHERE note_id = :noteId")
    List<NoteAttachment> getAttachmentsForNoteSync(int noteId);

    @Delete
    void delete(NoteAttachment attachment);

    @Query("DELETE FROM note_attachments WHERE note_id = :noteId")
    void deleteAttachmentsForNote(int noteId);
}
