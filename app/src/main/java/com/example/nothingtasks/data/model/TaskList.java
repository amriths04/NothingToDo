package com.example.nothingtasks.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "task_lists")
public class TaskList {

    @PrimaryKey(autoGenerate = true)
    public int id; // ← make it public (just this)

    private String name;
    private String description;
    private int color;

    // Constructor required by Room
    public TaskList(String name, String description, int color) {
        this.name = name;
        this.description = description;
        this.color = color;
    }

    // Room needs this
    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getColor() {
        return color;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
