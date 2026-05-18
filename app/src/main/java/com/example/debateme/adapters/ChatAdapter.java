package com.example.debateme.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.debateme.R;
import com.example.debateme.models.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<ChatMessage> messages = new ArrayList<>();

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
        notifyDataSetChanged();
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
        ChatMessage message = messages.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() { return messages.size(); }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvUserSender;
        private final TextView tvUserMessage;
        private final TextView tvAiSender;
        private final TextView tvAiMessage;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserSender = itemView.findViewById(R.id.tvUserSender);
            tvUserMessage = itemView.findViewById(R.id.tvUserMessage);
            tvAiSender = itemView.findViewById(R.id.tvAiSender);
            tvAiMessage = itemView.findViewById(R.id.tvAiMessage);
        }

        public void bind(ChatMessage message) {
            if (message.isUser()) {
                tvUserSender.setVisibility(View.VISIBLE);
                tvUserMessage.setVisibility(View.VISIBLE);
                tvUserMessage.setText(message.getMessage());
                tvAiSender.setVisibility(View.GONE);
                tvAiMessage.setVisibility(View.GONE);
            } else {
                tvAiSender.setVisibility(View.VISIBLE);
                tvAiMessage.setVisibility(View.VISIBLE);
                tvAiMessage.setText(message.getMessage());
                tvUserSender.setVisibility(View.GONE);
                tvUserMessage.setVisibility(View.GONE);
            }
        }
    }
}
