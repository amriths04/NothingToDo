package com.example.nothingtasks.ui.helpers;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.CheckBox;
import android.widget.PopupWindow;
import android.widget.RadioButton;

import com.example.nothingtasks.R;

public class Filter {

    public interface OnFilterApplied {
        void onApply(
                boolean today,
                boolean tomorrow,
                boolean done,
                boolean flagged,
                boolean overdue,
                String repeatOption,
                String sortOption
        );
    }

    public static void show(Activity activity, OnFilterApplied listener) {
        // Inflate the overlay layout
        View overlay = LayoutInflater.from(activity).inflate(R.layout.filter_overlay, null);

        final PopupWindow popup = new PopupWindow(
                overlay,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        // Make it draw above navigation/gesture bar
        popup.setClippingEnabled(false);
        popup.setBackgroundDrawable(new ColorDrawable(0x00000000));
        popup.setOutsideTouchable(true);
        popup.setTouchable(true);

        // Show at bottom
        View root = activity.findViewById(android.R.id.content);
        popup.showAtLocation(root, Gravity.BOTTOM | Gravity.START, 0, 0);

        // Slide-up animation
        overlay.post(() -> {
            TranslateAnimation anim = new TranslateAnimation(0, 0, overlay.getHeight(), 0);
            anim.setDuration(250);
            overlay.startAnimation(anim);
        });

        // Swipe-to-dismiss via top handle
        View handle = overlay.findViewById(R.id.filterOverlayRoot); // top drag handle
        overlay.setOnTouchListener(new View.OnTouchListener() {
            float startY = 0;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float dy = event.getRawY() - startY;
                        if (dy > 0) overlay.setTranslationY(dy);
                        return true;
                    case MotionEvent.ACTION_UP:
                        float distance = event.getRawY() - startY;
                        if (distance > overlay.getHeight() / 4) popup.dismiss();
                        else overlay.animate().translationY(0).setDuration(150).start();
                        return true;
                }
                return false;
            }
        });

        // Filter checkboxes
        CheckBox today = overlay.findViewById(R.id.filterToday);
        CheckBox tomorrow = overlay.findViewById(R.id.filterTomorrow);
        CheckBox done = overlay.findViewById(R.id.filterDone);
        CheckBox flagged = overlay.findViewById(R.id.filterFlagged);
        CheckBox overdue = overlay.findViewById(R.id.filterOverdue);

        // Repeat buttons
        RadioButton repeatNone = overlay.findViewById(R.id.repeatNone);
        RadioButton repeatDay = overlay.findViewById(R.id.repeatDay);
        RadioButton repeatWeek = overlay.findViewById(R.id.repeatWeek);
        RadioButton repeatMonth = overlay.findViewById(R.id.repeatMonth);
        RadioButton[] repeatButtons = new RadioButton[]{repeatNone, repeatDay, repeatWeek, repeatMonth};

        // Sort buttons
        RadioButton sortDate = overlay.findViewById(R.id.sortDate);
        RadioButton sortTitle = overlay.findViewById(R.id.sortTitle);
        RadioButton sortPriority = overlay.findViewById(R.id.sortPriority);
        RadioButton[] sortButtons = new RadioButton[]{sortDate, sortTitle, sortPriority};

        // Make horizontal buttons act like radio groups
        View.OnClickListener repeatClickListener = v -> {
            for (RadioButton btn : repeatButtons) btn.setChecked(btn == v);
            applyFilters(today, tomorrow, done, flagged, overdue, repeatButtons, sortButtons, listener, popup);
        };

        View.OnClickListener sortClickListener = v -> {
            for (RadioButton btn : sortButtons) btn.setChecked(btn == v);
            applyFilters(today, tomorrow, done, flagged, overdue, repeatButtons, sortButtons, listener, popup);
        };

        for (RadioButton btn : repeatButtons) btn.setOnClickListener(repeatClickListener);
        for (RadioButton btn : sortButtons) btn.setOnClickListener(sortClickListener);

        // Apply immediately on checkbox click
        View.OnClickListener checkBoxListener = v ->
                applyFilters(today, tomorrow, done, flagged, overdue, repeatButtons, sortButtons, listener, popup);

        today.setOnClickListener(checkBoxListener);
        tomorrow.setOnClickListener(checkBoxListener);
        done.setOnClickListener(checkBoxListener);
        flagged.setOnClickListener(checkBoxListener);
        overdue.setOnClickListener(checkBoxListener);
    }

    private static void applyFilters(
            CheckBox today, CheckBox tomorrow, CheckBox done,
            CheckBox flagged, CheckBox overdue,
            RadioButton[] repeatButtons, RadioButton[] sortButtons,
            OnFilterApplied listener, PopupWindow popup
    ) {
        String repeatOption = "none";
        for (RadioButton btn : repeatButtons) {
            if (btn.isChecked()) {
                repeatOption = btn.getText().toString().toLowerCase();
                break;
            }
        }

        String sortOption = null;
        for (RadioButton btn : sortButtons) {
            if (btn.isChecked()) {
                sortOption = btn.getText().toString().toLowerCase();
                break;
            }
        }

        listener.onApply(
                today.isChecked(),
                tomorrow.isChecked(),
                done.isChecked(),
                flagged.isChecked(),
                overdue.isChecked(),
                repeatOption,
                sortOption
        );
    }
}
