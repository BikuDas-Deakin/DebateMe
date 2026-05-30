package com.example.debateme.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.debateme.database.DebateDatabase;
import com.example.debateme.models.DebateSession;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * FIX 6: Loads a single DebateSession by id on a background thread and
 * exposes it as LiveData for HistoryDetailActivity.
 */
public class HistoryDetailViewModel extends AndroidViewModel {

    private final MutableLiveData<DebateSession> session = new MutableLiveData<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public HistoryDetailViewModel(Application application) {
        super(application);
    }

    public void loadSession(int id) {
        executor.execute(() -> {
            DebateSession s = DebateDatabase.getInstance(getApplication())
                    .debateSessionDao()
                    .getSessionById(id);
            mainHandler.post(() -> session.setValue(s));
        });
    }

    public LiveData<DebateSession> getSession() { return session; }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}
