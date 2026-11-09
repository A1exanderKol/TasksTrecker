package com.example.tasktracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories ORDER BY name")
    fun getAll(): Flow<List<Category>>

    @Query(
        """
        SELECT c.id AS id, c.name AS name, c.colorHex AS colorHex, COUNT(t.id) AS taskCount
        FROM categories c
        LEFT JOIN tasks t ON t.categoryId = c.id
        GROUP BY c.id
        ORDER BY c.name
        """
    )
    fun getAllWithCount(): Flow<List<CategoryWithCount>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: Long): Category?

    @Insert
    suspend fun insert(category: Category): Long

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteById(id: Long)
}
