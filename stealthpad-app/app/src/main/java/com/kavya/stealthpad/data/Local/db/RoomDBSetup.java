package com.kavya.stealthpad.data.Local.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.kavya.stealthpad.data.Local.Dao.NoteAttachmentDao;
import com.kavya.stealthpad.data.Local.Dao.NotesDao;
import com.kavya.stealthpad.data.Local.model.NoteAttachment;
import com.kavya.stealthpad.data.Local.model.NotesModel;

@Database(entities = {NotesModel.class, NoteAttachment.class}, version = 6, exportSchema = false)
public abstract class RoomDBSetup extends RoomDatabase {
    public static final String DB_NAME = "Notes_Room";

    public abstract NotesDao notesDao();
    public abstract NoteAttachmentDao noteAttachmentDao();

    public static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `note_attachments` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`note_id` INTEGER NOT NULL, " +
                    "`file_name` TEXT, " +
                    "`local_path` TEXT, " +
                    "`mime_type` TEXT, " +
                    "`file_size` INTEGER NOT NULL, " +
                    "FOREIGN KEY(`note_id`) REFERENCES `notes`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_note_attachments_note_id` ON `note_attachments` (`note_id`) ");
        }
    };

    public static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE notes ADD COLUMN last_updated INTEGER NOT NULL DEFAULT 0");
            // Set last_updated to timestamp for existing notes
            database.execSQL("UPDATE notes SET last_updated = timestamp");
        }
    };
}

/*
* This file defines the schema of the db......
* The actual instance will be created by the Database Module...
* */
