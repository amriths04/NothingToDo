package com.example.nothingtasks;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ListDetailsActivity extends AppCompatActivity {

    private int listId;
    private String listName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_list_details);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.listDetailRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get list ID and name from Intent
        listId = getIntent().getIntExtra("listId", -1);
        listName = getIntent().getStringExtra("listName");

        if (listId == -1 || listName == null) {
            Toast.makeText(this, "Invalid list data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView title = findViewById(R.id.listTitle);
        title.setText(listName);

        // Back button closes activity
        ImageButton back = findViewById(R.id.backButton);
        back.setOnClickListener(v -> finish());

        // Delete list button with DB delete logic
        ImageButton delete = findViewById(R.id.deleteListButton);
        delete.setOnClickListener(v -> {
            new Thread(() -> {
                TaskDatabase db = TaskDatabase.getInstance(getApplicationContext());
                db.taskListDao().deleteListById(listId);

                runOnUiThread(() -> {
                    Toast.makeText(this, "List deleted", Toast.LENGTH_SHORT).show();
                    finish(); // close activity and return to previous screen
                });
            }).start();
        });

        // Add Reminder button (placeholder)
        findViewById(R.id.addReminderBtn).setOnClickListener(v -> {
            Toast.makeText(this, "Add reminder in " + listName, Toast.LENGTH_SHORT).show();
            // TODO: Show dialog to add reminder
        });

        // Filter button (placeholder)
        findViewById(R.id.filterButton).setOnClickListener(v -> {
            Toast.makeText(this, "Filter coming soon", Toast.LENGTH_SHORT).show();
        });
    }
}
