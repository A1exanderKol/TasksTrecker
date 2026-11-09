package com.example.tasktracker.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Category::class, Task::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun taskDao(): TaskDao
}
