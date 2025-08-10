package com.example.nothingtasks.ui.activites;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nothingtasks.R;
import com.example.nothingtasks.ui.adapters.TaskListAdapter;
import com.example.nothingtasks.data.db.ReminderDao;
import com.example.nothingtasks.data.db.TaskDatabase;
import com.example.nothingtasks.data.model.Reminder;
import com.example.nothingtasks.data.model.TaskList;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private TaskListAdapter adapter;
    private TaskDatabase db;

    private TextView todayCount, scheduledCount, allCount, flaggedCount;

    // ✅ Handler for auto-updating scheduled count
    private final Handler handler = new Handler();
    private Runnable scheduledUpdaterRunnable;

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

        db = TaskDatabase.getInstance(getApplicationContext());

        // --- Grid views ---
        View todayGrid = findViewById(R.id.todayGrid);
        ImageView todayIcon = todayGrid.findViewById(R.id.gridIcon);
        TextView todayTitle = todayGrid.findViewById(R.id.gridTitle);
        todayCount = todayGrid.findViewById(R.id.gridCount);

        View scheduledGrid = findViewById(R.id.scheduledGrid);
        ImageView scheduledIcon = scheduledGrid.findViewById(R.id.gridIcon);
        TextView scheduledTitle = scheduledGrid.findViewById(R.id.gridTitle);
        scheduledCount = scheduledGrid.findViewById(R.id.gridCount);

        View allGrid = findViewById(R.id.allGrid);
        ImageView allIcon = allGrid.findViewById(R.id.gridIcon);
        TextView allTitle = allGrid.findViewById(R.id.gridTitle);
        allCount = allGrid.findViewById(R.id.gridCount);

        View flaggedGrid = findViewById(R.id.flaggedGrid);
        ImageView flaggedIcon = flaggedGrid.findViewById(R.id.gridIcon);
        TextView flaggedTitle = flaggedGrid.findViewById(R.id.gridTitle);
        flaggedCount = flaggedGrid.findViewById(R.id.gridCount);

        todayIcon.setImageResource(R.drawable.today);
        todayTitle.setText("Today");

        scheduledIcon.setImageResource(R.drawable.calendar);
        scheduledTitle.setText("Scheduled");

        allIcon.setImageResource(R.drawable.all);
        allTitle.setText("All");

        flaggedIcon.setImageResource(R.drawable.bb);
        flaggedTitle.setText("Flagged");

        todayGrid.setOnClickListener(v -> openGridActivity(GridActivity.FILTER_TODAY));
        scheduledGrid.setOnClickListener(v -> openGridActivity(GridActivity.FILTER_SCHEDULED));
        allGrid.setOnClickListener(v -> openGridActivity(GridActivity.FILTER_ALL));
        flaggedGrid.setOnClickListener(v -> openGridActivity(GridActivity.FILTER_FLAGGED));

        // Settings
        ImageButton settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, SettingsActivity.class))
        );

        // Add List
        ImageButton addListButton = findViewById(R.id.addListButton);
        addListButton.setOnClickListener(v -> showAddListDialog());

        // Add Reminder
        TextView newReminderText = findViewById(R.id.newReminderText);
        newReminderText.setOnClickListener(v -> {
            AddReminderDialog.show(MainActivity.this, (title, desc, dateTime, repeat) -> {
                Reminder reminder = new Reminder(title, desc, false, false, dateTime, null, repeat);
                new Thread(() -> {
                    db.reminderDao().insert(reminder);
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this, "Reminder added", Toast.LENGTH_SHORT).show());
                }).start();
            });
        });

        // Apply Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Setup RecyclerView
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

        // Observe task lists
        db.taskListDao().getAllLists().observe(this, taskLists -> {
            if (taskLists == null) return;
            db.reminderDao().getAllReminders().observe(this, reminders -> {
                if (reminders == null) return;
                Map<Integer, Integer> reminderCounts = new HashMap<>();
                for (Reminder r : reminders) {
                    Integer listId = r.getListId();
                    if (listId != null) {
                        reminderCounts.put(listId, reminderCounts.getOrDefault(listId, 0) + 1);
                    }
                }
                for (TaskList list : taskLists) {
                    list.setReminderCount(reminderCounts.getOrDefault(list.getId(), 0));
                }
                adapter.setTaskLists(taskLists);
            });
        });

        // Observe counts
        observeGridCounts();
    }

    private void openGridActivity(int filterType) {
        Intent intent = new Intent(MainActivity.this, GridActivity.class);
        intent.putExtra("filterType", filterType);
        startActivity(intent);
    }

    private void observeGridCounts() {
        ReminderDao dao = db.reminderDao();

        dao.getAllReminders().observe(this, reminders ->
                allCount.setText(String.valueOf(reminders.size()))
        );

        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        dao.getRemindersForToday(todayDate).observe(this, reminders ->
                todayCount.setText(String.valueOf(reminders.size()))
        );

        // ✅ Periodically update scheduled count every 60 seconds
        scheduledUpdaterRunnable = new Runnable() {
            @Override
            public void run() {
                String nowDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
                db.reminderDao().getScheduledReminders(nowDateTime).observe(MainActivity.this, reminders ->
                        scheduledCount.setText(String.valueOf(reminders.size()))
                );
                handler.postDelayed(this, 5000); // Run again in 60 seconds
            }
        };
        handler.post(scheduledUpdaterRunnable); // Start it

        dao.getFlaggedReminders().observe(this, reminders ->
                flaggedCount.setText(String.valueOf(reminders.size()))
        );
    }
    private void showAddListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_list, null);

        EditText inputName = view.findViewById(R.id.listNameInput);
        EditText inputDesc = view.findViewById(R.id.listDescInput);
        TextView cancelBtn = view.findViewById(R.id.cancelBtn);
        TextView addBtn = view.findViewById(R.id.addBtn);

        InputFilter lengthFilter = new InputFilter() {
            private final int MAX_LENGTH = 30;

            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                int newLength = dest.length() + (end - start) - (dend - dstart);

                if (newLength > MAX_LENGTH) {
                    inputName.setError("Maximum 30 characters allowed");
                    return ""; // block input
                } else {
                    inputName.setError(null); // clear error if under limit
                    return null; // accept input
                }
            }
        };

        inputName.setFilters(new InputFilter[] { lengthFilter });

        AlertDialog dialog = builder.setView(view).create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        addBtn.setOnClickListener(v -> {
            String name = inputName.getText().toString().trim();
            String desc = inputDesc.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                inputName.setError("List name is required");
                return;
            }

            TaskList newList = new TaskList(name, desc);

            new Thread(() -> {
                long id = db.taskListDao().insert(newList); // updated method name
                newList.setId((int) id);
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "List created", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                });
            }).start();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (scheduledUpdaterRunnable != null) {
            handler.removeCallbacks(scheduledUpdaterRunnable);
        }
    }
}
