package com.example.debateme.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.debateme.R;
import com.example.debateme.databinding.ActivityHomeBinding;
import com.google.android.material.button.MaterialButton;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private String selectedTone = "Casual";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToneButtons();
        setupClickListeners();
    }

    private void setupToneButtons() {
        // Default: Casual selected
        updateToneSelection("Casual");

        binding.btnCasual.setOnClickListener(v -> updateToneSelection("Casual"));
        binding.btnAcademic.setOnClickListener(v -> updateToneSelection("Academic"));
        binding.btnChallenging.setOnClickListener(v -> updateToneSelection("Challenging"));
    }

    private void updateToneSelection(String tone) {
        selectedTone = tone;

        // Reset all buttons
        binding.btnCasual.setAlpha(0.5f);
        binding.btnAcademic.setAlpha(0.5f);
        binding.btnChallenging.setAlpha(0.5f);

        // Highlight selected
        MaterialButton selected;
        int color;
        switch (tone) {
            case "Academic":
                selected = binding.btnAcademic;
                color = getColor(R.color.tone_academic);
                break;
            case "Challenging":
                selected = binding.btnChallenging;
                color = getColor(R.color.tone_challenging);
                break;
            default:
                selected = binding.btnCasual;
                color = getColor(R.color.tone_casual);
                break;
        }
        selected.setAlpha(1.0f);
        binding.tvSelectedTone.setText("Selected: " + tone);
        binding.tvSelectedTone.setTextColor(color);
    }

    private void setupClickListeners() {
        binding.btnStartDebate.setOnClickListener(v -> {
            String topic = binding.etTopic.getText().toString().trim();
            if (topic.isEmpty()) {
                Toast.makeText(this, getString(R.string.empty_topic), Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, DebateActivity.class);
            intent.putExtra("topic", topic);
            intent.putExtra("tone", selectedTone);
            startActivity(intent);
        });

        binding.btnHistory.setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class)));
    }
}
