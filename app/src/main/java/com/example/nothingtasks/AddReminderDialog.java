package com.example.nothingtasks;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;

import java.util.Calendar;

public class AddReminderDialog {

    public interface OnReminderAddListener {
        void onReminderAdded(String title, String description, String datetime);
    }

    public static void show(Context context, OnReminderAddListener listener) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_reminder, null);

        EditText titleEdit = dialogView.findViewById(R.id.reminderTitle);
        EditText descEdit = dialogView.findViewById(R.id.reminderDesc);
        TextView dateText = dialogView.findViewById(R.id.dateText);
        TextView timeText = dialogView.findViewById(R.id.timeText);
        ImageButton dateBtn = dialogView.findViewById(R.id.pickDateBtn);
        ImageButton timeBtn = dialogView.findViewById(R.id.pickTimeBtn);

        TextView todayShortcut = dialogView.findViewById(R.id.todayShortcut);
        TextView clearDateBtn = dialogView.findViewById(R.id.clearDateBtn);

        final Calendar selectedCalendar = Calendar.getInstance();
        final boolean[] hasDate = {false};
        final boolean[] hasTime = {false};

        // Shared listener to open DatePickerDialog
        View.OnClickListener openDatePicker = v -> {
            Calendar now = Calendar.getInstance();
            new DatePickerDialog(context, (view, year, month, dayOfMonth) -> {
                hasDate[0] = true;
                selectedCalendar.set(year, month, dayOfMonth);
                dateText.setText(String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth));
            }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show();
        };

        // Shared listener to open TimePickerDialog
        View.OnClickListener openTimePicker = v -> {
            Calendar now = Calendar.getInstance();
            new TimePickerDialog(context, (view, hourOfDay, minute) -> {
                hasTime[0] = true;
                selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selectedCalendar.set(Calendar.MINUTE, minute);
                timeText.setText(String.format("%02d:%02d", hourOfDay, minute));
            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false).show();
        };

        // Assign shared listeners to both icons and text views
        dateBtn.setOnClickListener(openDatePicker);
        dateText.setOnClickListener(openDatePicker);

        timeBtn.setOnClickListener(openTimePicker);
        timeText.setOnClickListener(openTimePicker);

        todayShortcut.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            selectedCalendar.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
            dateText.setText("Today");
            hasDate[0] = true;
        });

        clearDateBtn.setOnClickListener(v -> {
            titleEdit.setText("");
            descEdit.setText("");
            dateText.setText("No date");
            timeText.setText("No time");
            hasDate[0] = false;
            hasTime[0] = false;
        });

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        dialogView.findViewById(R.id.cancelBtn).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.addBtn).setOnClickListener(v -> {
            String title = titleEdit.getText().toString().trim();
            String desc = descEdit.getText().toString().trim();

            if (title.isEmpty()) {
                Toast.makeText(context, "Enter a title", Toast.LENGTH_SHORT).show();
                return;
            }

            String dateTime = null;
            if (hasDate[0] || hasTime[0]) {
                dateTime = String.format("%04d-%02d-%02d %02d:%02d",
                        selectedCalendar.get(Calendar.YEAR),
                        selectedCalendar.get(Calendar.MONTH) + 1,
                        selectedCalendar.get(Calendar.DAY_OF_MONTH),
                        selectedCalendar.get(Calendar.HOUR_OF_DAY),
                        selectedCalendar.get(Calendar.MINUTE));
            }

            listener.onReminderAdded(title, desc, dateTime);
            dialog.dismiss();
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
        dialog.show();
    }
}
