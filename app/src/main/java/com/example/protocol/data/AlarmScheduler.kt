package com.example.protocol.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.protocol.alarm.AlarmReceiver
import java.util.Calendar

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleTask(task: Task) {
        if (!task.isActive) return

        val triggerTime = getNextTriggerTime(task) ?: return
        val pendingIntent = buildPendingIntent(task)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    fun cancelTask(task: Task) {
        val pendingIntent = buildPendingIntent(task)
        alarmManager.cancel(pendingIntent)
    }

    private fun buildPendingIntent(task: Task): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("TASK_ID", task.id)
            putExtra("TASK_TITLE", task.title)
            putExtra("TASK_DESC", task.description)
            putExtra("TASK_REPEAT", task.repeatType.name)
            putExtra("TASK_START_HOUR", task.startHour)
            putExtra("TASK_START_MIN", task.startMinute)
            putExtra("TASK_WEEK_DAYS", task.weekDays)
            putExtra("TASK_DAY_OF_MONTH", task.dayOfMonth)
        }
        return PendingIntent.getBroadcast(
            context,
            task.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun getNextTriggerTime(task: Task): Long? {
        val now = Calendar.getInstance()
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, task.startHour)
            set(Calendar.MINUTE, task.startMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return when (task.repeatType) {
            RepeatType.DAILY -> {
                if (cal.timeInMillis <= now.timeInMillis) {
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                }
                cal.timeInMillis
            }
            RepeatType.WEEKLY -> {
                val days = task.weekDays.split(",").mapNotNull { it.trim().toIntOrNull() }
                if (days.isEmpty()) return null
                findNextWeekDay(now, cal, days)
            }
            RepeatType.MONTHLY -> {
                cal.set(Calendar.DAY_OF_MONTH, task.dayOfMonth)
                if (cal.timeInMillis <= now.timeInMillis) {
                    cal.add(Calendar.MONTH, 1)
                }
                cal.timeInMillis
            }
            RepeatType.NONE -> {
                if (cal.timeInMillis <= now.timeInMillis) null else cal.timeInMillis
            }
        }
    }

    private fun findNextWeekDay(now: Calendar, cal: Calendar, days: List<Int>): Long? {
        // days: 1=Monday, 2=Tuesday, ... 7=Sunday (Calendar.MONDAY=2 ... Calendar.SUNDAY=1)
        val calDayMap = mapOf(1 to Calendar.MONDAY, 2 to Calendar.TUESDAY,
            3 to Calendar.WEDNESDAY, 4 to Calendar.THURSDAY, 5 to Calendar.FRIDAY,
            6 to Calendar.SATURDAY, 7 to Calendar.SUNDAY)

        for (offset in 0..7) {
            val candidate = cal.clone() as Calendar
            candidate.add(Calendar.DAY_OF_YEAR, offset)
            val dayOfWeek = candidate.get(Calendar.DAY_OF_WEEK)
            val targetDays = days.mapNotNull { calDayMap[it] }
            if (dayOfWeek in targetDays && candidate.timeInMillis > now.timeInMillis) {
                return candidate.timeInMillis
            }
        }
        return null
    }
}
