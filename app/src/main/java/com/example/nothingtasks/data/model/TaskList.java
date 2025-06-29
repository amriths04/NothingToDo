package com.example.nothingtasks.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "task_lists")
public class TaskList {

    @PrimaryKey(autoGenerate = true)
    public int id;

    private String name;
    private String description;

    public TaskList(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public void setId(int id) { this.id = id; }
    public int getId() { return id; }

    public String getName() { return name; }
    public String getDescription() { return description; }

    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
}
