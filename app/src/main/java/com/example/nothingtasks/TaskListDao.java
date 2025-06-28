package com.example.nothingtasks;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;

import java.util.List;

@Dao
public interface TaskListDao {

    @Insert
    void insert(TaskList taskList);

    @Delete
    void delete(TaskList taskList);

    @Query("SELECT * FROM task_lists ORDER BY id DESC")
    LiveData<List<TaskList>> getAllLists();
}
