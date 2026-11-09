package com.example.tasktracker.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tasktracker.data.Task

/** taskId == NEW_TASK_ID — создание, иначе редактирование. */
const val NEW_TASK_ID = -1L

@Composable
fun TaskEditScreen(
    taskId: Long,
    viewModel: AppViewModel
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedCategoryId.collectAsStateWithLifecycle()

    var original by remember { mutableStateOf<Task?>(null) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf<Long?>(if (taskId == NEW_TASK_ID) selectedFilter else null) }
    var isCompleted by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    // Загрузка редактируемой задачи
    LaunchedEffect(taskId) {
        if (taskId != NEW_TASK_ID) {
            viewModel.getTask(taskId)?.let { task ->
                original = task
                title = task.title
                description = task.description
                categoryId = task.categoryId
                isCompleted = task.isCompleted
            }
        }
    }

    val selectedName = categories.firstOrNull { it.id == categoryId }?.name ?: ""

    Column(
        Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Заголовок") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Описание") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        // Выпадающий список категорий
        Box {
            OutlinedTextField(
                value = selectedName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Категория") },
                modifier = Modifier.fillMaxWidth()
            )
            Box(Modifier.matchParentSize().clickable { expanded = true })
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.name) },
                        onClick = {
                            categoryId = category.id
                            expanded = false
                        }
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = isCompleted, onCheckedChange = { isCompleted = it })
            Text("Выполнена")
        }

        Button(
            onClick = {
                val cat = categoryId
                if (cat == null) {
                    viewModel.notify("Выберите категорию")
                } else {
                    viewModel.saveTask(
                        Task(
                            id = original?.id ?: 0L,
                            title = title.trim(),
                            description = description.trim(),
                            categoryId = cat,
                            isCompleted = isCompleted,
                            createdAt = original?.createdAt ?: System.currentTimeMillis()
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Сохранить") }
    }
}
