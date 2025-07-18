package com.example.nothingtasks.ui.adapters;

import android.content.Context;
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
import com.example.nothingtasks.ui.helpers.ReminderPillHelper;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {

    private final List<Reminder> reminders = new ArrayList<>();
    private final ReminderDao reminderDao;
    private final Set<Integer> expandedPositions = new HashSet<>();

    public ReminderAdapter(ReminderDao dao) {
        this.reminderDao = dao;
    }

    public void setReminders(List<Reminder> newReminders) {
        reminders.clear();
        reminders.addAll(newReminders);
        notifyDataSetChanged();
    }

    public Reminder getReminderAt(int position) {
        return reminders.get(position);
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reminder, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        Reminder reminder = reminders.get(position);
        Context context = holder.itemView.getContext();

        holder.title.setText(reminder.title);
        holder.description.setText(reminder.description);

        boolean isExpanded = expandedPositions.contains(position);
        holder.expandedSection.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.itemView.setElevation(isExpanded ? 8f : 2f);

        holder.itemView.setOnClickListener(v -> {
            if (isExpanded) expandedPositions.remove(position);
            else expandedPositions.add(position);
            notifyItemChanged(position);
        });

        if (reminder.isDone) {
            holder.title.setPaintFlags(holder.title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.title.setPaintFlags(holder.title.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        // Set the correct image from selector
        holder.checkBox.setSelected(reminder.isDone);

// Toggle done status on click
        holder.checkBox.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

            boolean isNowChecked = !v.isSelected();
            v.setSelected(isNowChecked);

            reminder.isDone = isNowChecked;

            new Thread(() -> reminderDao.update(reminder)).start();
            notifyItemChanged(position);
        });




        holder.flagIcon.setImageResource(reminder.isFlagged ? R.drawable.bb : R.drawable.aa);
        holder.flagIcon.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            reminder.isFlagged = !reminder.isFlagged;
            new Thread(() -> reminderDao.update(reminder)).start();
            notifyItemChanged(position);
        });

        if (reminder.date != null && !reminder.date.trim().isEmpty()) {
            holder.date.setVisibility(View.VISIBLE);
            holder.date.setText("Scheduled: " + reminder.date);
        } else {
            holder.date.setVisibility(View.GONE);
        }

        // ✅ Use extracted pill helper here
        ReminderPillHelper.applyPills(context, reminder, holder.pill1, holder.pill2);
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
        return reminders.size();
    }

    public List<Reminder> getReminders() {
        return reminders;
    }

    static class ReminderViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, date, pill1, pill2;
        ImageView checkBox;
        ImageView flagIcon;
        LinearLayout expandedSection;

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.reminderTitle);
            description = itemView.findViewById(R.id.reminderDescription);
            checkBox = itemView.findViewById(R.id.checkBoxDone);
            flagIcon = itemView.findViewById(R.id.flagIcon);
            expandedSection = itemView.findViewById(R.id.expandedSection);
            date = itemView.findViewById(R.id.reminderDate);
            pill1 = itemView.findViewById(R.id.pill1);
            pill2 = itemView.findViewById(R.id.pill2);
        }
    }
}
