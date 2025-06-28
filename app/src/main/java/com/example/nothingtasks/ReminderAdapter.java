package com.example.nothingtasks;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.checkbox.MaterialCheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {

    private List<Reminder> reminders = new ArrayList<>();
    private final ReminderDao reminderDao;

    public ReminderAdapter(ReminderDao dao) {
        this.reminderDao = dao;
    }

    public void setReminders(List<Reminder> reminders) {
        this.reminders = reminders;
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
        holder.flagBox.setChecked(reminder.isFlagged);

        // Strike-through title if done
        if (reminder.isDone) {
            holder.title.setPaintFlags(holder.title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.title.setPaintFlags(holder.title.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        // Toggle isDone
        holder.checkBox.setOnClickListener(v -> {
            reminder.isDone = holder.checkBox.isChecked();
            new Thread(() -> reminderDao.update(reminder)).start();
            notifyItemChanged(position);
        });

        // Toggle isFlagged
        holder.flagBox.setOnClickListener(v -> {
            reminder.isFlagged = holder.flagBox.isChecked();
            new Thread(() -> reminderDao.update(reminder)).start();
        });
    }

    @Override
    public int getItemCount() {
        return (reminders != null) ? reminders.size() : 0;
    }

    static class ReminderViewHolder extends RecyclerView.ViewHolder {
        TextView title, description;
        MaterialCheckBox checkBox, flagBox;

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.reminderTitle);
            description = itemView.findViewById(R.id.reminderDescription);
            checkBox = itemView.findViewById(R.id.checkBoxDone);
            flagBox = itemView.findViewById(R.id.checkBoxFlag); // Must exist in layout
        }
    }
}
