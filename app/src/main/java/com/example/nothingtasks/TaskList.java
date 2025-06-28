package com.example.nothingtasks;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "task_lists")
public class TaskList {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public String description;  // Optional
    public int color;

    public TaskList(String name, String description, int color) {
        this.name = name;
        this.description = description;
        this.color = color;
    }
}
