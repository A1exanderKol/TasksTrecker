package com.example.tasktracker.data

import androidx.room.Embedded
import androidx.room.Relation

/** Задача вместе с категорией (результат JOIN через @Relation). */
data class TaskWithCategory(
    @Embedded val task: Task,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: Category
)

/** Категория с количеством задач (результат агрегирующего запроса). */
data class CategoryWithCount(
    val id: Long,
    val name: String,
    val colorHex: Long,
    val taskCount: Int
)
