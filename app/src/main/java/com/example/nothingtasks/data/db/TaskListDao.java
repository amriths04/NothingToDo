package com.example.nothingtasks.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;

import com.example.nothingtasks.data.model.TaskList;

import java.util.List;

@Dao
public interface TaskListDao {

    @Insert
    long insert(TaskList taskList); // ✅ Return ID if needed

    @Delete
    void delete(TaskList taskList);

    @Query("SELECT * FROM task_lists ORDER BY id DESC")
    LiveData<List<TaskList>> getAllLists();

    @Query("DELETE FROM task_lists WHERE id = :listId")
    void deleteListById(int listId);
}
