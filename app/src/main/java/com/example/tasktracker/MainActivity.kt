package com.example.tasktracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.tasktracker.data.AppDatabase
import com.example.tasktracker.data.AppRepository
import com.example.tasktracker.ui.AppNavigation
import com.example.tasktracker.ui.AppViewModel
import com.example.tasktracker.ui.AppViewModelFactory
import com.example.tasktracker.ui.theme.TaskTrackerTheme
import kotlin.getValue

class MainActivity : ComponentActivity() {
    private val database by lazy {
        Room.databaseBuilder(applicationContext, AppDatabase::class.java, "tasktracker.db").build()
    }
    private val repository by lazy { AppRepository(database.categoryDao(), database.taskDao()) }
    private val viewModel: AppViewModel by viewModels { AppViewModelFactory(repository) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface { AppNavigation(viewModel) }
            }
        }
    }
}