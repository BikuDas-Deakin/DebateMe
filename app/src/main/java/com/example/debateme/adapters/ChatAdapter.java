package com.example.debateme.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.debateme.R;
import com.example.debateme.models.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private static final String TAG = "ChatAdapter";
    private List<ChatMessage> messages = new ArrayList<>();

    public void setMessages(List<ChatMessage> newMessages) {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return messages.size(); }
            @Override public int getNewListSize() { return newMessages.size(); }

            @Override
            public boolean areItemsTheSame(int oldPos, int newPos) {
                ChatMessage o = messages.get(oldPos);
                ChatMessage n = newMessages.get(newPos);
                return o.getTimestamp() == n.getTimestamp() && o.getType() == n.getType();
            }

            @Override
            public boolean areContentsTheSame(int oldPos, int newPos) {
                ChatMessage o = messages.get(oldPos);
                ChatMessage n = newMessages.get(newPos);
                boolean sameText    = o.getMessage().equals(n.getMessage());
                String  oq          = o.getQuality(), nq = n.getQuality();
                boolean sameQuality = (oq == null && nq == null)
                        || (oq != null && oq.equals(nq));
                return sameText && sameQuality;
            }
        });
        messages = new ArrayList<>(newMessages);
        result.dispatchUpdatesTo(this);
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        holder.bind(messages.get(position));
    }

    @Override
    public int getItemCount() { return messages.size(); }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvUserSender;
        private final TextView tvUserMessage;
        private final TextView tvQualityBadge;
        private final TextView tvAiSender;
        private final TextView tvAiMessage;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserSender   = itemView.findViewById(R.id.tvUserSender);
            tvUserMessage  = itemView.findViewById(R.id.tvUserMessage);
            tvQualityBadge = itemView.findViewById(R.id.tvQualityBadge);
            tvAiSender     = itemView.findViewById(R.id.tvAiSender);
            tvAiMessage    = itemView.findViewById(R.id.tvAiMessage);
        }

        public void bind(ChatMessage message) {
            if (message.isUser()) {
                tvUserSender.setVisibility(View.VISIBLE);
                tvUserMessage.setVisibility(View.VISIBLE);
                tvUserMessage.setText(message.getMessage());
                tvAiSender.setVisibility(View.GONE);
                tvAiMessage.setVisibility(View.GONE);

                String quality = message.getQuality();
                Log.d(TAG, "Binding user message, quality=" + quality);

                if (quality != null && !quality.isEmpty()) {
                    tvQualityBadge.setVisibility(View.VISIBLE);

                    // Use a dedicated drawable per quality level instead of
                    // runtime tinting — avoids RecyclerView recycling/mutation issues.
                    int badgeDrawable;
                    String badgeText;
                    switch (quality) {
                        case ChatMessage.QUALITY_STRONG:
                            badgeText     = "💪 Strong Argument";
                            badgeDrawable = R.drawable.bg_badge_strong;
                            break;
                        case ChatMessage.QUALITY_WEAK:
                            badgeText     = "⚠️ Weak Argument";
                            badgeDrawable = R.drawable.bg_badge_weak;
                            break;
                        default: // MODERATE
                            badgeText     = "👍 Moderate Argument";
                            badgeDrawable = R.drawable.bg_badge_moderate;
                            break;
                    }
                    tvQualityBadge.setText(badgeText);
                    tvQualityBadge.setBackground(
                            ContextCompat.getDrawable(itemView.getContext(), badgeDrawable));

                } else {
                    tvQualityBadge.setVisibility(View.GONE);
                }

            } else {
                tvAiSender.setVisibility(View.VISIBLE);
                tvAiMessage.setVisibility(View.VISIBLE);
                tvAiMessage.setText(message.getMessage());
                tvUserSender.setVisibility(View.GONE);
                tvUserMessage.setVisibility(View.GONE);
                tvQualityBadge.setVisibility(View.GONE);
            }
        }
    }
}
