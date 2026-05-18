package com.example.debateme.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.debateme.adapters.HistoryAdapter;
import com.example.debateme.databinding.ActivityHistoryBinding;
import com.example.debateme.viewmodel.HistoryViewModel;

public class HistoryActivity extends AppCompatActivity {

    private ActivityHistoryBinding binding;
    private HistoryAdapter historyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setupRecyclerView();
        setupViewModel();
    }

    private void setupRecyclerView() {
        historyAdapter = new HistoryAdapter();
        binding.recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewHistory.setAdapter(historyAdapter);
    }

    private void setupViewModel() {
        HistoryViewModel viewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
        viewModel.getAllSessions().observe(this, sessions -> {
            if (sessions == null || sessions.isEmpty()) {
                binding.recyclerViewHistory.setVisibility(View.GONE);
                binding.tvEmpty.setVisibility(View.VISIBLE);
            } else {
                binding.recyclerViewHistory.setVisibility(View.VISIBLE);
                binding.tvEmpty.setVisibility(View.GONE);
                historyAdapter.setSessions(sessions);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
