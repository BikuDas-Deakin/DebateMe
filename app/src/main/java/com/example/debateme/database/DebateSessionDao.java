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

    @Query("SELECT COUNT(*) FROM debate_sessions")
    int getTotalDebates();

    @Query("SELECT AVG(score) FROM debate_sessions WHERE score > 0")
    float getAverageScore();

    @Query("SELECT tone FROM debate_sessions GROUP BY tone ORDER BY COUNT(*) DESC LIMIT 1")
    String getFavouriteTone();

    @Query("SELECT * FROM debate_sessions ORDER BY score DESC LIMIT 1")
    DebateSession getBestDebate();

    // FIX 6: Fetch a single session by primary key for HistoryDetailActivity.
    @Query("SELECT * FROM debate_sessions WHERE id = :id LIMIT 1")
    DebateSession getSessionById(int id);

    @Query("DELETE FROM debate_sessions WHERE id = :id")
    void deleteById(int id);

    @Query("DELETE FROM debate_sessions")
    void deleteAll();
}