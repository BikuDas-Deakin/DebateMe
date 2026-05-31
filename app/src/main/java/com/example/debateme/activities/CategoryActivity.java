package com.example.debateme.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.debateme.R;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryActivity extends AppCompatActivity {

    // FIX 3: Key used to pass the selected tone from HomeActivity.
    public static final String EXTRA_TONE = "tone";

    private static final Map<String, List<String>> CATEGORIES = new HashMap<String, List<String>>() {{
        put("🌍 Politics & Society", Arrays.asList(
                "Social media does more harm than good",
                "Universal basic income should be implemented",
                "Voting should be mandatory",
                "Borders should be open to all immigrants",
                "Surveillance cameras improve public safety"
        ));
        put("🎓 Education", Arrays.asList(
                "University degrees are no longer worth it",
                "Homework should be abolished",
                "AI should be allowed in exams",
                "School uniforms should be mandatory",
                "Online education is better than traditional schooling"
        ));
        put("💻 Technology", Arrays.asList(
                "Artificial intelligence will destroy more jobs than it creates",
                "Social media companies should be regulated like utilities",
                "Smartphones have made us less social",
                "Cryptocurrency is the future of money",
                "Screen time limits should be legally enforced"
        ));
        put("🌱 Ethics & Environment", Arrays.asList(
                "Everyone should adopt a vegan diet",
                "Nuclear energy is the solution to climate change",
                "Animals should have the same rights as humans",
                "Individual action cannot solve climate change",
                "Genetic engineering of humans should be permitted"
        ));
        put("💼 Work & Lifestyle", Arrays.asList(
                "Remote work is more productive than office work",
                "A four-day work week should be standard",
                "Entrepreneurship is better than employment",
                "Money is the most important factor in career choice",
                "Work-life balance is a myth"
        ));
    }};

    // FIX 3: Retain the tone chosen on HomeActivity so topic launches honour it.
    private String selectedTone = "Casual";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        // FIX 3: Read the tone that HomeActivity put in the intent.
        String intentTone = getIntent().getStringExtra(EXTRA_TONE);
        if (intentTone != null && !intentTone.isEmpty()) {
            selectedTone = intentTone;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Browse Topics");
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerViewCategories);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new CategoryAdapter());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }

    class CategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_HEADER = 0;
        private static final int TYPE_ITEM = 1;

        private final List<Object> items;

        CategoryAdapter() {
            items = new java.util.ArrayList<>();
            for (Map.Entry<String, List<String>> entry : CATEGORIES.entrySet()) {
                items.add(entry.getKey());
                items.addAll(entry.getValue());
            }
        }

        @Override
        public int getItemViewType(int position) {
            return items.get(position) instanceof String &&
                    CATEGORIES.containsKey(items.get(position)) ? TYPE_HEADER : TYPE_ITEM;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(
                    viewType == TYPE_HEADER ? R.layout.item_category_header : R.layout.item_category_topic,
                    parent, false);
            return new RecyclerView.ViewHolder(view) {};
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            String text = (String) items.get(position);
            TextView tv = holder.itemView.findViewById(R.id.tvText);
            tv.setText(text);

            if (getItemViewType(position) == TYPE_ITEM) {
                holder.itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(CategoryActivity.this, DebateActivity.class);
                    intent.putExtra("topic", text);
                    // FIX 3: Use the tone passed from HomeActivity, not a hardcoded "Casual".
                    intent.putExtra("tone", selectedTone);
                    startActivity(intent);
                });
            }
        }

        @Override
        public int getItemCount() { return items.size(); }
    }
}