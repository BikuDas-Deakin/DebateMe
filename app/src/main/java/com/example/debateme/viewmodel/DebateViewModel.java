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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DebateViewModel extends AndroidViewModel {

    private static final String TAG = "DebateViewModel";
    private static final String MODEL_PATH = "/data/local/tmp/llm/model.bin";
    private static final Pattern SCORE_PATTERN =
            Pattern.compile("SCORE:\\s*(\\d{1,3})", Pattern.CASE_INSENSITIVE);

    private final MutableLiveData<List<ChatMessage>> messages       = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String>            status         = new MutableLiveData<>();
    private final MutableLiveData<Boolean>           isThinking     = new MutableLiveData<>(false);
    private final MutableLiveData<String>            analysisResult = new MutableLiveData<>();
    private final MutableLiveData<Integer>           debateScore    = new MutableLiveData<>();

    private LlmInference llmInference;
    private String systemPrompt;
    private String topic;
    private String tone;
    private final DebateRepository repository;
    private final ExecutorService executor    = Executors.newSingleThreadExecutor();
    private final Handler         mainHandler = new Handler(Looper.getMainLooper());

    public DebateViewModel(Application application) {
        super(application);
        repository = new DebateRepository(application);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Model init
    // ─────────────────────────────────────────────────────────────────────────

    public void initModel(Context context) {
        status.setValue("Loading AI model…");
        executor.execute(() -> {
            try {
                llmInference = createLlmInference(context);
                mainHandler.post(() -> status.setValue("AI ready. Make your case!"));
            } catch (Exception e) {
                Log.e(TAG, "Model load error: " + e.getMessage());
                mainHandler.post(() -> status.setValue("Model error: " + e.getMessage()));
            }
        });
    }

    private LlmInference createLlmInference(Context context) {
        try {
            Log.d(TAG, "Attempting default (GPU) backend…");
            LlmInference.LlmInferenceOptions options = LlmInference.LlmInferenceOptions.builder()
                    .setModelPath(MODEL_PATH)
                    .setMaxTokens(512)
                    .build();
            return LlmInference.createFromOptions(context, options);
        } catch (Exception gpuError) {
            Log.w(TAG, "GPU backend failed (" + gpuError.getMessage() + "), retrying on CPU");
            LlmInference.LlmInferenceOptions cpuOptions = LlmInference.LlmInferenceOptions.builder()
                    .setModelPath(MODEL_PATH)
                    .setMaxTokens(512)
                    .build();
            return LlmInference.createFromOptions(context, cpuOptions);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Debate setup
    // ─────────────────────────────────────────────────────────────────────────

    public void setupDebate(String topic, String tone) {
        this.topic        = topic;
        this.tone         = tone;
        this.systemPrompt = PromptBuilder.buildSystemPrompt(topic, tone);
        messages.setValue(new ArrayList<>());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Send message — counter-argument + quality rating in one executor chain
    // ─────────────────────────────────────────────────────────────────────────

    public void sendMessage(String userMessage) {
        if (llmInference == null) {
            status.setValue("AI model not ready yet. Please wait.");
            return;
        }

        List<ChatMessage> current = messages.getValue();
        if (current == null) current = new ArrayList<>();

        // Snapshot history BEFORE adding the new user message so buildFullPrompt
        // doesn't include it twice.
        final List<ChatMessage> historySnapshot = new ArrayList<>(current);

        // Add the user message immediately so it appears in the chat.
        ChatMessage userMsg = new ChatMessage(userMessage, ChatMessage.TYPE_USER);
        current.add(userMsg);
        // Record the index so we can update quality on it later.
        final int userMsgIndex = current.size() - 1;
        messages.setValue(new ArrayList<>(current));
        isThinking.setValue(true);

        executor.execute(() -> {
            String finalResponse;
            String quality;

            // ── Step 1: generate AI counter-argument ──────────────────────
            try {
                String fullPrompt = PromptBuilder.buildFullPrompt(systemPrompt, historySnapshot, userMessage);
                String response   = llmInference.generateResponse(fullPrompt);
                Log.d(TAG, "Step1 raw response length=" + (response != null ? response.length() : "null"));

                if (response != null) {
                    response = response.trim();
                    int userIndex = response.indexOf("\nUser:");
                    if (userIndex != -1) response = response.substring(0, userIndex).trim();
                }
                finalResponse = (response != null && !response.isEmpty())
                        ? response : "I disagree. Please elaborate on your point.";
            } catch (Exception e) {
                Log.e(TAG, "Step1 inference error: " + e.getMessage(), e);
                finalResponse = "I strongly disagree with that point. Care to elaborate?";
            }

            // ── Step 2: rate the user's argument quality ──────────────────
            // Runs in its own try/catch so a failure here never suppresses the
            // AI response, and quality is never null reaching the adapter.
            try {
                String qualityPrompt = PromptBuilder.buildQualityPrompt(topic, userMessage);
                String qualityRaw    = llmInference.generateResponse(qualityPrompt);
                Log.d(TAG, "Step2 qualityRaw='" + qualityRaw + "'");
                quality = parseQuality(qualityRaw);
            } catch (Exception qe) {
                Log.w(TAG, "Step2 quality rating failed: " + qe.getMessage(), qe);
                quality = ChatMessage.QUALITY_MODERATE; // safe fallback — never null
            }
            Log.d(TAG, "Step2 parsed quality=" + quality);

            // ── Post both updates to the main thread ──────────────────────
            final String postedResponse = finalResponse;
            final String postedQuality  = quality;
            mainHandler.post(() -> {
                List<ChatMessage> updated = messages.getValue();
                if (updated == null) updated = new ArrayList<>();

                // Replace the user message with a NEW instance that carries the quality
                // rating via withQuality(). We must NOT call setQuality() on the existing
                // object — it is the same reference that the adapter's old list holds, so
                // DiffUtil would compare it with itself and see no change, skipping the
                // rebind entirely. withQuality() returns a fresh object → DiffUtil sees
                // a difference → adapter redraws the badge.
                if (userMsgIndex < updated.size()) {
                    updated.set(userMsgIndex,
                            updated.get(userMsgIndex).withQuality(postedQuality));
                    Log.d(TAG, "Replaced index=" + userMsgIndex + " with quality=" + postedQuality);
                } else {
                    Log.w(TAG, "userMsgIndex=" + userMsgIndex + " out of bounds, size=" + updated.size());
                }

                updated.add(new ChatMessage(postedResponse, ChatMessage.TYPE_AI));
                messages.setValue(new ArrayList<>(updated));
                isThinking.setValue(false);
            });
        });
    }

    /**
     * Normalises the quality rating returned by the model.
     * Accepts "Strong", "Moderate", "Weak" (case-insensitive).
     * Falls back to Moderate for any unexpected output.
     */
    private String parseQuality(String raw) {
        if (raw == null) return ChatMessage.QUALITY_MODERATE;
        String trimmed   = raw.trim().toLowerCase();
        String firstWord = trimmed.split("\\s+")[0].replaceAll("[^a-z]", "");
        switch (firstWord) {
            case "strong": return ChatMessage.QUALITY_STRONG;
            case "weak":   return ChatMessage.QUALITY_WEAK;
            default:       return ChatMessage.QUALITY_MODERATE;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Analysis
    // ─────────────────────────────────────────────────────────────────────────

    public void generateAnalysis() {
        if (llmInference == null) return;

        List<ChatMessage> current = messages.getValue();
        if (current == null || current.isEmpty()) return;

        isThinking.setValue(true);
        status.setValue("Analysing your debate performance…");

        final List<ChatMessage> snapshot = new ArrayList<>(current);

        executor.execute(() -> {
            try {
                String analysisPrompt = buildAnalysisPrompt(snapshot);
                String response       = llmInference.generateResponse(analysisPrompt);
                int    score          = extractScore(response);

                final String finalAnalysis = (response != null) ? response.trim() : "Analysis unavailable.";
                final int    finalScore    = score;

                mainHandler.post(() -> {
                    analysisResult.setValue(finalAnalysis);
                    debateScore.setValue(finalScore);
                    isThinking.setValue(false);
                    status.setValue("Analysis complete!");
                });

            } catch (Exception e) {
                Log.e(TAG, "Analysis error: " + e.getMessage());
                mainHandler.post(() -> {
                    analysisResult.setValue(null);
                    debateScore.setValue(50);
                    isThinking.setValue(false);
                    status.setValue("Analysis failed. Try again or save without analysis.");
                });
            }
        });
    }

    private String buildAnalysisPrompt(List<ChatMessage> snapshot) {
        StringBuilder convo = new StringBuilder();
        for (ChatMessage msg : snapshot) {
            convo.append(msg.isUser() ? "User: " : "AI: ")
                 .append(msg.getMessage()).append("\n");
        }
        // Uses a filled-in concrete example so the model sees exactly what
        // output is expected — far more reliable than placeholders in brackets.
        return "You are a debate coach. Evaluate only the USER's performance in this debate.\n\n"
                + "Debate topic: " + topic + "\n\n"
                + "Conversation:\n" + convo + "\n\n"
                + "Reply using ONLY these four sections, no other text, no numbering, no markdown:\n\n"
                + "SCORE: 72\n"
                + "STRENGTHS:\n"
                + "- The user supported their claim with a specific example\n"
                + "- The user directly addressed the AI's counter-argument\n"
                + "WEAKNESSES:\n"
                + "- The user relied on anecdote rather than evidence\n"
                + "- The user did not address the strongest counter-argument\n"
                + "VERDICT: The user made some solid points but needs to strengthen their evidence.\n\n"
                + "Now write the actual analysis for the conversation above in exactly that format. "
                + "Replace the example content with your real assessment. Keep each section brief.";
    }

    private int extractScore(String response) {
        if (response == null) return 50;
        Matcher matcher = SCORE_PATTERN.matcher(response);
        if (matcher.find()) {
            String group = matcher.group(1);
            if (group != null) {
                try {
                    return Math.min(100, Math.max(0, Integer.parseInt(group)));
                } catch (NumberFormatException ignored) {}
            }
        }
        return 50;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Persistence
    // ─────────────────────────────────────────────────────────────────────────

    public void saveSession() {
        List<ChatMessage> current = messages.getValue();
        if (current == null || current.isEmpty()) return;

        try {
            JSONArray jsonArray = new JSONArray();
            for (ChatMessage msg : current) {
                JSONObject obj = new JSONObject();
                obj.put("message",   msg.getMessage());
                obj.put("type",      msg.getType());
                obj.put("timestamp", msg.getTimestamp());
                if (msg.getQuality() != null) obj.put("quality", msg.getQuality());
                jsonArray.put(obj);
            }

            String analysis = analysisResult.getValue() != null ? analysisResult.getValue() : "";
            int    score    = debateScore.getValue()    != null ? debateScore.getValue()    : 0;

            DebateSession session = new DebateSession(
                    topic, tone, jsonArray.toString(),
                    System.currentTimeMillis(), current.size(),
                    analysis, score
            );
            repository.insert(session);
        } catch (Exception e) {
            Log.e(TAG, "Save error: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Accessors
    // ─────────────────────────────────────────────────────────────────────────

    public LiveData<List<ChatMessage>> getMessages()       { return messages; }
    public LiveData<String>            getStatus()         { return status; }
    public LiveData<Boolean>           getIsThinking()     { return isThinking; }
    public LiveData<String>            getAnalysisResult() { return analysisResult; }
    public LiveData<Integer>           getDebateScore()    { return debateScore; }
    public String                      getTopic()          { return topic; }
    public String                      getTone()           { return tone; }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (llmInference != null) llmInference.close();
        executor.shutdown();
    }
}
