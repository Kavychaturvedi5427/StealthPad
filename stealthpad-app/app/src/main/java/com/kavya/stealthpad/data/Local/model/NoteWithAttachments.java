package com.kavya.stealthpad.data.Local.model;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class NoteWithAttachments {
    @Embedded
    public NotesModel note;

    @Relation(
        parentColumn = "id",
        entityColumn = "note_id"
    )
    public List<NoteAttachment> attachments;
}
