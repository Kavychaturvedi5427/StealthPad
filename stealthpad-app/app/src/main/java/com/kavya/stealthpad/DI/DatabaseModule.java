package com.kavya.stealthpad.DI;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.kavya.stealthpad.data.Local.Dao.NoteAttachmentDao;
import com.kavya.stealthpad.data.Local.Dao.NotesDao;
import com.kavya.stealthpad.data.Local.db.RoomDBSetup;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    @Provides
    @Singleton
    public RoomDBSetup provideDatabase(
            @ApplicationContext Context context
    ) {

        Migration MIGRATION_3_4 = new Migration(3, 4) {
            @Override
            public void migrate(@NonNull SupportSQLiteDatabase database) {
                database.execSQL("ALTER TABLE notes ADD COLUMN is_vault INTEGER NOT NULL DEFAULT 0");
            }
        };

        return Room.databaseBuilder(
                        context,
                        RoomDBSetup.class,
                        RoomDBSetup.DB_NAME
                )
                .addMigrations(MIGRATION_3_4, RoomDBSetup.MIGRATION_4_5, RoomDBSetup.MIGRATION_5_6)
                .fallbackToDestructiveMigration()
                .build();
    }

    @Provides
    public NotesDao provideNotesDao(
            RoomDBSetup database
    ) {
        return database.notesDao();
    }

    @Provides
    public NoteAttachmentDao provideNoteAttachmentDao(
            RoomDBSetup database
    ) {
        return database.noteAttachmentDao();
    }
}
