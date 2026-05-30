package com.example.debateme.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.example.debateme.R;
import com.example.debateme.viewmodel.HistoryDetailViewModel;

/**
 * FIX 6: Shows the stored analysis and score for a past DebateSession so users
 * can review their performance after tapping a row in HistoryActivity.
 */
public class HistoryDetailActivity extends AppCompatActivity {

    public static final String EXTRA_SESSION_ID = "session_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Debate Review");
        }

        int sessionId = getIntent().getIntExtra(EXTRA_SESSION_ID, -1);

        TextView tvTopic      = findViewById(R.id.tvTopic);
        TextView tvTone       = findViewById(R.id.tvTone);
        TextView tvScore      = findViewById(R.id.tvScore);
        TextView tvScoreLabel = findViewById(R.id.tvScoreLabel);
        TextView tvAnalysis   = findViewById(R.id.tvAnalysis);
        ProgressBar progressScore = findViewById(R.id.progressScore);

        HistoryDetailViewModel viewModel =
                new ViewModelProvider(this).get(HistoryDetailViewModel.class);
        viewModel.loadSession(sessionId);

        viewModel.getSession().observe(this, session -> {
            if (session == null) return;

            tvTopic.setText(session.getTopic());
            tvTone.setText(session.getTone() + " Mode");

            int score = session.getScore();
            tvScore.setText(score + "/100");
            progressScore.setProgress(score);

            String label;
            if (score >= 80)      label = "Excellent! 🔥";
            else if (score >= 60) label = "Good effort! 👍";
            else if (score >= 40) label = "Keep practicing! 💪";
            else                  label = "Room to improve! 📚";
            tvScoreLabel.setText(label);

            String analysis = session.getAnalysisResult();
            tvAnalysis.setText((analysis != null && !analysis.isEmpty())
                    ? analysis : "No analysis was saved for this session.");
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
