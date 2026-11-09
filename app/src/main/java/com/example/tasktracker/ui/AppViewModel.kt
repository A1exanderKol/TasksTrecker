package com.example.tasktracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tasktracker.data.AppRepository
import com.example.tasktracker.data.Category
import com.example.tasktracker.data.CategoryWithCount
import com.example.tasktracker.data.Task
import com.example.tasktracker.data.TaskWithCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Одноразовые события для UI. */
sealed interface UiEvent {
    data class Message(val text: String) : UiEvent
    data object NavigateBack : UiEvent
}

class AppViewModel(private val repository: AppRepository) : ViewModel() {

    // ---------- Состояние (StateFlow) ----------

    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val categories: StateFlow<List<CategoryWithCount>> =
        repository.categoriesWithCount.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val tasks: StateFlow<List<TaskWithCategory>> =
        _selectedCategoryId
            .flatMapLatest { categoryId -> repository.tasks(categoryId) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // ---------- События (SharedFlow, replay = 0) ----------

    private val _events = MutableSharedFlow<UiEvent>(replay = 0, extraBufferCapacity = 16)
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    // ---------- Фильтр ----------

    fun selectCategory(id: Long?) {
        _selectedCategoryId.value = id
    }

    fun notify(text: String) {
        _events.tryEmit(UiEvent.Message(text))
    }

    // ---------- Категории ----------

    fun addCategory(name: String, colorHex: Long) {
        if (name.isBlank()) {
            notify("Введите название категории")
            return
        }
        runIo("Категория «${name.trim()}» создана") {
            repository.addCategory(Category(name = name.trim(), colorHex = colorHex))
        }
    }

    fun deleteCategory(category: CategoryWithCount) {
        runIo("Категория «${category.name}» удалена вместе с задачами (${category.taskCount})") {
            repository.deleteCategory(category.id)
            if (_selectedCategoryId.value == category.id) _selectedCategoryId.value = null
        }
    }

    // ---------- Задачи ----------

    suspend fun getTask(id: Long): Task? =
        withContext(Dispatchers.IO) { repository.getTask(id) }

    fun saveTask(task: Task) {
        if (task.title.isBlank()) {
            notify("Введите заголовок задачи")
            return
        }
        val isNew = task.id == 0L
        runIo(if (isNew) "Задача добавлена" else "Задача обновлена", navigateBack = true) {
            if (isNew) repository.addTask(task) else repository.updateTask(task)
        }
    }

    fun toggleCompleted(task: Task) {
        runIo(null) { repository.setTaskCompleted(task.id, !task.isCompleted) }
    }

    fun deleteTask(task: Task) {
        runIo("Задача удалена") { repository.deleteTask(task) }
    }

    // ---------- Общий запуск suspend-операций ----------

    private fun runIo(
        successMessage: String?,
        navigateBack: Boolean = false,
        block: suspend () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                block()
                if (navigateBack) _events.emit(UiEvent.NavigateBack)
                successMessage?.let { _events.emit(UiEvent.Message(it)) }
            } catch (e: Exception) {
                _events.emit(UiEvent.Message("Ошибка: ${e.localizedMessage ?: "неизвестная"}"))
            } finally {
                _isLoading.value = false
            }
        }
    }
}

/** Фабрика вручную — вместо DI. */
class AppViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            return AppViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
