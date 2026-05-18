package com.example.debateme.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.debateme.R;
import com.example.debateme.models.DebateSession;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<DebateSession> sessions = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault());

    public void setSessions(List<DebateSession> sessions) {
        this.sessions = sessions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        DebateSession session = sessions.get(position);
        holder.bind(session);
    }

    @Override
    public int getItemCount() { return sessions.size(); }

    class HistoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvToneBadge;
        private final TextView tvDate;
        private final TextView tvTopic;
        private final TextView tvMessageCount;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvToneBadge = itemView.findViewById(R.id.tvToneBadge);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTopic = itemView.findViewById(R.id.tvTopic);
            tvMessageCount = itemView.findViewById(R.id.tvMessageCount);
        }

        public void bind(DebateSession session) {
            tvToneBadge.setText(session.getTone());
            tvDate.setText(dateFormat.format(new Date(session.getTimestamp())));
            tvTopic.setText(session.getTopic());
            tvMessageCount.setText(session.getMessageCount() + " messages");
        }
    }
}
