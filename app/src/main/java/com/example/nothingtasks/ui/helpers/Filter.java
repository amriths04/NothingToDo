package com.example.nothingtasks.ui.helpers;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.PopupWindow;
import android.widget.RadioButton;

import com.example.nothingtasks.R;

public class Filter {

    public interface OnFilterApplied {
        void onApply(
                String dateFilter,   // Today / Upcoming / Overdue
                String flagFilter,   // Flagged only
                String doneFilter,   // Completed only / Pending only
                String repeatFilter, // Repeating only / Non-repeating only
                String sortOption    // Date ↑ / Date ↓ / Title A–Z / Title Z–A / Flagged first
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

        // --- FIND BUTTON GROUPS ---

        // Date group
        RadioButton dateToday = overlay.findViewById(R.id.filterToday);
        RadioButton dateUpcoming = overlay.findViewById(R.id.filterUpcoming);
        RadioButton dateOverdue = overlay.findViewById(R.id.filterOverdue);
        RadioButton[] dateGroup = {dateToday, dateUpcoming, dateOverdue};

        // Flag group
        RadioButton flagOnly = overlay.findViewById(R.id.flagOnly);
        RadioButton[] flagGroup = {flagOnly};

        // Done group
        RadioButton doneOnly = overlay.findViewById(R.id.doneOnly);
        RadioButton pendingOnly = overlay.findViewById(R.id.pendingOnly);
        RadioButton[] doneGroup = {doneOnly, pendingOnly};

        // Repeat group
        RadioButton repeatOnly = overlay.findViewById(R.id.repeatOnly);
        RadioButton nonRepeatOnly = overlay.findViewById(R.id.nonRepeatOnly);
        RadioButton[] repeatGroup = {repeatOnly, nonRepeatOnly};

        // Sort group
        RadioButton sortDateAsc = overlay.findViewById(R.id.sortDateAsc);
        RadioButton sortDateDesc = overlay.findViewById(R.id.sortDateDesc);
        RadioButton sortTitleAsc = overlay.findViewById(R.id.sortTitleAsc);
        RadioButton sortTitleDesc = overlay.findViewById(R.id.sortTitleDesc);
        RadioButton sortFlaggedFirst = overlay.findViewById(R.id.sortFlaggedFirst);
        RadioButton[] sortGroup = {sortDateAsc, sortDateDesc, sortTitleAsc, sortTitleDesc, sortFlaggedFirst};

        // --- SHARED CLICK LOGIC ---
        View.OnClickListener listenerShared = v -> {
            // Ensure mutual exclusivity for each group
            updateGroup(dateGroup, v);
            updateGroup(flagGroup, v);
            updateGroup(doneGroup, v);
            updateGroup(repeatGroup, v);
            updateGroup(sortGroup, v);

            // Notify listener with explicit defaults if none selected
            listener.onApply(
                    getCheckedText(dateGroup, "Today"),
                    getCheckedText(flagGroup, "Flagged only"),
                    getCheckedText(doneGroup, "Completed only"),
                    getCheckedText(repeatGroup, "Repeating only"),
                    getCheckedText(sortGroup, "Date ↑")
            );
        };

        // Attach click listeners
        attachClick(dateGroup, listenerShared);
        attachClick(flagGroup, listenerShared);
        attachClick(doneGroup, listenerShared);
        attachClick(repeatGroup, listenerShared);
        attachClick(sortGroup, listenerShared);
    }

    private static void attachClick(RadioButton[] group, View.OnClickListener listener) {
        for (RadioButton rb : group) rb.setOnClickListener(listener);
    }

    private static void updateGroup(RadioButton[] group, View clicked) {
        for (RadioButton rb : group)
            rb.setChecked(rb == clicked);
    }

    private static String getCheckedText(RadioButton[] group, String defaultText) {
        for (RadioButton rb : group)
            if (rb.isChecked()) return rb.getText().toString();
        return defaultText;
    }
}
