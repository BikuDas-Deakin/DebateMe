package com.example.debateme.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "debate_sessions")
public class DebateSession {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String topic;
    private String tone;
    private String messagesJson;
    private long timestamp;
    private int messageCount;
    private String analysisResult;
    private int score; // 0-100

    public DebateSession(String topic, String tone, String messagesJson, long timestamp, int messageCount, String analysisResult, int score) {
        this.topic = topic;
        this.tone = tone;
        this.messagesJson = messagesJson;
        this.timestamp = timestamp;
        this.messageCount = messageCount;
        this.analysisResult = analysisResult;
        this.score = score;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTopic() { return topic; }
    public String getTone() { return tone; }
    public String getMessagesJson() { return messagesJson; }
    public long getTimestamp() { return timestamp; }
    public int getMessageCount() { return messageCount; }
    public String getAnalysisResult() { return analysisResult; }
    public int getScore() { return score; }
}