package com.example.nothingtasks;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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

import java.util.List;

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

        // Settings
        ImageButton settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        });

        // "+" Add List
        ImageButton addListButton = findViewById(R.id.addListButton);
        addListButton.setOnClickListener(v -> showAddListDialog());

        // Apply edge insets
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

        TaskDatabase db = TaskDatabase.getInstance(getApplicationContext());
        db.taskListDao().getAllLists().observe(this, new Observer<List<TaskList>>() {
            @Override
            public void onChanged(List<TaskList> taskLists) {
                adapter.setTaskLists(taskLists);
            }
        });
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

                // ✅ Get inserted ID
                long id = db.taskListDao().insertAndReturnId(newList);
                newList.setId((int) id); // Important if you'll use this object later

                runOnUiThread(() -> {
                    Toast.makeText(this, "List created", Toast.LENGTH_SHORT).show();
                });
            }).start();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}
