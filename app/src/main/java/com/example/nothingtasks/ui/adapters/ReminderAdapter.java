package com.example.nothingtasks.ui.adapters;

import android.graphics.Paint;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.nothingtasks.R;
import com.example.nothingtasks.data.db.ReminderDao;
import com.example.nothingtasks.data.model.Reminder;
import com.google.android.material.checkbox.MaterialCheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {

    private List<Reminder> reminders = new ArrayList<>();
    private final ReminderDao reminderDao;

    // Store expanded positions to allow toggling
    private final Set<Integer> expandedPositions = new HashSet<>();

    public ReminderAdapter(ReminderDao dao) {
        this.reminderDao = dao;
    }

    public void setReminders(List<Reminder> newReminders) {
        this.reminders.clear();
        this.reminders.addAll(newReminders);
        notifyDataSetChanged();
    }

    public Reminder getReminderAt(int position) {
        return reminders.get(position);
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reminder, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        Reminder reminder = reminders.get(position);

        holder.title.setText(reminder.title);
        holder.description.setText(reminder.description);

        holder.checkBox.setChecked(reminder.isDone);

        // Check if expanded
        boolean isExpanded = expandedPositions.contains(position);
        holder.expandedSection.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        // Elevation effect
        holder.itemView.setElevation(isExpanded ? 8f : 2f);

        // Toggle expand/collapse on item click
        holder.itemView.setOnClickListener(v -> {
            if (isExpanded) {
                expandedPositions.remove(position);
            } else {
                expandedPositions.add(position);
            }
            notifyItemChanged(position); // Triggers animation + elevation
        });

        // Strike-through title if done
        if (reminder.isDone) {
            holder.title.setPaintFlags(holder.title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.title.setPaintFlags(holder.title.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        // Toggle isDone
        holder.checkBox.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            reminder.isDone = holder.checkBox.isChecked();
            new Thread(() -> reminderDao.update(reminder)).start();
            notifyItemChanged(position);
        });

        // Toggle isFlagged
        // Set correct flag icon
        holder.flagIcon.setImageResource(reminder.isFlagged ? R.drawable.bb : R.drawable.aa);

// Toggle on click
        holder.flagIcon.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            reminder.isFlagged = !reminder.isFlagged;
            new Thread(() -> reminderDao.update(reminder)).start();
            notifyItemChanged(position);
        });


        // If date field is present
        if (holder.date != null) {
            if (reminder.date != null && !reminder.date.trim().isEmpty()) {
                holder.date.setVisibility(View.VISIBLE);
                holder.date.setText("Scheduled: " + reminder.date);
            } else {
                holder.date.setVisibility(View.GONE);
            }
        }
    }
    public void removeReminder(Reminder reminder) {
        int index = reminders.indexOf(reminder);
        if (index != -1) {
            reminders.remove(index);
            notifyItemRemoved(index);
        }
    }

    @Override
    public int getItemCount() {
        return (reminders != null) ? reminders.size() : 0;
    }
    public List<Reminder> getReminders() {
        return reminders;
    }


    static class ReminderViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, date;
        MaterialCheckBox checkBox;
        ImageView flagIcon;

        LinearLayout expandedSection;

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.reminderTitle);
            description = itemView.findViewById(R.id.reminderDescription);
            checkBox = itemView.findViewById(R.id.checkBoxDone);
            flagIcon = itemView.findViewById(R.id.flagIcon);
            expandedSection = itemView.findViewById(R.id.expandedSection);
            date = itemView.findViewById(R.id.reminderDate); // optional, safe to null-check
        }
    }
}
