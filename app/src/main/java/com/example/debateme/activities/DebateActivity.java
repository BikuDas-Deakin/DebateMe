package com.example.debateme.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.debateme.R;
import com.example.debateme.adapters.ChatAdapter;
import com.example.debateme.databinding.ActivityDebateBinding;
import com.example.debateme.viewmodel.DebateViewModel;

public class DebateActivity extends AppCompatActivity {

    private ActivityDebateBinding binding;
    private DebateViewModel viewModel;
    private ChatAdapter chatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDebateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String topic = getIntent().getStringExtra("topic");
        String tone = getIntent().getStringExtra("tone");

        setupToolbar(topic, tone);
        setupRecyclerView();
        setupViewModel(topic, tone);
        setupSendButton();
    }

    private void setupToolbar(String topic, String tone) {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(tone + " Debate");
            getSupportActionBar().setSubtitle(topic != null && topic.length() > 40
                    ? topic.substring(0, 40) + "…" : topic);
        }
    }

    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.recyclerViewChat.setLayoutManager(layoutManager);
        binding.recyclerViewChat.setAdapter(chatAdapter);
    }

    private void setupViewModel(String topic, String tone) {
        viewModel = new ViewModelProvider(this).get(DebateViewModel.class);
        viewModel.setupDebate(topic, tone);
        viewModel.initModel(this);

        viewModel.getMessages().observe(this, messages -> {
            chatAdapter.setMessages(messages);
            if (!messages.isEmpty()) {
                binding.recyclerViewChat.scrollToPosition(messages.size() - 1);
            }
        });

        viewModel.getStatus().observe(this, status ->
                binding.tvStatus.setText(status));

        viewModel.getIsThinking().observe(this, isThinking -> {
            binding.layoutTyping.setVisibility(isThinking ? View.VISIBLE : View.GONE);
            binding.btnSend.setEnabled(!isThinking);
            binding.etMessage.setEnabled(!isThinking);
        });
    }

    private void setupSendButton() {
        binding.btnSend.setOnClickListener(v -> {
            String message = binding.etMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                viewModel.sendMessage(message);
                binding.etMessage.setText("");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "End & Save").setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == 1) {
            showEndDebateDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showEndDebateDialog() {
        new AlertDialog.Builder(this)
                .setTitle("End Debate")
                .setMessage("Save this debate session to history?")
                .setPositiveButton("Save & Exit", (dialog, which) -> {
                    viewModel.saveSession();
                    finish();
                })
                .setNegativeButton("Exit Without Saving", (dialog, which) -> finish())
                .setNeutralButton("Continue", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        showEndDebateDialog();
    }
}
