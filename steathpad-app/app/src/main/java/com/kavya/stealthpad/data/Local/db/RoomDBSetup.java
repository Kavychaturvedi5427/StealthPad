package com.kavya.stealthpad.data.Local.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.kavya.stealthpad.data.Local.Dao.NotesDao;
import com.kavya.stealthpad.data.Local.model.NotesModel;

@Database(entities = {NotesModel.class}, version = 2, exportSchema = false)
public abstract class RoomDBSetup extends RoomDatabase {
    public static final String DB_NAME = "Notes_Room";

    public abstract NotesDao notesDao();
}

/*
* This file defines the schema of the db......
* The actual instance will be created by the Database Module...
* */
