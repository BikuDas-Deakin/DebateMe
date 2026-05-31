package com.example.debateme.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.example.debateme.R;
import com.example.debateme.viewmodel.StatsViewModel;

public class StatsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Stats");
        }

        TextView tvTotalDebates = findViewById(R.id.tvTotalDebates);
        TextView tvAvgScore = findViewById(R.id.tvAvgScore);
        TextView tvBestTopic = findViewById(R.id.tvBestTopic);
        TextView tvStreakLabel = findViewById(R.id.tvStreakLabel);

        StatsViewModel viewModel = new ViewModelProvider(this).get(StatsViewModel.class);

        viewModel.getTotalDebates().observe(this, total ->
                tvTotalDebates.setText(String.valueOf(total)));

        viewModel.getAverageScore().observe(this, avg ->
                tvAvgScore.setText(String.format("%.0f/100", avg)));

        viewModel.getBestDebate().observe(this, session -> {
            if (session != null) {
                tvBestTopic.setText(session.getTopic());
                tvStreakLabel.setText("Best score: " + session.getScore() + "/100");
            } else {
                tvBestTopic.setText("No debates yet");
                tvStreakLabel.setText("Start debating to see your stats!");
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}