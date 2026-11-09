package com.example.tasktracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Transaction
    @Query("SELECT * FROM tasks ORDER BY isCompleted, createdAt DESC")
    fun getAllWithCategory(): Flow<List<TaskWithCategory>>

    @Transaction
    @Query("SELECT * FROM tasks WHERE categoryId = :categoryId ORDER BY isCompleted, createdAt DESC")
    fun getByCategory(categoryId: Long): Flow<List<TaskWithCategory>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: Long): Task?

    @Insert
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("UPDATE tasks SET isCompleted = :completed WHERE id = :id")
    suspend fun setCompleted(id: Long, completed: Boolean)
}
