package com.example.debateme.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.debateme.R;
import com.example.debateme.activities.HistoryDetailActivity;
import com.example.debateme.models.DebateSession;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<DebateSession> sessions = new ArrayList<>();
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault());

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
        holder.bind(sessions.get(position));
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
            tvToneBadge    = itemView.findViewById(R.id.tvToneBadge);
            tvDate         = itemView.findViewById(R.id.tvDate);
            tvTopic        = itemView.findViewById(R.id.tvTopic);
            tvMessageCount = itemView.findViewById(R.id.tvMessageCount);
        }

        public void bind(DebateSession session) {
            tvToneBadge.setText(session.getTone());
            tvDate.setText(dateFormat.format(new Date(session.getTimestamp())));
            tvTopic.setText(session.getTopic());

            String scoreText = session.getScore() > 0
                    ? session.getMessageCount() + " messages · " + session.getScore() + "/100"
                    : session.getMessageCount() + " messages";
            tvMessageCount.setText(scoreText);

            // FIX 6: Tapping a history row opens HistoryDetailActivity with
            // the session id so the user can review the conversation & analysis.
            itemView.setOnClickListener(v -> {
                Context ctx = itemView.getContext();
                Intent intent = new Intent(ctx, HistoryDetailActivity.class);
                intent.putExtra(HistoryDetailActivity.EXTRA_SESSION_ID, session.getId());
                ctx.startActivity(intent);
            });
        }
    }
}