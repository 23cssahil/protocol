package com.example.protocol.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.protocol.ProtocolApp
import com.example.protocol.data.RepeatType
import com.example.protocol.data.Task
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as ProtocolApp
    private val taskDao = app.database.taskDao()
    private val scheduler = app.alarmScheduler

    val tasks: StateFlow<List<Task>> = taskDao.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTask(
        title: String,
        description: String,
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int,
        repeatType: RepeatType,
        weekDays: String,
        dayOfMonth: Int
    ) {
        viewModelScope.launch {
            val task = Task(
                title = title,
                description = description,
                startHour = startHour,
                startMinute = startMinute,
                endHour = endHour,
                endMinute = endMinute,
                repeatType = repeatType,
                weekDays = weekDays,
                dayOfMonth = dayOfMonth,
                isActive = true
            )
            val id = taskDao.insertTask(task)
            val savedTask = task.copy(id = id.toInt())
            scheduler.scheduleTask(savedTask)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            taskDao.updateTask(task)
            if (task.isActive) {
                scheduler.scheduleTask(task)
            } else {
                scheduler.cancelTask(task)
            }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            scheduler.cancelTask(task)
            taskDao.deleteTask(task)
        }
    }

    fun toggleTask(task: Task) {
        viewModelScope.launch {
            val updated = task.copy(isActive = !task.isActive)
            taskDao.updateTask(updated)
            if (updated.isActive) {
                scheduler.scheduleTask(updated)
            } else {
                scheduler.cancelTask(updated)
            }
        }
    }
}
