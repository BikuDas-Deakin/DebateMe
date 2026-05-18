package com.example.debateme.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.debateme.database.DebateDatabase;
import com.example.debateme.database.DebateSessionDao;
import com.example.debateme.models.DebateSession;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DebateRepository {

    private final DebateSessionDao dao;
    private final ExecutorService executor;
    private final LiveData<List<DebateSession>> allSessions;

    public DebateRepository(Application application) {
        DebateDatabase db = DebateDatabase.getInstance(application);
        dao = db.debateSessionDao();
        executor = Executors.newSingleThreadExecutor();
        allSessions = dao.getAllSessions();
    }

    public void insert(DebateSession session) {
        executor.execute(() -> dao.insert(session));
    }

    public LiveData<List<DebateSession>> getAllSessions() {
        return allSessions;
    }

    public void deleteAll() {
        executor.execute(dao::deleteAll);
    }
}
