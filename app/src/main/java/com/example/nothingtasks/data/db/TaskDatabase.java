package com.example.nothingtasks.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.nothingtasks.data.model.Reminder;
import com.example.nothingtasks.data.model.TaskList;

@Database(entities = {TaskList.class, Reminder.class}, version = 1)
public abstract class TaskDatabase extends RoomDatabase {

    private static TaskDatabase instance;

    public abstract TaskListDao taskListDao();
    public abstract ReminderDao reminderDao();

    public static synchronized TaskDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            TaskDatabase.class,
                            "task_database"
                    ).fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
