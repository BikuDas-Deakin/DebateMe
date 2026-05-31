package com.example.debateme.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.debateme.R;
import com.google.android.material.button.MaterialButton;

public class AnalysisActivity extends AppCompatActivity {

    private static final String TAG = "AnalysisActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Debate Analysis");
        }

        String analysis = getIntent().getStringExtra("analysis");
        int    score    = getIntent().getIntExtra("score", 50);
        String topic    = getIntent().getStringExtra("topic");
        String tone     = getIntent().getStringExtra("tone");

        // Log the raw analysis so we can see what the model actually returned
        Log.d(TAG, "Raw analysis:\n" + analysis);

        TextView       tvTopic       = findViewById(R.id.tvTopic);
        TextView       tvTone        = findViewById(R.id.tvTone);
        TextView       tvScore       = findViewById(R.id.tvScore);
        TextView       tvScoreLabel  = findViewById(R.id.tvScoreLabel);
        TextView       tvStrengths   = findViewById(R.id.tvStrengths);
        TextView       tvWeaknesses  = findViewById(R.id.tvWeaknesses);
        TextView       tvVerdict     = findViewById(R.id.tvVerdict);
        ProgressBar    progressScore = findViewById(R.id.progressScore);
        MaterialButton btnDebateAgain= findViewById(R.id.btnDebateAgain);
        MaterialButton btnGoHome     = findViewById(R.id.btnGoHome);

        tvTopic.setText(topic);
        tvTone.setText(tone + " Mode");
        tvScore.setText(score + "/100");
        progressScore.setProgress(score);

        String label;
        if      (score >= 80) label = "Excellent! 🔥";
        else if (score >= 60) label = "Good effort! 👍";
        else if (score >= 40) label = "Keep practicing! 💪";
        else                  label = "Room to improve! 📚";
        tvScoreLabel.setText(label);

        AnalysisSections sections = parseAnalysis(analysis);
        Log.d(TAG, "Parsed — strengths: [" + sections.strengths + "]");
        Log.d(TAG, "Parsed — weaknesses: [" + sections.weaknesses + "]");
        Log.d(TAG, "Parsed — verdict: [" + sections.verdict + "]");

        tvStrengths.setText(sections.strengths);
        tvWeaknesses.setText(sections.weaknesses);
        tvVerdict.setText(sections.verdict);

        btnDebateAgain.setOnClickListener(v -> {
            Intent intent = new Intent(this, DebateActivity.class);
            intent.putExtra("topic", topic);
            intent.putExtra("tone", tone);
            startActivity(intent);
            finish();
        });

        btnGoHome.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finishAffinity();
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Parser
    // ─────────────────────────────────────────────────────────────────────────

    private static class AnalysisSections {
        String strengths  = "";
        String weaknesses = "";
        String verdict    = "";
    }

    /**
     * Robust parser for the LLM analysis response.
     *
     * Handles all common Llama 3.2 output variations:
     *   - Plain headers:      STRENGTHS:
     *   - Numbered headers:   2. WEAKNESSES:
     *   - Markdown bold:      **WEAKNESSES**
     *   - Mixed case:         Weaknesses:
     *   - "Improvement" label instead of "Weaknesses"
     */
    private AnalysisSections parseAnalysis(String raw) {
        AnalysisSections result = new AnalysisSections();
        if (raw == null || raw.isEmpty()) {
            result.strengths  = "No analysis available.";
            result.weaknesses = "No analysis available.";
            result.verdict    = "No verdict available.";
            return result;
        }

        // 1. Normalise line endings and strip markdown bold markers (**text**)
        String text = raw
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .replaceAll("\\*\\*", "");          // remove ** bold markers
        // Strip leading numbers+dots from lines e.g. "1. STRENGTHS:" → "STRENGTHS:"
        text = text.replaceAll("(?m)^\\s*\\d+\\.\\s*", "");

        String upper = text.toUpperCase();

        // 2. Find each section's start index using all known aliases
        int idxStrengths  = firstOf(upper, "STRENGTHS");
        int idxWeaknesses = firstOf(upper, "WEAKNESSES", "AREAS TO IMPROVE",
                                           "IMPROVEMENTS", "AREAS FOR IMPROVEMENT",
                                           "IMPROVEMENT");
        int idxVerdict    = firstOf(upper, "VERDICT", "CONCLUSION", "SUMMARY", "OVERALL");

        // 3. Extract each section as text between its header and the next header
        if (idxStrengths != -1) {
            int end = earliestAfter(upper, idxStrengths + 1, idxWeaknesses, idxVerdict);
            result.strengths = extractContent(text, upper, "STRENGTHS", idxStrengths, end);
        }

        if (idxWeaknesses != -1) {
            // Find which alias actually matched so we can skip past it correctly
            String weaknessHeader = whichMatched(upper, idxWeaknesses,
                    "WEAKNESSES", "AREAS TO IMPROVE", "AREAS FOR IMPROVEMENT",
                    "IMPROVEMENTS", "IMPROVEMENT");
            int end = earliestAfter(upper, idxWeaknesses + 1, idxStrengths, idxVerdict);
            result.weaknesses = extractContent(text, upper, weaknessHeader, idxWeaknesses, end);
        }

        if (idxVerdict != -1) {
            // Verdict goes to end of text — no stop tokens needed
            String verdictHeader = whichMatched(upper, idxVerdict,
                    "VERDICT", "CONCLUSION", "SUMMARY", "OVERALL");
            result.verdict = extractContent(text, upper, verdictHeader, idxVerdict, text.length());
        }

        // 4. Fallback: if everything failed, dump raw text into strengths
        if (result.strengths.isEmpty() && result.weaknesses.isEmpty() && result.verdict.isEmpty()) {
            result.strengths = text.trim();
        }

        if (result.strengths.isEmpty())  result.strengths  = "Not available.";
        if (result.weaknesses.isEmpty()) result.weaknesses = "Not available.";
        if (result.verdict.isEmpty())    result.verdict    = "Not available.";

        return result;
    }

    /**
     * Returns the earliest index at which any of the given keywords appears
     * in {@code upper}, or -1 if none found.
     */
    private int firstOf(String upper, String... keywords) {
        int earliest = -1;
        for (String kw : keywords) {
            int idx = upper.indexOf(kw);
            if (idx != -1 && (earliest == -1 || idx < earliest)) {
                earliest = idx;
            }
        }
        return earliest;
    }

    /**
     * Returns which of the given keywords matched at position {@code pos}.
     * Falls back to the first keyword if none match exactly (shouldn't happen).
     */
    private String whichMatched(String upper, int pos, String... keywords) {
        for (String kw : keywords) {
            if (upper.indexOf(kw) == pos) return kw;
        }
        return keywords[0];
    }

    /**
     * Given a set of candidate end positions (some may be -1 meaning absent),
     * returns the smallest positive one that is strictly after {@code after},
     * or {@code text.length()} if none qualify.
     */
    private int earliestAfter(String upper, int after, int... candidates) {
        int end = upper.length();
        for (int c : candidates) {
            if (c > after && c < end) end = c;
        }
        return end;
    }

    /**
     * Extracts the content that follows a header at {@code headerPos} up to {@code end}.
     * Skips past the header keyword, an optional colon, and leading whitespace.
     */
    private String extractContent(String text, String upper,
                                  String header, int headerPos, int end) {
        int start = headerPos + header.length();
        // Skip optional colon
        if (start < text.length() && text.charAt(start) == ':') start++;
        // Skip whitespace / newlines
        while (start < end && (text.charAt(start) == ' '
                || text.charAt(start) == '\n'
                || text.charAt(start) == '\t')) {
            start++;
        }
        if (start >= end) return "";
        return text.substring(start, end).trim();
    }

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            startActivity(new Intent(this, HomeActivity.class));
            finishAffinity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
