package com.example.debateme.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.debateme.models.DebateSession;
import com.example.debateme.repository.DebateRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StatsViewModel extends AndroidViewModel {

    private final DebateRepository repository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final MutableLiveData<Integer> totalDebates = new MutableLiveData<>(0);
    private final MutableLiveData<Float> averageScore = new MutableLiveData<>(0f);
    private final MutableLiveData<DebateSession> bestDebate = new MutableLiveData<>();

    public StatsViewModel(Application application) {
        super(application);
        repository = new DebateRepository(application);
        loadStats();
    }

    private void loadStats() {
        executor.execute(() -> {
            int total = repository.getTotalDebates();
            float avg = repository.getAverageScore();
            DebateSession best = repository.getBestDebate();

            mainHandler.post(() -> {
                totalDebates.setValue(total);
                averageScore.setValue(avg);
                bestDebate.setValue(best);
            });
        });
    }

    public LiveData<Integer> getTotalDebates() { return totalDebates; }
    public LiveData<Float> getAverageScore() { return averageScore; }
    public LiveData<DebateSession> getBestDebate() { return bestDebate; }
}