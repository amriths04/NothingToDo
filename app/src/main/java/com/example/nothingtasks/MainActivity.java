package com.example.nothingtasks;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TaskListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // --- Grid views ---
        View todayGrid = findViewById(R.id.todayGrid);
        ImageView todayIcon = todayGrid.findViewById(R.id.gridIcon);
        TextView todayTitle = todayGrid.findViewById(R.id.gridTitle);
        TextView todayCount = todayGrid.findViewById(R.id.gridCount);

        View scheduledGrid = findViewById(R.id.scheduledGrid);
        ImageView scheduledIcon = scheduledGrid.findViewById(R.id.gridIcon);
        TextView scheduledTitle = scheduledGrid.findViewById(R.id.gridTitle);
        TextView scheduledCount = scheduledGrid.findViewById(R.id.gridCount);

        View allGrid = findViewById(R.id.allGrid);
        ImageView allIcon = allGrid.findViewById(R.id.gridIcon);
        TextView allTitle = allGrid.findViewById(R.id.gridTitle);
        TextView allCount = allGrid.findViewById(R.id.gridCount);

        View flaggedGrid = findViewById(R.id.flaggedGrid);
        ImageView flaggedIcon = flaggedGrid.findViewById(R.id.gridIcon);
        TextView flaggedTitle = flaggedGrid.findViewById(R.id.gridTitle);
        TextView flaggedCount = flaggedGrid.findViewById(R.id.gridCount);

        // Set icons and titles once
        todayIcon.setImageResource(R.drawable.today);
        todayTitle.setText("Today");

        scheduledIcon.setImageResource(R.drawable.calendar);
        scheduledTitle.setText("Scheduled");

        allIcon.setImageResource(R.drawable.all);
        allTitle.setText("All");

        flaggedIcon.setImageResource(R.drawable.flagged);
        flaggedTitle.setText("Flagged");
        flaggedIcon.setColorFilter(Color.RED); // flagged in red

        // Setup buttons and clicks for grids (your existing code)
        todayGrid.setOnClickListener(v -> openGridActivity(GridActivity.FILTER_TODAY));
        scheduledGrid.setOnClickListener(v -> openGridActivity(GridActivity.FILTER_SCHEDULED));
        allGrid.setOnClickListener(v -> openGridActivity(GridActivity.FILTER_ALL));
        flaggedGrid.setOnClickListener(v -> openGridActivity(GridActivity.FILTER_FLAGGED));

        // Settings button
        ImageButton settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        });

        // "+" Add List button
        ImageButton addListButton = findViewById(R.id.addListButton);
        addListButton.setOnClickListener(v -> showAddListDialog());

        // "+ New Reminder" from bottom bar
        TextView newReminderText = findViewById(R.id.newReminderText);
        newReminderText.setOnClickListener(v -> {
            AddReminderDialog.show(MainActivity.this, (title, desc, dateTime, repeat) -> {
                Reminder reminder = new Reminder(title, desc, false, false, dateTime, null, repeat);
                new Thread(() -> {
                    TaskDatabase db = TaskDatabase.getInstance(getApplicationContext());
                    db.reminderDao().insert(reminder);

                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Reminder added", Toast.LENGTH_SHORT).show();
                        updateGridCounts();  // update counts after adding reminder
                    });
                }).start();
            });

        });

        // Apply edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Setup RecyclerView for task lists
        RecyclerView recyclerView = findViewById(R.id.myListsRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TaskListAdapter(list -> {
            if (list != null) {
                Intent intent = new Intent(MainActivity.this, ListDetailsActivity.class);
                intent.putExtra("listId", list.getId());
                intent.putExtra("listName", list.getName());
                startActivity(intent);
            } else {
                Toast.makeText(this, "List is null", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setAdapter(adapter);

        TaskDatabase db = TaskDatabase.getInstance(getApplicationContext());
        db.taskListDao().getAllLists().observe(this, new Observer<List<TaskList>>() {
            @Override
            public void onChanged(List<TaskList> taskLists) {
                adapter.setTaskLists(taskLists);
            }
        });

        // Initial counts update
        updateGridCounts();
    }

    private void openGridActivity(int filterType) {
        Intent intent = new Intent(MainActivity.this, GridActivity.class);
        intent.putExtra("filterType", filterType);
        startActivity(intent);
    }


    private void updateGridCounts() {
        TaskDatabase db = TaskDatabase.getInstance(getApplicationContext());

        // Find views again (or make them class variables)
        View todayGrid = findViewById(R.id.todayGrid);
        TextView todayCount = todayGrid.findViewById(R.id.gridCount);

        View scheduledGrid = findViewById(R.id.scheduledGrid);
        TextView scheduledCount = scheduledGrid.findViewById(R.id.gridCount);

        View allGrid = findViewById(R.id.allGrid);
        TextView allCount = allGrid.findViewById(R.id.gridCount);

        View flaggedGrid = findViewById(R.id.flaggedGrid);
        TextView flaggedCount = flaggedGrid.findViewById(R.id.gridCount);

        // Get today's date string in your app's date format (assumed yyyy-MM-dd here)
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        new Thread(() -> {
            int countAll = db.reminderDao().getCountAll();
            int countToday = db.reminderDao().getCountToday(todayDate);
            int countScheduled = db.reminderDao().getCountScheduled(todayDate);
            int countFlagged = db.reminderDao().getCountFlagged();

            runOnUiThread(() -> {
                allCount.setText(String.valueOf(countAll));
                todayCount.setText(String.valueOf(countToday));
                scheduledCount.setText(String.valueOf(countScheduled));
                flaggedCount.setText(String.valueOf(countFlagged));
            });
        }).start();
    }

    private void showAddListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New List");

        EditText inputName = new EditText(this);
        inputName.setHint("List name");
        inputName.setPadding(40, 30, 40, 10);
        inputName.setTextSize(16f);

        EditText inputDesc = new EditText(this);
        inputDesc.setHint("Optional description");
        inputDesc.setPadding(40, 10, 40, 30);
        inputDesc.setTextSize(14f);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.addView(inputName);
        container.addView(inputDesc);
        builder.setView(container);
        builder.setPositiveButton("Create", (dialog, which) -> {
            String name = inputName.getText().toString().trim();
            String desc = inputDesc.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                Toast.makeText(this, "List name can't be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            int color = 0xFF2196F3; // static blue
            TaskList newList = new TaskList(name, desc, color);

            new Thread(() -> {
                TaskDatabase db = TaskDatabase.getInstance(getApplicationContext());
                long id = db.taskListDao().insertAndReturnId(newList);
                newList.setId((int) id);

                runOnUiThread(() -> {
                    Toast.makeText(this, "List created", Toast.LENGTH_SHORT).show();
                });
            }).start();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}
