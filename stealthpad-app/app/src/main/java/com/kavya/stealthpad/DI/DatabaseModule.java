package com.kavya.stealthpad.DI;

import android.content.Context;

import androidx.room.Room;

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

        return Room.databaseBuilder(
                        context,
                        RoomDBSetup.class,
                        RoomDBSetup.DB_NAME
                )
                .fallbackToDestructiveMigration()
                .build();
    }

    @Provides
    public NotesDao provideNotesDao(
            RoomDBSetup database
    ) {
        return database.notesDao();
    }
}