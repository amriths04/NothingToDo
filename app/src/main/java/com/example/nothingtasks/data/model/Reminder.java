package com.example.nothingtasks.data.model;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "reminders")
public class Reminder {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public String description;

    public boolean isDone;
    public boolean isFlagged;

    @Nullable
    public String date;     // e.g., "2025-06-30" or null

    @Nullable
    public Integer listId;  // Nullable foreign key to TaskList

    @Nullable
    public String repeat;   // NEW: e.g., "None", "Daily", "Weekly", "Monthly"

    public Reminder(String title, String description, boolean isDone, boolean isFlagged, @Nullable String date, @Nullable Integer listId, @Nullable String repeat) {
        this.title = title;
        this.description = description;
        this.isDone = isDone;
        this.isFlagged = isFlagged;
        this.date = date;
        this.listId = listId;
        this.repeat = repeat;
    }
}
