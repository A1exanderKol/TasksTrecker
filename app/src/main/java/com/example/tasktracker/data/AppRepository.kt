package com.example.tasktracker.data

import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val categoryDao: CategoryDao,
    private val taskDao: TaskDao
) {
    // ---- Категории ----
    val categories: Flow<List<Category>> = categoryDao.getAll()
    val categoriesWithCount: Flow<List<CategoryWithCount>> = categoryDao.getAllWithCount()

    suspend fun getCategory(id: Long): Category? = categoryDao.getById(id)
    suspend fun addCategory(category: Category): Long = categoryDao.insert(category)
    suspend fun updateCategory(category: Category) = categoryDao.update(category)
    suspend fun deleteCategory(id: Long) = categoryDao.deleteById(id) // задачи удалятся каскадно

    // ---- Задачи ----
    fun tasks(categoryId: Long?): Flow<List<TaskWithCategory>> =
        if (categoryId == null) taskDao.getAllWithCategory() else taskDao.getByCategory(categoryId)

    suspend fun getTask(id: Long): Task? = taskDao.getById(id)
    suspend fun addTask(task: Task): Long = taskDao.insert(task)
    suspend fun updateTask(task: Task) = taskDao.update(task)
    suspend fun deleteTask(task: Task) = taskDao.delete(task)
    suspend fun setTaskCompleted(id: Long, completed: Boolean) = taskDao.setCompleted(id, completed)
}
