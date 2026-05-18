package com.example.debateme.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.debateme.models.DebateSession;

import java.util.List;

@Dao
public interface DebateSessionDao {

    @Insert
    void insert(DebateSession session);

    @Query("SELECT * FROM debate_sessions ORDER BY timestamp DESC")
    LiveData<List<DebateSession>> getAllSessions();

    @Query("DELETE FROM debate_sessions WHERE id = :id")
    void deleteById(int id);

    @Query("DELETE FROM debate_sessions")
    void deleteAll();
}
