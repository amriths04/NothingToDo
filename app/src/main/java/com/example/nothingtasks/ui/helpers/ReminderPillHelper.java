package com.example.nothingtasks.ui.helpers;

import android.content.Context;
import android.util.TypedValue;
import android.widget.TextView;

import com.example.nothingtasks.R;
import com.example.nothingtasks.data.model.Reminder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ReminderPillHelper {

    private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    private static final SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private static String getDatePill(Context context, Reminder reminder) {
        if (reminder.date == null || reminder.date.trim().isEmpty()) return null;
        try {
            Date reminderDate;
            if (reminder.date.length() > 10) {
                reminderDate = dateTimeFormat.parse(reminder.date);
            } else {
                reminderDate = dateOnlyFormat.parse(reminder.date);
            }
            if (reminderDate == null) return null;

            SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
            String reminderDay = dayFormat.format(reminderDate);
            String todayDay = dayFormat.format(new Date());

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, 1);
            String tomorrowDay = dayFormat.format(cal.getTime());

            if (reminderDay.equals(todayDay)) return "TODAY";
            if (reminderDay.equals(tomorrowDay)) return "TOMORROW";
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
    private static int getDatePillColorAttr(String pill) {
        switch (pill) {
            case "TODAY": return R.attr.pillTodayColor;
            case "TOMORROW": return R.attr.pillTomorrowColor;
            default: return R.attr.pillNoDateColor;
        }
    }

    public static void applyPills(Context context, Reminder reminder, TextView pill1, TextView pill2) {
        pill1.setVisibility(TextView.GONE);
        pill2.setVisibility(TextView.GONE);

        boolean hasRepeat = reminder.repeat != null && !reminder.repeat.trim().isEmpty();
        boolean hasDate = reminder.date != null && !reminder.date.trim().isEmpty();

        if (hasRepeat) {
            String repeatText = getRepeatText(reminder.repeat);
            if (reminder.isDone) {
                setPill(context, pill1, "DONE", R.attr.pillDoneColor);
                setPill(context, pill2, repeatText, R.attr.pillRepeatColor);
            } else if (isOverdue(reminder)) {
                setPill(context, pill1, "OVERDUE", R.attr.pillOverdueColor);
                setPill(context, pill2, repeatText, R.attr.pillRepeatColor);
            } else {
                String datePill = getDatePill(context, reminder);
                if (datePill != null) {
                    setPill(context, pill1, datePill, getDatePillColorAttr(datePill));
                    setPill(context, pill2, repeatText, R.attr.pillRepeatColor);
                } else {
                    setPill(context, pill1, repeatText, R.attr.pillRepeatColor);
                }
            }

            return; // exit here as repeat handled
        }

        if (!hasDate) {
            if (reminder.isDone) {
                setPill(context, pill1, "DONE", R.attr.pillDoneColor);
            } else {
                setPill(context, pill1, "NO DATE", R.attr.pillNoDateColor);
            }
            return;
        }

        Date reminderDate = null;
        boolean hasTime = reminder.date.length() > 10; // assume if longer than yyyy-MM-dd, time is included

        try {
            if (hasTime) {
                reminderDate = dateTimeFormat.parse(reminder.date);
            } else {
                reminderDate = dateOnlyFormat.parse(reminder.date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (reminderDate == null) {
            // Parsing failed, fallback
            if (reminder.isDone) {
                setPill(context, pill1, "DONE", R.attr.pillDoneColor);
            } else {
                setPill(context, pill1, "NO DATE", R.attr.pillNoDateColor);
            }
            return;
        }

        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.add(Calendar.DAY_OF_YEAR, 1);
        Date tomorrow = cal.getTime();

        String reminderDay = dayFormat.format(reminderDate);
        String todayDay = dayFormat.format(now);
        String tomorrowDay = dayFormat.format(tomorrow);

        if (isOverdue(reminder)) {
            setPill(context, pill1, "OVERDUE", R.attr.pillOverdueColor);
        } else if (reminder.isDone && reminderDay.equals(todayDay)) {
            setPill(context, pill1, "DONE", R.attr.pillDoneColor);
        } else if (!reminder.isDone && reminderDay.equals(todayDay)) {
            if (!hasTime) {
                // No time means show TODAY
                setPill(context, pill1, "TODAY", R.attr.pillTodayColor);
            } else {
                // Has time, check if overdue or today
                Calendar remCal = Calendar.getInstance();
                remCal.setTime(reminderDate);
                if (reminderDate.before(now)) {
                    setPill(context, pill1, "OVERDUE", R.attr.pillOverdueColor);
                } else {
                    setPill(context, pill1, "TODAY", R.attr.pillTodayColor);
                }
            }
        } else if (!reminder.isDone && reminderDay.equals(tomorrowDay)) {
            setPill(context, pill1, "TOMORROW", R.attr.pillTomorrowColor);
        } else if (reminder.isDone) {
            setPill(context, pill1, "DONE", R.attr.pillDoneColor);
        }
    }

    private static String getRepeatText(String repeat) {
        switch (repeat.toLowerCase(Locale.ROOT)) {
            case "day":
                return "REPEAT DAILY";
            case "week":
                return "REPEAT WEEKLY";
            case "month":
                return "REPEAT MONTHLY";
            case "year":
                return "REPEAT YEARLY";
            default:
                return "REPEAT";
        }
    }

    private static boolean isOverdue(Reminder reminder) {
        if (reminder.date == null || reminder.date.trim().isEmpty()) return false;
        if (reminder.isDone) return false;

        try {
            Date reminderDate;
            boolean hasTime = reminder.date.length() > 10;

            if (hasTime) {
                reminderDate = dateTimeFormat.parse(reminder.date);
                if (reminderDate == null) return false;
                return reminderDate.before(new Date());
            } else {
                reminderDate = dateOnlyFormat.parse(reminder.date);
                if (reminderDate == null) return false;

                SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
                String reminderDay = dayFormat.format(reminderDate);
                String todayDay = dayFormat.format(new Date());

                return reminderDay.compareTo(todayDay) < 0; // before today
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void setPill(Context context, TextView pill, String text, int colorAttr) {
        pill.setText(text);
        TypedValue bgColor = new TypedValue();
        context.getTheme().resolveAttribute(colorAttr, bgColor, true);
        pill.setBackgroundColor(bgColor.data);

        TypedValue textColor = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.pillTextColor, textColor, true);
        pill.setTextColor(textColor.data);

        pill.setVisibility(TextView.VISIBLE);
    }
}
