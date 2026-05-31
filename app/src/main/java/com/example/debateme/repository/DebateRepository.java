package com.example.debateme.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.debateme.database.DebateDatabase;
import com.example.debateme.database.DebateSessionDao;
import com.example.debateme.models.DebateSession;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DebateRepository {

    private final DebateSessionDao dao;
    private final ExecutorService executor;
    private final LiveData<List<DebateSession>> allSessions;

    public DebateRepository(Application application) {
        DebateDatabase db = DebateDatabase.getInstance(application);
        dao = db.debateSessionDao();
        // FIX 1: Single shared executor — all DB work (reads AND writes) goes
        // through here, so callers never need their own executor for DAO calls.
        executor = Executors.newSingleThreadExecutor();
        allSessions = dao.getAllSessions();
    }

    public void insert(DebateSession session) {
        executor.execute(() -> dao.insert(session));
    }

    public LiveData<List<DebateSession>> getAllSessions() {
        return allSessions;
    }

    // FIX 1: Synchronous DAO methods are now wrapped so they always execute on
    // the background executor. Callers (StatsViewModel) can safely call these
    // from their own background thread — Future.get() blocks until done.
    public int getTotalDebates() {
        return runOnExecutor(dao::getTotalDebates, 0);
    }

    public float getAverageScore() {
        return runOnExecutor(dao::getAverageScore, 0f);
    }

    public DebateSession getBestDebate() {
        return runOnExecutor(dao::getBestDebate, null);
    }

    public void deleteAll() {
        executor.execute(dao::deleteAll);
    }

    /** Submits a callable to the shared executor and blocks for the result. */
    private <T> T runOnExecutor(Callable<T> callable, T fallback) {
        Future<T> future = executor.submit(callable);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            return fallback;
        }
    }
}