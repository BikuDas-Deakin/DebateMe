package com.example.debateme.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.debateme.models.DebateSession;
import com.example.debateme.repository.DebateRepository;

import java.util.List;

public class HistoryViewModel extends AndroidViewModel {

    private final DebateRepository repository;

    public HistoryViewModel(Application application) {
        super(application);
        repository = new DebateRepository(application);
    }

    public LiveData<List<DebateSession>> getAllSessions() {
        return repository.getAllSessions();
    }
}
