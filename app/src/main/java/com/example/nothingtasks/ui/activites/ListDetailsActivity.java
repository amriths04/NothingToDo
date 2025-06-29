package com.example.nothingtasks.ui.activites;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
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

import com.example.nothingtasks.R;
import com.example.nothingtasks.ui.adapters.ReminderAdapter;
import com.example.nothingtasks.data.db.ReminderDao;
import com.example.nothingtasks.data.db.TaskDatabase;
import com.example.nothingtasks.data.model.Reminder;
import com.google.android.material.snackbar.Snackbar;

public class ListDetailsActivity extends AppCompatActivity {

    private int listId;
    private String listName;
    private ReminderAdapter reminderAdapter;

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
        View emptyStateView = findViewById(R.id.emptyStateView);
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

        // Setup DB, DAO, Adapter, RecyclerView
        TaskDatabase db = TaskDatabase.getInstance(this);
        ReminderDao reminderDao = db.reminderDao();

        RecyclerView recyclerView = findViewById(R.id.remindersRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        reminderAdapter = new ReminderAdapter(reminderDao);
        recyclerView.setAdapter(reminderAdapter);

        // Observe reminders
        reminderDao.getRemindersByList(listId).observe(this, reminders -> {
            reminderAdapter.setReminders(reminders);

            if (reminders == null || reminders.isEmpty()) {
                emptyStateView.setVisibility(View.VISIBLE);
            } else {
                emptyStateView.setVisibility(View.GONE);
            }
        });


        // ✅ Add swipe-to-delete support
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
                        viewHolder.itemView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                        // Fade-out animation before actual removal
                        View itemView = viewHolder.itemView;
                        itemView.animate()
                                .alpha(0f)
                                .setDuration(200)
                                .withEndAction(() -> {
                                    // Remove from adapter list + DB
                                    reminderAdapter.getReminders().remove(position);
                                    reminderAdapter.notifyItemRemoved(position);

                                    new Thread(() -> reminderDao.delete(reminderToDelete)).start();

                                    // Show styled Snackbar for Undo
                                    Snackbar snackbar = Snackbar.make(findViewById(R.id.listDetailRoot), "Reminder deleted", Snackbar.LENGTH_LONG)
                                            .setAction("UNDO", v -> {
                                                // Re-insert if Undo tapped
                                                new Thread(() -> reminderDao.insert(reminderToDelete)).start();
                                            })
                                            .setAnchorView(R.id.bottomBar);

                                    // Styling
                                    View sbView = snackbar.getView();
                                    sbView.setBackgroundColor(Color.parseColor("#333333")); // dark bg
                                    TextView sbText = sbView.findViewById(com.google.android.material.R.id.snackbar_text);
                                    sbText.setTextColor(Color.WHITE);
                                    sbText.setTextSize(14f);
                                    sbText.setTypeface(getResources().getFont(R.font.nf)); // 🧠 Requires nf.ttf in font folder

                                    TextView sbAction = sbView.findViewById(com.google.android.material.R.id.snackbar_action);
                                    sbAction.setTextColor(Color.parseColor("#2196F3")); // accent blue

                                    snackbar.show();
                                });
                    }



                    @Override
                    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                            @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                            int actionState, boolean isCurrentlyActive) {

                        // Background color
                        Paint paint = new Paint();
                        paint.setColor(ContextCompat.getColor(ListDetailsActivity.this, R.color.flagRed));

                        View itemView = viewHolder.itemView;
                        c.drawRect(itemView.getLeft(), itemView.getTop(),
                                itemView.getLeft() + dX, itemView.getBottom(), paint);

                        // Draw trash icon
                        Drawable icon = ContextCompat.getDrawable(ListDetailsActivity.this, android.R.drawable.ic_menu_delete);
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
                Reminder reminder = new Reminder(reminderTitle, desc, false, false, date, listId, repeat); // ✅ pass repeat directly
                new Thread(() -> reminderDao.insert(reminder)).start();
            });
        });


        // Filter button
        findViewById(R.id.filterButton).setOnClickListener(v -> {
            Toast.makeText(this, "Filter coming soon", Toast.LENGTH_SHORT).show();
        });
    }
}
