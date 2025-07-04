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

import java.text.DateFormatSymbols;
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

        FrameLayout datePickerContainer = dialogView.findViewById(R.id.datePickerContainer);
        FrameLayout timePickerContainer = dialogView.findViewById(R.id.timePickerContainer);
        TimePicker timePicker = dialogView.findViewById(R.id.timePicker);

        ImageButton pickDateBtn = dialogView.findViewById(R.id.pickDateBtn);
        ImageButton pickTimeBtn = dialogView.findViewById(R.id.pickTimeBtn);

        final Calendar selectedCalendar = Calendar.getInstance();
        final boolean[] hasDate = {false};

        // Spinner DatePicker inside datePickerContainer
        View spinnerDatePicker = dialogView.findViewById(R.id.spinnerDatePicker);
        NumberPicker monthPicker = spinnerDatePicker.findViewById(R.id.monthPicker);
        NumberPicker dayPicker = spinnerDatePicker.findViewById(R.id.dayPicker);
        NumberPicker yearPicker = spinnerDatePicker.findViewById(R.id.yearPicker);

        Calendar now = Calendar.getInstance();
        int currentYear = now.get(Calendar.YEAR);
        int currentMonth = now.get(Calendar.MONTH);
        int currentDay = now.get(Calendar.DAY_OF_MONTH);
        Calendar today = Calendar.getInstance();
        int minYear = today.get(Calendar.YEAR);
        int minMonth = today.get(Calendar.MONTH);
        int minDay = today.get(Calendar.DAY_OF_MONTH);

        monthPicker.setMinValue(0);
        monthPicker.setMaxValue(11);
        monthPicker.setDisplayedValues(new DateFormatSymbols().getMonths());
        monthPicker.setValue(currentMonth);

        yearPicker.setMinValue(currentYear);
        yearPicker.setMaxValue(currentYear + 5);
        yearPicker.setValue(currentYear);

        Runnable updateDays = () -> {
            int selectedYear = yearPicker.getValue();
            int selectedMonth = monthPicker.getValue();

            Calendar temp = Calendar.getInstance();
            temp.set(selectedYear, selectedMonth, 1);
            int maxDay = temp.getActualMaximum(Calendar.DAY_OF_MONTH);

            int minDayToSet = 1;
            boolean isSameYear = selectedYear == minYear;
            boolean isSameMonth = selectedMonth == minMonth;

            if (selectedYear < minYear || (isSameYear && selectedMonth < minMonth)) {
                yearPicker.setValue(minYear);
                monthPicker.setValue(minMonth);
                dayPicker.setMinValue(minDay);
                dayPicker.setMaxValue(maxDay);
                dayPicker.setValue(minDay);
                return;
            }

            if (isSameYear && isSameMonth) {
                minDayToSet = minDay;
            }

            dayPicker.setMinValue(minDayToSet);
            dayPicker.setMaxValue(maxDay);

            if (dayPicker.getValue() < minDayToSet) {
                dayPicker.setValue(minDayToSet);
            } else if (dayPicker.getValue() > maxDay) {
                dayPicker.setValue(maxDay);
            }
        };

        monthPicker.setOnValueChangedListener((p, o, n) -> updateDays.run());
        yearPicker.setOnValueChangedListener((p, o, n) -> updateDays.run());
        updateDays.run();

        Runnable updateDateText = () -> {
            String month = new DateFormatSymbols().getMonths()[monthPicker.getValue()];
            dateText.setText(String.format("%s %02d, %04d", month, dayPicker.getValue(), yearPicker.getValue()));
            hasDate[0] = true;
        };

        monthPicker.setOnValueChangedListener((p, o, n) -> {
            updateDays.run();
            updateDateText.run();
        });
        yearPicker.setOnValueChangedListener((p, o, n) -> {
            updateDays.run();
            updateDateText.run();
        });
        dayPicker.setOnValueChangedListener((p, o, n) -> updateDateText.run());

        updateDays.run();
        // Commented out the following line to avoid auto-setting date on dialog start
        // updateDateText.run();

        // Set dateText to "No date" by default and keep hasDate false
        dateText.setText("No date");

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                context,
                R.array.repeat_options,
                R.layout.spinner_item_nf
        );
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_nf);
        repeatSpinner.setAdapter(adapter);

        View.OnClickListener toggleDatePicker = v -> {
            if (datePickerContainer.getVisibility() == View.VISIBLE) {
                collapse(datePickerContainer);
            } else {
                if (timePickerContainer.getVisibility() == View.VISIBLE) collapse(timePickerContainer);
                expand(datePickerContainer);
                String month = new DateFormatSymbols().getMonths()[monthPicker.getValue()];
                dateText.setText(String.format("%s %02d, %04d", month, dayPicker.getValue(), yearPicker.getValue()));
                hasDate[0] = true;
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

        timePicker.setOnTimeChangedListener((view, hourOfDay, minute) -> {
            String ampm = hourOfDay >= 12 ? "PM" : "AM";
            int hour12 = (hourOfDay % 12 == 0) ? 12 : hourOfDay % 12;
            timeText.setText(String.format("%02d:%02d %s", hour12, minute, ampm));
        });

        todayShortcut.setOnClickListener(v -> {
            Calendar now1 = Calendar.getInstance();
            yearPicker.setValue(now1.get(Calendar.YEAR));
            monthPicker.setValue(now1.get(Calendar.MONTH));
            dayPicker.setValue(now1.get(Calendar.DAY_OF_MONTH));
            hasDate[0] = true;
            dateText.setText("Today");
            todayShortcut.setSelected(true);
            clearDateBtn.setSelected(false);
        });

        clearDateBtn.setOnClickListener(v -> {
            dateText.setText("No date");
            hasDate[0] = false;
            timeText.setText("No time");
            todayShortcut.setSelected(false);
        });

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        dialogView.findViewById(R.id.cancelBtn).setOnClickListener(v -> dialog.dismiss());

        final boolean[] warnedOnce = {false};
        final boolean[] warnedTimeWithoutDate = {false};

        dialogView.findViewById(R.id.addBtn).setOnClickListener(v -> {
            String title = titleEdit.getText().toString().trim();
            String desc = descEdit.getText().toString().trim();

            if (title.isEmpty()) {
                Toast.makeText(context, "Enter a title", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean timePicked = !timeText.getText().toString().equals("No time");
            boolean repeatSelected = !repeatSpinner.getSelectedItem().toString().equals("None");

            // ⛔️ Warn if only time selected but no date or repeat
            if (!hasDate[0] && timePicked && !repeatSelected) {
                if (!warnedTimeWithoutDate[0]) {
                    Toast.makeText(context, "Please select a date or enable repeat to save the time.", Toast.LENGTH_LONG).show();
                    warnedTimeWithoutDate[0] = true;
                    return;
                }
            }

            String dateTime = null;
            if (hasDate[0]) {
                selectedCalendar.set(Calendar.YEAR, yearPicker.getValue());
                selectedCalendar.set(Calendar.MONTH, monthPicker.getValue());
                selectedCalendar.set(Calendar.DAY_OF_MONTH, dayPicker.getValue());

                if (timePicked) {
                    selectedCalendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                    selectedCalendar.set(Calendar.MINUTE, timePicker.getMinute());

                    dateTime = String.format("%04d-%02d-%02d %02d:%02d",
                            selectedCalendar.get(Calendar.YEAR),
                            selectedCalendar.get(Calendar.MONTH) + 1,
                            selectedCalendar.get(Calendar.DAY_OF_MONTH),
                            selectedCalendar.get(Calendar.HOUR_OF_DAY),
                            selectedCalendar.get(Calendar.MINUTE));
                } else {
                    dateTime = String.format("%04d-%02d-%02d",
                            selectedCalendar.get(Calendar.YEAR),
                            selectedCalendar.get(Calendar.MONTH) + 1,
                            selectedCalendar.get(Calendar.DAY_OF_MONTH));
                }
            }

            String repeat = repeatSpinner.getSelectedItem().toString();
            if ("None".equals(repeat)) {
                repeat = null;
            }

            // ✅ Past time warning (only if date is present)
            if (hasDate[0]) {
                Calendar nowTime = Calendar.getInstance();
                boolean isPast;
                if (timePicked) {
                    isPast = selectedCalendar.getTimeInMillis() < nowTime.getTimeInMillis();
                } else {
                    Calendar onlyDate = (Calendar) selectedCalendar.clone();
                    onlyDate.set(Calendar.HOUR_OF_DAY, 0);
                    onlyDate.set(Calendar.MINUTE, 0);
                    onlyDate.set(Calendar.SECOND, 0);
                    onlyDate.set(Calendar.MILLISECOND, 0);

                    Calendar nowDate = (Calendar) nowTime.clone();
                    nowDate.set(Calendar.HOUR_OF_DAY, 0);
                    nowDate.set(Calendar.MINUTE, 0);
                    nowDate.set(Calendar.SECOND, 0);
                    nowDate.set(Calendar.MILLISECOND, 0);

                    isPast = onlyDate.before(nowDate);
                }

                if (isPast && !warnedOnce[0]) {
                    Toast.makeText(context, "Reminder is in the past. Tap Add again to confirm.", Toast.LENGTH_LONG).show();
                    warnedOnce[0] = true;
                    return;
                }
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
