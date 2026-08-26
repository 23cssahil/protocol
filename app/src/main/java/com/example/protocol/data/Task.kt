package com.example.protocol.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class RepeatType { DAILY, WEEKLY, MONTHLY, NONE }

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String = "",
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val repeatType: RepeatType = RepeatType.DAILY,
    val weekDays: String = "1,2,3,4,5",  // Mon-Fri default
    val dayOfMonth: Int = 1,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
