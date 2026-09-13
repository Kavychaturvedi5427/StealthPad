package com.kavya.stealthpad.data.Local.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "note_attachments",
    foreignKeys = @ForeignKey(
        entity = NotesModel.class,
        parentColumns = "id",
        childColumns = "note_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("note_id")}
)
public class NoteAttachment {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "note_id")
    private int noteId;

    @ColumnInfo(name = "file_name")
    private String fileName;

    @ColumnInfo(name = "local_path")
    private String localPath;

    @ColumnInfo(name = "mime_type")
    private String mimeType;

    @ColumnInfo(name = "file_size")
    private long fileSize;

    public NoteAttachment() {
    }

    public NoteAttachment(int noteId, String fileName, String localPath, String mimeType, long fileSize) {
        this.noteId = noteId;
        this.fileName = fileName;
        this.localPath = localPath;
        this.mimeType = mimeType;
        this.fileSize = fileSize;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNoteId() {
        return noteId;
    }

    public void setNoteId(int noteId) {
        this.noteId = noteId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
}
