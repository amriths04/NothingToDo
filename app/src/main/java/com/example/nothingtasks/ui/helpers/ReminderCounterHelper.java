package com.example.nothingtasks.ui.helpers;

import android.view.View;
import android.widget.TextView;

public class ReminderCounterHelper {

    public static void bindCount(TextView countView, int reminderCount) {
        if (reminderCount > 0) {
            countView.setVisibility(View.VISIBLE);
            countView.setText(String.valueOf(reminderCount));
        } else {
            countView.setVisibility(View.GONE);
        }
    }
}
