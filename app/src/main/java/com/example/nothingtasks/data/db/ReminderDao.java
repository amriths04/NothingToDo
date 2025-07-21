package com.example.nothingtasks.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import androidx.room.Update;

import com.example.nothingtasks.data.model.Reminder;

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

    // Get reminders for today (date = current date)
    @Query("SELECT * FROM reminders WHERE date LIKE :todayDate || '%' ORDER BY id DESC")
    LiveData<List<Reminder>> getRemindersForToday(String todayDate);

    // Get scheduled reminders (date after today)
    @Query("SELECT * FROM reminders WHERE datetime(date) > datetime(:nowDateTime) ORDER BY datetime(date) ASC")
    LiveData<List<Reminder>> getScheduledReminders(String nowDateTime);


    @Query("SELECT COUNT(*) FROM reminders")
    int getCountAll();

    @Query("SELECT COUNT(*) FROM reminders WHERE date = :todayDate")
    int getCountToday(String todayDate);

    @Query("SELECT COUNT(*) FROM reminders WHERE date > :todayDate")
    int getCountScheduled(String todayDate);

    @Query("SELECT COUNT(*) FROM reminders WHERE isFlagged = 1")
    int getCountFlagged();
    @Query("SELECT * FROM reminders WHERE listId = :listId AND isDone = 1 ORDER BY id DESC")
    LiveData<List<Reminder>> getDoneRemindersByList(int listId);
}
