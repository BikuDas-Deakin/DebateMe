package com.example.debateme.viewmodel;

import com.example.debateme.models.ChatMessage;

import java.util.List;

public class PromptBuilder {

    public static String buildSystemPrompt(String topic, String tone) {
        String toneInstruction;
        switch (tone.toLowerCase()) {
            case "academic":
                toneInstruction = "Use formal, academic language. Reference logical frameworks, "
                        + "cite hypothetical studies, and use structured argumentation. "
                        + "Be thorough and evidence-based.";
                break;
            case "challenging":
                toneInstruction = "Be assertive, direct, and relentlessly challenging. "
                        + "Push back hard on every point. Use rhetorical questions and "
                        + "expose every weakness in the user's argument.";
                break;
            default: // casual
                toneInstruction = "Be conversational and friendly but still argue firmly. "
                        + "Use everyday language and relatable examples.";
                break;
        }

        return "You are DebateMe AI, a debate partner. The user believes: \"" + topic + "\". "
                + "Your role is to argue the OPPOSITE position firmly and consistently. "
                + "Never agree with the user. Never switch sides. Always counter their points. "
                + toneInstruction + " "
                + "Keep responses under 150 words. Be sharp and focused. "
                + "Do not mention you are an AI or that you are playing a role. Just debate.";
    }

    public static String buildFullPrompt(String systemPrompt,
                                         List<ChatMessage> history,
                                         String newUserMessage) {
        StringBuilder sb = new StringBuilder();
        sb.append(systemPrompt).append("\n\n");

        for (ChatMessage msg : history) {
            sb.append(msg.isUser() ? "User: " : "DebateMe AI: ")
                    .append(msg.getMessage()).append("\n");
        }

        sb.append("User: ").append(newUserMessage).append("\n");
        sb.append("DebateMe AI:");
        return sb.toString();
    }

    /**
     * Asks the model to rate a single user argument as Strong, Moderate, or Weak.
     * The prompt is intentionally tiny — we only need one word back.
     *
     * @param topic         the debate topic / user's original claim
     * @param userArgument  the specific message to rate
     * @return a compact prompt whose response should be exactly one of:
     *         "Strong", "Moderate", or "Weak"
     */
    public static String buildQualityPrompt(String topic, String userArgument) {
        // Few-shot examples teach the model what each rating looks like in
        // practice. Small models like Llama 3.2 3B classify far more accurately
        // from concrete examples than from abstract descriptions or option lists.
        return "Classify debate argument quality as exactly one of: Strong, Moderate, or Weak.\n\n"
                + "Examples:\n"
                + "Argument: \"Everyone knows social media is bad.\"\n"
                + "Quality: Weak\n\n"
                + "Argument: \"Social media can be harmful, especially for younger users.\"\n"
                + "Quality: Moderate\n\n"
                + "Argument: \"A 2023 APA study found teens using social media over 3 hours daily were 60% more likely to report anxiety symptoms.\"\n"
                + "Quality: Strong\n\n"
                + "Argument: \"It just makes people feel bad about themselves.\"\n"
                + "Quality: Weak\n\n"
                + "Argument: \"Research consistently links heavy social media use to poor sleep and lower self-esteem in adolescents.\"\n"
                + "Quality: Strong\n\n"
                + "Now classify this argument about \"" + topic + "\":\n"
                + "Argument: \"" + userArgument + "\"\n"
                + "Quality:";
    }
}