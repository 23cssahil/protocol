package com.example.protocol

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.protocol.data.Task
import com.example.protocol.theme.ProtocolTheme
import com.example.protocol.ui.AddTaskScreen
import com.example.protocol.ui.HomeScreen
import com.example.protocol.ui.TaskViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: TaskViewModel by viewModels()

    private val notifPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* handled silently */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Request exact alarm permission (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            }
        }

        setContent {
            ProtocolTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }

    when (val screen = currentScreen) {
        is Screen.Home -> {
            HomeScreen(
                tasks = tasks,
                onAddTask = { taskToEdit = null; currentScreen = Screen.AddTask },
                onToggleTask = { viewModel.toggleTask(it) },
                onDeleteTask = { viewModel.deleteTask(it) },
                onEditTask = { task -> taskToEdit = task; currentScreen = Screen.AddTask }
            )
        }
        is Screen.AddTask -> {
            AddTaskScreen(
                existingTask = taskToEdit,
                onSave = { title, desc, startH, startM, endH, endM, repeat, weekDays, dayOfMonth ->
                    val existing = taskToEdit
                    if (existing != null) {
                        viewModel.updateTask(
                            existing.copy(
                                title = title,
                                description = desc,
                                startHour = startH,
                                startMinute = startM,
                                endHour = endH,
                                endMinute = endM,
                                repeatType = repeat,
                                weekDays = weekDays,
                                dayOfMonth = dayOfMonth
                            )
                        )
                    } else {
                        viewModel.addTask(title, desc, startH, startM, endH, endM, repeat, weekDays, dayOfMonth)
                    }
                    currentScreen = Screen.Home
                },
                onBack = { currentScreen = Screen.Home }
            )
        }
    }
}

sealed class Screen {
    object Home : Screen()
    object AddTask : Screen()
}
