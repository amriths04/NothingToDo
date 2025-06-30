package com.example.nothingtasks.ui.activites;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.example.nothingtasks.R;

import java.util.Calendar;

public class AddReminderDialog {

    public interface OnReminderAddListener {
        void onReminderAdded(String title, String description, @Nullable String datetime, @Nullable String repeat);
    }

    public static void show(Context context, OnReminderAddListener listener) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_reminder, null);

        EditText titleEdit = dialogView.findViewById(R.id.reminderTitle);
        EditText descEdit = dialogView.findViewById(R.id.reminderDesc);
        TextView dateText = dialogView.findViewById(R.id.dateText);
        TextView timeText = dialogView.findViewById(R.id.timeText);
        Spinner repeatSpinner = dialogView.findViewById(R.id.repeatSpinner);
        TextView todayShortcut = dialogView.findViewById(R.id.todayShortcut);
        TextView clearDateBtn = dialogView.findViewById(R.id.clearDateBtn);

        // Updated: use containers
        FrameLayout datePickerContainer = dialogView.findViewById(R.id.datePickerContainer);
        FrameLayout timePickerContainer = dialogView.findViewById(R.id.timePickerContainer);
        DatePicker datePicker = dialogView.findViewById(R.id.datePicker);
        TimePicker timePicker = dialogView.findViewById(R.id.timePicker);

        ImageButton pickDateBtn = dialogView.findViewById(R.id.pickDateBtn);
        ImageButton pickTimeBtn = dialogView.findViewById(R.id.pickTimeBtn);

        final Calendar selectedCalendar = Calendar.getInstance();
        final boolean[] hasDate = {false};

        // Spinner setup
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                context,
                R.array.repeat_options,
                R.layout.spinner_item_nf
        );
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_nf);
        repeatSpinner.setAdapter(adapter);

        // Accordion toggle
        View.OnClickListener toggleDatePicker = v -> {
            if (datePickerContainer.getVisibility() == View.VISIBLE) {
                collapse(datePickerContainer);
            } else {
                if (timePickerContainer.getVisibility() == View.VISIBLE) collapse(timePickerContainer);
                expand(datePickerContainer);
                int day = datePicker.getDayOfMonth();
                int month = datePicker.getMonth() + 1;
                int year = datePicker.getYear();
                dateText.setText(String.format("%04d-%02d-%02d", year, month, day));
            }
        };

        View.OnClickListener toggleTimePicker = v -> {
            if (timePickerContainer.getVisibility() == View.VISIBLE) {
                collapse(timePickerContainer);
            } else {
                if (datePickerContainer.getVisibility() == View.VISIBLE) collapse(datePickerContainer);
                expand(timePickerContainer);
                int hour = timePicker.getHour();
                int minute = timePicker.getMinute();
                String ampm = hour >= 12 ? "PM" : "AM";
                int hour12 = (hour % 12 == 0) ? 12 : hour % 12;
                timeText.setText(String.format("%02d:%02d %s", hour12, minute, ampm));
            }
        };

        pickDateBtn.setOnClickListener(toggleDatePicker);
        dateText.setOnClickListener(toggleDatePicker);
        pickTimeBtn.setOnClickListener(toggleTimePicker);
        timeText.setOnClickListener(toggleTimePicker);

        // Live updates
        datePicker.setOnDateChangedListener((view, year, monthOfYear, dayOfMonth) -> {
            dateText.setText(String.format("%04d-%02d-%02d", year, monthOfYear + 1, dayOfMonth));
        });

        timePicker.setOnTimeChangedListener((view, hourOfDay, minute) -> {
            String ampm = hourOfDay >= 12 ? "PM" : "AM";
            int hour12 = (hourOfDay % 12 == 0) ? 12 : hourOfDay % 12;
            timeText.setText(String.format("%02d:%02d %s", hour12, minute, ampm));
        });

        todayShortcut.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            selectedCalendar.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
            dateText.setText("Today");
            hasDate[0] = true;
            todayShortcut.setSelected(true);
            clearDateBtn.setSelected(false);
        });

        clearDateBtn.setOnClickListener(v -> {
            titleEdit.setText("");
            descEdit.setText("");
            dateText.setText("No date");
            hasDate[0] = false;
            todayShortcut.setSelected(false);
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
            if (datePickerContainer.getVisibility() == View.VISIBLE || timePickerContainer.getVisibility() == View.VISIBLE) {
                selectedCalendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                selectedCalendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                selectedCalendar.set(Calendar.MINUTE, timePicker.getMinute());

                dateTime = String.format("%04d-%02d-%02d %02d:%02d",
                        selectedCalendar.get(Calendar.YEAR),
                        selectedCalendar.get(Calendar.MONTH) + 1,
                        selectedCalendar.get(Calendar.DAY_OF_MONTH),
                        timePicker.getHour(),
                        timePicker.getMinute());
            }

            String repeat = repeatSpinner.getSelectedItem().toString();
            if ("None".equals(repeat)) {
                repeat = null;
            }

            listener.onReminderAdded(title, desc, dateTime, repeat);
            dialog.dismiss();
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            int width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.97);
            window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private static void expand(View view) {
        view.setVisibility(View.VISIBLE);
        view.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int targetHeight = view.getMeasuredHeight();

        view.getLayoutParams().height = 0;
        view.requestLayout();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                view.getLayoutParams().height = (interpolatedTime == 1)
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int) (targetHeight * interpolatedTime);
                view.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
        a.setDuration(200);
        view.startAnimation(a);
    }

    private static void collapse(View view) {
        final int initialHeight = view.getHeight();
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    view.setVisibility(View.GONE);
                } else {
                    view.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    view.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
        a.setDuration(200);
        view.startAnimation(a);
    }
}
