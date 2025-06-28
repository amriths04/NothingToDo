package com.example.nothingtasks;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ReminderDao {

    @Insert
    void insert(Reminder reminder);

    @Update
    void update(Reminder reminder);

    @Delete
    void delete(Reminder reminder);

    @Query("SELECT * FROM reminders ORDER BY id DESC")
    LiveData<List<Reminder>> getAllReminders();

    @Query("SELECT * FROM reminders WHERE listId = :listId ORDER BY id DESC")
    LiveData<List<Reminder>> getRemindersByList(int listId);

    @Query("SELECT * FROM reminders WHERE isFlagged = 1 ORDER BY id DESC")
    LiveData<List<Reminder>> getFlaggedReminders();

    @Query("SELECT * FROM reminders WHERE date = :filterDate ORDER BY id DESC")
    LiveData<List<Reminder>> getRemindersByDate(String filterDate);
}
