package com.example.debateme.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.debateme.models.DebateSession;

@Database(entities = {DebateSession.class}, version = 1, exportSchema = false)
public abstract class DebateDatabase extends RoomDatabase {

    private static DebateDatabase instance;

    public abstract DebateSessionDao debateSessionDao();

    public static synchronized DebateDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    DebateDatabase.class,
                    "debate_database"
            ).fallbackToDestructiveMigration().build();
        }
        return instance;
    }
}
