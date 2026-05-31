package com.example.debateme.models;

public class ChatMessage {
    public static final int TYPE_USER = 0;
    public static final int TYPE_AI = 1;

    // Quality badge values for user messages
    public static final String QUALITY_STRONG   = "Strong";
    public static final String QUALITY_MODERATE = "Moderate";
    public static final String QUALITY_WEAK     = "Weak";

    private String message;
    private int type;
    private long timestamp;
    // Populated after the AI responds; null until rated, and always null for AI messages.
    private String quality;

    public ChatMessage(String message, int type) {
        this.message   = message;
        this.type      = type;
        this.timestamp = System.currentTimeMillis();
        this.quality   = null;
    }

    public String getMessage()  { return message; }
    public int    getType()     { return type; }
    public long   getTimestamp(){ return timestamp; }
    public boolean isUser()     { return type == TYPE_USER; }

    public String getQuality()            { return quality; }
    public void   setQuality(String q)    { this.quality = q; }

    /**
     * Returns a new ChatMessage identical to this one except for quality.
     * Use this instead of setQuality() when the message object is already
     * referenced by the adapter's old list — mutating it in-place means
     * DiffUtil compares the same reference for both old and new entries,
     * finds no difference, and skips the rebind entirely.
     */
    public ChatMessage withQuality(String q) {
        ChatMessage copy = new ChatMessage(this.message, this.type);
        copy.timestamp = this.timestamp;  // preserve original timestamp for DiffUtil identity check
        copy.quality   = q;
        return copy;
    }
}
