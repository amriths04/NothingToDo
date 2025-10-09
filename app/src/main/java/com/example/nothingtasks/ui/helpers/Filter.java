package com.example.nothingtasks.ui.helpers;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;

import com.example.nothingtasks.R;

public class Filter {

    public interface OnFilterApplied {
        void onApply(
                String[] dateFilters,   // Today / Upcoming / Overdue (any combination)
                boolean flagFilter,     // Flagged only
                String[] doneFilters,   // Completed only / Pending only
                String[] repeatFilters, // Repeating only / Non-repeating only
                String sortOption       // Date ↑ / Date ↓ / Title A–Z / Title Z–A / Flagged first
        );
    }

    public static void show(Activity activity, OnFilterApplied listener) {
        // Inflate the overlay
        View overlay = LayoutInflater.from(activity).inflate(R.layout.filter_overlay, null);

        final PopupWindow popup = new PopupWindow(
                overlay,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        popup.setClippingEnabled(false);
        popup.setBackgroundDrawable(new ColorDrawable(0x00000000));
        popup.setOutsideTouchable(true);
        popup.setTouchable(true);

        View root = activity.findViewById(android.R.id.content);
        popup.showAtLocation(root, Gravity.BOTTOM, 0, 0);

        // Slide-up animation
        overlay.post(() -> {
            TranslateAnimation anim = new TranslateAnimation(0, 0, overlay.getHeight(), 0);
            anim.setDuration(250);
            overlay.startAnimation(anim);
        });

        // Swipe-to-dismiss gesture
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

        // --- FIND FILTERS (CHECKBOXES) ---
        CheckBox dateToday = overlay.findViewById(R.id.filterToday);
        CheckBox dateUpcoming = overlay.findViewById(R.id.filterUpcoming);
        CheckBox dateOverdue = overlay.findViewById(R.id.filterOverdue);
        CheckBox[] dateFilters = {dateToday, dateUpcoming, dateOverdue};

        CheckBox flagOnly = overlay.findViewById(R.id.flagOnly);
        CheckBox[] flagFilters = {flagOnly};

        CheckBox doneOnly = overlay.findViewById(R.id.doneOnly);
        CheckBox pendingOnly = overlay.findViewById(R.id.pendingOnly);
        CheckBox[] doneFilters = {doneOnly, pendingOnly};

        CheckBox repeatOnly = overlay.findViewById(R.id.repeatOnly);
        CheckBox nonRepeatOnly = overlay.findViewById(R.id.nonRepeatOnly);
        CheckBox[] repeatFilters = {repeatOnly, nonRepeatOnly};

        // --- SORT OPTIONS (RADIOBUTTONS) ---
        RadioButton sortDateAsc = overlay.findViewById(R.id.sortDateAsc);
        RadioButton sortDateDesc = overlay.findViewById(R.id.sortDateDesc);
        RadioButton sortTitleAsc = overlay.findViewById(R.id.sortTitleAsc);
        RadioButton sortTitleDesc = overlay.findViewById(R.id.sortTitleDesc);
        RadioButton sortFlaggedFirst = overlay.findViewById(R.id.sortFlaggedFirst);
        RadioButton[] sortGroup = {sortDateAsc, sortDateDesc, sortTitleAsc, sortTitleDesc, sortFlaggedFirst};

        LinearLayout buttonLayout = overlay.findViewById(R.id.buttonLayout);
        Button btnApply = overlay.findViewById(R.id.btnApply);
        Button btnClear = overlay.findViewById(R.id.btnClear);

        // --- SHOW BUTTONS WHEN ANY FILTER OR SORT ACTIVE ---
        Runnable updateButtonVisibility = () -> {
            boolean anyChecked = false;

            for (CheckBox cb : dateFilters) if (cb.isChecked()) anyChecked = true;
            for (CheckBox cb : flagFilters) if (cb.isChecked()) anyChecked = true;
            for (CheckBox cb : doneFilters) if (cb.isChecked()) anyChecked = true;
            for (CheckBox cb : repeatFilters) if (cb.isChecked()) anyChecked = true;
            for (RadioButton rb : sortGroup) if (rb.isChecked()) anyChecked = true;

            buttonLayout.setVisibility(anyChecked ? View.VISIBLE : View.GONE);
        };

        // Attach checkbox listener
        for (CheckBox cb : dateFilters) cb.setOnClickListener(v -> updateButtonVisibility.run());
        for (CheckBox cb : flagFilters) cb.setOnClickListener(v -> updateButtonVisibility.run());
        for (CheckBox cb : doneFilters) cb.setOnClickListener(v -> updateButtonVisibility.run());
        for (CheckBox cb : repeatFilters) cb.setOnClickListener(v -> updateButtonVisibility.run());

        // --- APPLY BUTTON ---
        btnApply.setOnClickListener(v -> {
            String[] selectedDates = getChecked(dateFilters);
            String[] selectedDone = getChecked(doneFilters);
            String[] selectedRepeat = getChecked(repeatFilters);
            boolean flag = flagOnly.isChecked();
            String sortOption = getCheckedRadio(sortGroup, "Date ↑");

            listener.onApply(selectedDates, flag, selectedDone, selectedRepeat, sortOption);
            popup.dismiss();
        });

        // --- CLEAR BUTTON ---
        btnClear.setOnClickListener(v -> {
            for (CheckBox cb : dateFilters) cb.setChecked(false);
            for (CheckBox cb : flagFilters) cb.setChecked(false);
            for (CheckBox cb : doneFilters) cb.setChecked(false);
            for (CheckBox cb : repeatFilters) cb.setChecked(false);
            for (RadioButton rb : sortGroup) rb.setChecked(false);
            buttonLayout.setVisibility(View.GONE);
        });

        // --- SORT BUTTON TOGGLE LOGIC ---
        final RadioButton[] currentChecked = {null}; // track currently selected

        for (RadioButton rb : sortGroup) {
            rb.setOnClickListener(v -> {
                if (currentChecked[0] == rb) {
                    // Clicked the same button → uncheck it
                    rb.setChecked(false);
                    currentChecked[0] = null;
                } else {
                    // Clicked a new button → check it and uncheck previous
                    if (currentChecked[0] != null) currentChecked[0].setChecked(false);
                    rb.setChecked(true);
                    currentChecked[0] = rb;
                }
                updateButtonVisibility.run();
            });
        }

    }

    private static String[] getChecked(CheckBox[] group) {
        return java.util.Arrays.stream(group)
                .filter(CheckBox::isChecked)
                .map(cb -> cb.getText().toString())
                .toArray(String[]::new);
    }

    private static String getCheckedRadio(RadioButton[] group, String defaultText) {
        for (RadioButton rb : group)
            if (rb.isChecked()) return rb.getText().toString();
        return defaultText;
    }
}
