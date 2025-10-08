package com.example.nothingtasks.ui.activites;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nothingtasks.R;
import com.example.nothingtasks.ui.adapters.ReminderAdapter;
import com.example.nothingtasks.data.db.ReminderDao;
import com.example.nothingtasks.data.db.TaskDatabase;
import com.example.nothingtasks.data.model.Reminder;
import com.example.nothingtasks.ui.helpers.Filter;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class ListDetailsActivity extends AppCompatActivity {

    private int listId;
    private String listName;
    private ReminderAdapter reminderAdapter;

    private RecyclerView remindersRecycler;
    private RecyclerView completedRecycler;
    private LinearLayout completedSection;
    private View resizeHandle;

    private int defaultActiveWeight = 7;
    private int defaultCompletedWeight = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_list_details);

        View root = findViewById(R.id.listDetailRoot);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        View emptyStateView = findViewById(R.id.emptyStateView);
        completedSection = findViewById(R.id.completedSection);
        resizeHandle = findViewById(R.id.completedDivider);

        remindersRecycler = findViewById(R.id.remindersRecycler);
        completedRecycler = findViewById(R.id.completedRecycler);

        // Get list ID and name from Intent
        listId = getIntent().getIntExtra("listId", -1);
        listName = getIntent().getStringExtra("listName");
        if (listId == -1 || listName == null) {
            Toast.makeText(this, "Invalid list data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Top bar
        TextView title = findViewById(R.id.listTitle);
        title.setText(listName);

        ImageButton back = findViewById(R.id.backButton);
        back.setOnClickListener(v -> finish());

        ImageButton delete = findViewById(R.id.deleteListButton);
        delete.setOnClickListener(v -> {
            new Thread(() -> {
                TaskDatabase db = TaskDatabase.getInstance(getApplicationContext());
                db.taskListDao().deleteListById(listId);
                runOnUiThread(() -> {
                    Toast.makeText(this, "List deleted", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }).start();
        });

        // RecyclerView setup
        TaskDatabase db = TaskDatabase.getInstance(this);
        ReminderDao reminderDao = db.reminderDao();

        remindersRecycler.setLayoutManager(new LinearLayoutManager(this));
        reminderAdapter = new ReminderAdapter(reminderDao);
        remindersRecycler.setAdapter(reminderAdapter);

        // Observe reminders
        reminderDao.getRemindersByList(listId).observe(this, reminders -> {
            View completedTitle = findViewById(R.id.completedTitle);

            if (reminders == null || reminders.isEmpty()) {
                emptyStateView.setVisibility(View.VISIBLE);
                reminderAdapter.setReminders(null);
                completedSection.setVisibility(View.GONE);
                resizeHandle.setVisibility(View.GONE);
                return;
            }

            emptyStateView.setVisibility(View.GONE);

            List<Reminder> active = new ArrayList<>();
            List<Reminder> completed = new ArrayList<>();

            for (Reminder r : reminders) {
                if (r.isDone) completed.add(r);
                else active.add(r);
            }

            reminderAdapter.setReminders(active);

            if (!completed.isEmpty()) {
                completedSection.setVisibility(View.VISIBLE);
                resizeHandle.setVisibility(View.VISIBLE);
                completedTitle.setVisibility(View.VISIBLE);
                completedRecycler.setVisibility(View.VISIBLE);

                if (completedRecycler.getAdapter() == null) {
                    completedRecycler.setLayoutManager(new LinearLayoutManager(this));
                    completedRecycler.setAdapter(new ReminderAdapter(reminderDao));
                }
                ((ReminderAdapter) completedRecycler.getAdapter()).setReminders(completed);

            } else {
                completedSection.setVisibility(View.GONE);
                resizeHandle.setVisibility(View.GONE);
            }
        });

        // Swipe to delete for active reminders
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
                        int pos = viewHolder.getAdapterPosition();
                        Reminder reminderToDelete = reminderAdapter.getReminderAt(pos);
                        viewHolder.itemView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

                        viewHolder.itemView.animate()
                                .alpha(0f)
                                .setDuration(200)
                                .withEndAction(() -> {
                                    reminderAdapter.removeReminder(reminderToDelete);
                                    new Thread(() -> reminderDao.delete(reminderToDelete)).start();

                                    Snackbar snackbar = Snackbar.make(root, "Reminder deleted", Snackbar.LENGTH_LONG)
                                            .setAction("UNDO", v -> new Thread(() -> reminderDao.insert(reminderToDelete)).start())
                                            .setAnchorView(R.id.bottomBar);

                                    View sbView = snackbar.getView();
                                    sbView.setBackgroundColor(Color.parseColor("#333333"));
                                    TextView sbText = sbView.findViewById(com.google.android.material.R.id.snackbar_text);
                                    sbText.setTextColor(Color.WHITE);
                                    sbText.setTextSize(14f);
                                    sbText.setTypeface(ResourcesCompat.getFont(ListDetailsActivity.this, R.font.nf));
                                    TextView sbAction = sbView.findViewById(com.google.android.material.R.id.snackbar_action);
                                    sbAction.setTextColor(Color.parseColor("#2196F3"));

                                    snackbar.show();
                                });
                    }

                    @Override
                    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                            @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                            int actionState, boolean isCurrentlyActive) {
                        Paint paint = new Paint();
                        paint.setColor(ContextCompat.getColor(ListDetailsActivity.this, R.color.flagRed));

                        View itemView = viewHolder.itemView;
                        c.drawRect(itemView.getLeft(), itemView.getTop(),
                                itemView.getLeft() + dX, itemView.getBottom(), paint);

                        Drawable icon = ContextCompat.getDrawable(ListDetailsActivity.this, android.R.drawable.ic_menu_delete);
                        int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                        int iconTop = itemView.getTop() + iconMargin;
                        int iconLeft = itemView.getLeft() + 32;
                        int iconRight = iconLeft + icon.getIntrinsicWidth();
                        int iconBottom = iconTop + icon.getIntrinsicHeight();

                        if (dX > 0) {
                            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                            icon.draw(c);
                        }

                        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                    }
                });
        itemTouchHelper.attachToRecyclerView(remindersRecycler);

        // Add Reminder
        findViewById(R.id.addReminderBtn).setOnClickListener(v -> {
            AddReminderDialog.show(this, (reminderTitle, desc, date, repeat) -> {
                Reminder reminder = new Reminder(reminderTitle, desc, false, false, date, listId, repeat);
                new Thread(() -> reminderDao.insert(reminder)).start();
            });
        });

        // Filter button
        findViewById(R.id.filterButton).setOnClickListener(v ->
                Filter.show(ListDetailsActivity.this, (dateFilter, flagFilter, doneFilter, repeatFilter, sortOption) -> {
                    // Display selected filters for testing
                    String message = "Date=" + dateFilter +
                            ", Flag=" + flagFilter +
                            ", Done=" + doneFilter +
                            ", Repeat=" + repeatFilter +
                            ", Sort=" + sortOption;

                    Toast.makeText(ListDetailsActivity.this, message, Toast.LENGTH_SHORT).show();

                    // TODO: Implement RecyclerView filtering + sorting logic
                    // Example (pseudo-code):
            /*
            List<Reminder> filteredList = ReminderFilterHelper.filter(
                    allReminders, dateFilter, flagFilter, doneFilter, repeatFilter, sortOption
            );
            reminderAdapter.submitList(filteredList);
            */
                })
        );




        // ✅ Draggable divider for resizing completed section
        setupResizeHandle();
    }

    private void setupResizeHandle() {
        resizeHandle.setOnTouchListener(new View.OnTouchListener() {
            float startY;
            int activeHeight, completedHeight;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                LinearLayout.LayoutParams activeParams =
                        (LinearLayout.LayoutParams) remindersRecycler.getLayoutParams();
                LinearLayout.LayoutParams completedParams =
                        (LinearLayout.LayoutParams) completedSection.getLayoutParams();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startY = event.getRawY();
                        activeHeight = remindersRecycler.getHeight();
                        completedHeight = completedSection.getHeight();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float dy = event.getRawY() - startY;
                        int newActive = activeHeight + (int) dy;
                        int newCompleted = completedHeight - (int) dy;

                        if (newActive > 200 && newCompleted > 200) {
                            activeParams.height = newActive;
                            activeParams.weight = 0;
                            completedParams.height = newCompleted;
                            completedParams.weight = 0;

                            remindersRecycler.setLayoutParams(activeParams);
                            completedSection.setLayoutParams(completedParams);
                        }
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Reset split to default 70/30
        LinearLayout.LayoutParams activeParams =
                (LinearLayout.LayoutParams) remindersRecycler.getLayoutParams();
        LinearLayout.LayoutParams completedParams =
                (LinearLayout.LayoutParams) completedSection.getLayoutParams();

        activeParams.height = 0;
        activeParams.weight = defaultActiveWeight;
        completedParams.height = 0;
        completedParams.weight = defaultCompletedWeight;

        remindersRecycler.setLayoutParams(activeParams);
        completedSection.setLayoutParams(completedParams);
    }
}