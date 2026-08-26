package com.example.protocol.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.protocol.data.AppDatabase
import com.example.protocol.data.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getIntExtra("TASK_ID", -1)
        val taskTitle = intent.getStringExtra("TASK_TITLE") ?: "Task Reminder"
        val taskDesc = intent.getStringExtra("TASK_DESC") ?: ""

        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra("TASK_ID", taskId)
            putExtra("TASK_TITLE", taskTitle)
            putExtra("TASK_DESC", taskDesc)
            putExtra("TASK_REPEAT", intent.getStringExtra("TASK_REPEAT"))
            putExtra("TASK_START_HOUR", intent.getIntExtra("TASK_START_HOUR", 0))
            putExtra("TASK_START_MIN", intent.getIntExtra("TASK_START_MIN", 0))
            putExtra("TASK_WEEK_DAYS", intent.getStringExtra("TASK_WEEK_DAYS"))
            putExtra("TASK_DAY_OF_MONTH", intent.getIntExtra("TASK_DAY_OF_MONTH", 1))
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.LOCKED_BOOT_COMPLETED") {
            // Reschedule all alarms from DB after reboot
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                val db = AppDatabase.getDatabase(context)
                val scheduler = AlarmScheduler(context)
                val tasks = db.taskDao().getActiveTasks()
                tasks.forEach { scheduler.scheduleTask(it) }
            }
        }
    }
}

class DismissReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val stopIntent = Intent(context, AlarmService::class.java).apply {
            action = AlarmService.ACTION_STOP
        }
        context.startService(stopIntent)
    }
}
