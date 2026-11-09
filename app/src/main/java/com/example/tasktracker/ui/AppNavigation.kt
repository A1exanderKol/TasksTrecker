package com.example.tasktracker.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import kotlinx.coroutines.launch


object Routes {
    const val CATEGORIES = "categories"
    const val TASKS = "tasks"
    const val TASK_EDIT = "task_edit/{taskId}"
    fun taskEdit(id: Long) = "task_edit/$id"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(viewModel: AppViewModel) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route

    // Одноразовые события из SharedFlow
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.events.collect { event ->
                when (event) {
                    is UiEvent.Message -> launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar(
                            message = event.text,
                            duration = SnackbarDuration.Short
                        )
                    }
                    UiEvent.NavigateBack -> navController.popBackStack()
                }
            }
        }
    }

    val isEdit = route == Routes.TASK_EDIT

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (route) {
                            Routes.CATEGORIES -> "Категории"
                            Routes.TASKS -> "Задачи"
                            else -> {
                                val id = backStackEntry?.arguments?.getLong("taskId")
                                if (id == NEW_TASK_ID) "Новая задача" else "Редактирование задачи"
                            }
                        }
                    )
                },
                navigationIcon = {
                    if (isEdit) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (!isEdit) {
                NavigationBar {
                    NavigationBarItem(
                        selected = route == Routes.CATEGORIES,
                        onClick = { navigateTop(navController, Routes.CATEGORIES) },
                        icon = { Icon(Icons.Default.Home, null) },
                        label = { Text("Категории") }
                    )
                    NavigationBarItem(
                        selected = route == Routes.TASKS,
                        onClick = { navigateTop(navController, Routes.TASKS) },
                        icon = { Icon(Icons.AutoMirrored.Filled.List, null) },
                        label = { Text("Задачи") }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.CATEGORIES,
            modifier = Modifier.padding(padding),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }
        ) {
            composable(Routes.CATEGORIES) {
                CategoriesScreen(
                    viewModel = viewModel,
                    onCategoryClick = { id ->
                        viewModel.selectCategory(id)
                        navigateTop(navController, Routes.TASKS)
                    }
                )
            }
            composable(Routes.TASKS) {
                TasksScreen(
                    viewModel = viewModel,
                    onAddTask = { navController.navigate(Routes.taskEdit(NEW_TASK_ID)) },
                    onEditTask = { id -> navController.navigate(Routes.taskEdit(id)) }
                )
            }
            composable(
                route = Routes.TASK_EDIT,
                arguments = listOf(navArgument("taskId") { type = NavType.LongType })
            ) { entry ->
                TaskEditScreen(
                    taskId = entry.arguments?.getLong("taskId") ?: NEW_TASK_ID,
                    viewModel = viewModel
                )
            }
        }
    }
}

private fun navigateTop(navController: NavHostController, route: String) {
    navController.navigate(route) {
        popUpTo(Routes.CATEGORIES) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
