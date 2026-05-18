package com.example.debateme.viewmodel;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.debateme.models.ChatMessage;
import com.example.debateme.models.DebateSession;
import com.example.debateme.repository.DebateRepository;
import com.google.mediapipe.tasks.genai.llminference.LlmInference;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DebateViewModel extends AndroidViewModel {

    private static final String TAG = "DebateViewModel";
    private static final String MODEL_PATH = "/data/local/tmp/llm/model.bin";

    private final MutableLiveData<List<ChatMessage>> messages = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> status = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isThinking = new MutableLiveData<>(false);

    private LlmInference llmInference;
    private String systemPrompt;
    private String topic;
    private String tone;
    private final DebateRepository repository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public DebateViewModel(Application application) {
        super(application);
        repository = new DebateRepository(application);
    }

    public void initModel(Context context) {
        status.setValue("Loading AI model…");
        executor.execute(() -> {
            try {
                LlmInference.LlmInferenceOptions options = LlmInference.LlmInferenceOptions.builder()
                        .setModelPath(MODEL_PATH)
                        .setMaxTokens(300)
                        .build();
                llmInference = LlmInference.createFromOptions(context, options);
                llmInference = LlmInference.createFromOptions(context, options);
                mainHandler.post(() -> status.setValue("AI ready. Make your case!"));
            } catch (Exception e) {
                Log.e(TAG, "Model load error: " + e.getMessage());
                mainHandler.post(() -> status.setValue("Model error: " + e.getMessage()));
            }
        });
    }

    public void setupDebate(String topic, String tone) {
        this.topic = topic;
        this.tone = tone;
        this.systemPrompt = PromptBuilder.buildSystemPrompt(topic, tone);
        messages.setValue(new ArrayList<>());
    }

    public void sendMessage(String userMessage) {
        if (llmInference == null) {
            status.setValue("AI model not ready yet. Please wait.");
            return;
        }

        List<ChatMessage> current = messages.getValue();
        if (current == null) current = new ArrayList<>();

        // Add user message
        current.add(new ChatMessage(userMessage, ChatMessage.TYPE_USER));
        messages.setValue(new ArrayList<>(current));
        isThinking.setValue(true);

        final List<ChatMessage> historySnapshot = new ArrayList<>(current);

        executor.execute(() -> {
            try {
                String fullPrompt = PromptBuilder.buildFullPrompt(systemPrompt, historySnapshot, userMessage);
                String response = llmInference.generateResponse(fullPrompt);

                // Clean up response
                if (response != null) {
                    response = response.trim();
                    // Remove any "User:" continuation if model hallucinates
                    int userIndex = response.indexOf("\nUser:");
                    if (userIndex != -1) {
                        response = response.substring(0, userIndex).trim();
                    }
                }

                final String finalResponse = (response != null && !response.isEmpty())
                        ? response : "I disagree. Please elaborate on your point.";

                mainHandler.post(() -> {
                    List<ChatMessage> updated = messages.getValue();
                    if (updated == null) updated = new ArrayList<>();
                    updated.add(new ChatMessage(finalResponse, ChatMessage.TYPE_AI));
                    messages.setValue(new ArrayList<>(updated));
                    isThinking.setValue(false);
                });

            } catch (Exception e) {
                Log.e(TAG, "Inference error: " + e.getMessage());
                mainHandler.post(() -> {
                    List<ChatMessage> updated = messages.getValue();
                    if (updated == null) updated = new ArrayList<>();
                    updated.add(new ChatMessage("I strongly disagree with that point. Care to elaborate?", ChatMessage.TYPE_AI));
                    messages.setValue(new ArrayList<>(updated));
                    isThinking.setValue(false);
                    status.setValue("Error during inference");
                });
            }
        });
    }

    public void saveSession() {
        List<ChatMessage> current = messages.getValue();
        if (current == null || current.isEmpty()) return;

        try {
            JSONArray jsonArray = new JSONArray();
            for (ChatMessage msg : current) {
                JSONObject obj = new JSONObject();
                obj.put("message", msg.getMessage());
                obj.put("type", msg.getType());
                obj.put("timestamp", msg.getTimestamp());
                jsonArray.put(obj);
            }

            DebateSession session = new DebateSession(
                    topic, tone, jsonArray.toString(),
                    System.currentTimeMillis(), current.size()
            );
            repository.insert(session);
        } catch (Exception e) {
            Log.e(TAG, "Save error: " + e.getMessage());
        }
    }

    public LiveData<List<ChatMessage>> getMessages() { return messages; }
    public LiveData<String> getStatus() { return status; }
    public LiveData<Boolean> getIsThinking() { return isThinking; }
    public String getTopic() { return topic; }
    public String getTone() { return tone; }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (llmInference != null) {
            llmInference.close();
        }
        executor.shutdown();
    }
}
