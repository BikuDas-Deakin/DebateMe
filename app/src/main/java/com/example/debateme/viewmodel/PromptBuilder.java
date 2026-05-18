package com.example.debateme.viewmodel;

import com.example.debateme.models.ChatMessage;

import java.util.List;

public class PromptBuilder {

    public static String buildSystemPrompt(String topic, String tone) {
        String toneInstruction;
        switch (tone.toLowerCase()) {
            case "academic":
                toneInstruction = "Use formal, academic language. Reference logical frameworks, cite hypothetical studies, and use structured argumentation. Be thorough and evidence-based.";
                break;
            case "challenging":
                toneInstruction = "Be assertive, direct, and relentlessly challenging. Push back hard on every point. Use rhetorical questions and expose every weakness in the user's argument.";
                break;
            default: // casual
                toneInstruction = "Be conversational and friendly but still argue firmly. Use everyday language and relatable examples.";
                break;
        }

        return "You are DebateMe AI, a debate partner. The user believes: \"" + topic + "\". " +
                "Your role is to argue the OPPOSITE position firmly and consistently. " +
                "Never agree with the user. Never switch sides. Always counter their points. " +
                toneInstruction + " " +
                "Keep responses under 150 words. Be sharp and focused. " +
                "Do not mention you are an AI or that you are playing a role. Just debate.";
    }

    public static String buildFullPrompt(String systemPrompt, List<ChatMessage> history, String newUserMessage) {
        StringBuilder sb = new StringBuilder();
        sb.append(systemPrompt).append("\n\n");

        // Add conversation history
        for (ChatMessage msg : history) {
            if (msg.isUser()) {
                sb.append("User: ").append(msg.getMessage()).append("\n");
            } else {
                sb.append("DebateMe AI: ").append(msg.getMessage()).append("\n");
            }
        }

        // Add new user message
        sb.append("User: ").append(newUserMessage).append("\n");
        sb.append("DebateMe AI:");

        return sb.toString();
    }
}
