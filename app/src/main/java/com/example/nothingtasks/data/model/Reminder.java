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
    public String date;

    @Nullable
    public Integer listId;

    @Nullable
    public String repeat;

    public Reminder(String title, String description, boolean isDone, boolean isFlagged, @Nullable String date, @Nullable Integer listId, @Nullable String repeat) {
        this.title = title;
        this.description = description;
        this.isDone = isDone;
        this.isFlagged = isFlagged;
        this.date = date;
        this.listId = listId;
        this.repeat = repeat;
    }
    // Defensive copy constructor (for Undo)
    public Reminder(Reminder other) {
        this.id = other.id;
        this.title = other.title;
        this.description = other.description;
        this.isDone = other.isDone;
        this.isFlagged = other.isFlagged;
        this.date = other.date;
        this.listId = other.listId;
        this.repeat = other.repeat;
    }

}
