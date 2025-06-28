package com.example.nothingtasks;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Canvas;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GridActivity extends AppCompatActivity {

    public static final int FILTER_ALL = 0;
    public static final int FILTER_TODAY = 1;
    public static final int FILTER_FLAGGED = 2;
    public static final int FILTER_SCHEDULED = 3;

    private ReminderAdapter reminderAdapter;
    private int filterType;
    private String title;

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

        // Get filter type from Intent (default ALL)
        filterType = getIntent().getIntExtra("filterType", FILTER_ALL);

        // Determine title and which LiveData to observe
        TaskDatabase db = TaskDatabase.getInstance(this);
        ReminderDao reminderDao = db.reminderDao();

        switch (filterType) {
            case FILTER_ALL:
                title = "All";
                break;
            case FILTER_TODAY:
                title = "Today";
                break;
            case FILTER_FLAGGED:
                title = "Flagged";
                break;
            case FILTER_SCHEDULED:
                title = "Scheduled";
                break;
            default:
                title = "All";
        }

        TextView titleText = findViewById(R.id.listTitle);
        titleText.setText(title);

        // Hide delete list button for filtered views
        ImageButton deleteButton = findViewById(R.id.deleteListButton);
        deleteButton.setVisibility(View.GONE);

        // Back button closes activity
        ImageButton back = findViewById(R.id.backButton);
        back.setOnClickListener(v -> finish());

        // Setup RecyclerView and Adapter
        RecyclerView recyclerView = findViewById(R.id.remindersRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        reminderAdapter = new ReminderAdapter(reminderDao);
        recyclerView.setAdapter(reminderAdapter);

        // Observe filtered reminders based on filterType
        switch (filterType) {
            case FILTER_ALL:
                reminderDao.getAllReminders().observe(this, reminders -> {
                    reminderAdapter.setReminders(reminders);
                });
                break;
            case FILTER_TODAY:
                String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                reminderDao.getRemindersForToday(today).observe(this, reminders -> {
                    reminderAdapter.setReminders(reminders);
                });

                break;
            case FILTER_FLAGGED:
                reminderDao.getFlaggedReminders().observe(this, reminders -> {
                    reminderAdapter.setReminders(reminders);
                });
                break;
            case FILTER_SCHEDULED:
                String nowDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
                reminderDao.getScheduledReminders(nowDateTime).observe(this, reminders -> {
                    reminderAdapter.setReminders(reminders);
                });
                break;
            default:
                reminderDao.getAllReminders().observe(this, reminders -> {
                    reminderAdapter.setReminders(reminders);
                });
        }

        // Swipe to delete support
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView,
                                          @NonNull RecyclerView.ViewHolder viewHolder,
                                          @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        int position = viewHolder.getAdapterPosition();
                        Reminder reminderToDelete = reminderAdapter.getReminderAt(position);
                        new Thread(() -> reminderDao.delete(reminderToDelete)).start();
                    }

                    @Override
                    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                            @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                            int actionState, boolean isCurrentlyActive) {

                        // Background color
                        Paint paint = new Paint();
                        paint.setColor(Color.parseColor("#F44336")); // red

                        View itemView = viewHolder.itemView;
                        c.drawRect(itemView.getLeft(), itemView.getTop(),
                                itemView.getLeft() + dX, itemView.getBottom(), paint);

                        // Draw trash icon
                        Drawable icon = ContextCompat.getDrawable(GridActivity.this, android.R.drawable.ic_menu_delete);
                        int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                        int iconTop = itemView.getTop() + iconMargin;
                        int iconLeft = itemView.getLeft() + 32;
                        int iconRight = iconLeft + icon.getIntrinsicWidth();
                        int iconBottom = iconTop + icon.getIntrinsicHeight();

                        if (dX > 0) { // swiping right
                            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                            icon.draw(c);
                        }

                        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                    }
                }
        );
        itemTouchHelper.attachToRecyclerView(recyclerView);

        // Add Reminder button
        findViewById(R.id.addReminderBtn).setOnClickListener(v -> {
            AddReminderDialog.show(this, (reminderTitle, desc, date, repeat) -> {
                // For filtered views, new reminders get no list (listId = null)
                Reminder reminder = new Reminder(reminderTitle, desc, false, false, date, null, repeat);
                new Thread(() -> reminderDao.insert(reminder)).start();
            });
        });

        // Filter button (stub)
        findViewById(R.id.filterButton).setOnClickListener(v -> {
            Toast.makeText(this, "Filter coming soon", Toast.LENGTH_SHORT).show();
        });
    }
}
