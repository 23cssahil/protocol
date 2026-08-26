package com.example.protocol.alarm

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.*
import androidx.core.app.NotificationCompat
import com.example.protocol.R

class AlarmService : Service() {

    companion object {
        const val ACTION_STOP = "com.example.protocol.ACTION_STOP"
        const val CHANNEL_ID = "protocol_alarm_channel"
        const val NOTIF_ID = 1001
    }

    private var ringtone: android.media.Ringtone? = null
    private var vibrator: Vibrator? = null
    private val autoStopHandler = Handler(Looper.getMainLooper())

    private var taskId: Int = -1
    private var taskTitle: String = ""
    private var taskDesc: String = ""
    private var taskRepeat: String = "DAILY"
    private var taskStartHour: Int = 6
    private var taskStartMin: Int = 0
    private var taskWeekDays: String = "1,2,3,4,5"
    private var taskDayOfMonth: Int = 1

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopAlarm()
            return START_NOT_STICKY
        }

        taskId = intent?.getIntExtra("TASK_ID", -1) ?: -1
        taskTitle = intent?.getStringExtra("TASK_TITLE") ?: "Task Reminder"
        taskDesc = intent?.getStringExtra("TASK_DESC") ?: ""
        taskRepeat = intent?.getStringExtra("TASK_REPEAT") ?: "DAILY"
        taskStartHour = intent?.getIntExtra("TASK_START_HOUR", 6) ?: 6
        taskStartMin = intent?.getIntExtra("TASK_START_MIN", 0) ?: 0
        taskWeekDays = intent?.getStringExtra("TASK_WEEK_DAYS") ?: "1,2,3,4,5"
        taskDayOfMonth = intent?.getIntExtra("TASK_DAY_OF_MONTH", 1) ?: 1

        startForeground(NOTIF_ID, buildNotification())
        launchFullScreenActivity()
        startRingtone()
        startVibration()

        // Auto-stop after 1 minute, then schedule 10-min snooze
        autoStopHandler.postDelayed({
            stopAlarm()
            scheduleSnooze()
        }, 60_000L)

        return START_NOT_STICKY
    }

    private fun launchFullScreenActivity() {
        val fullScreenIntent = Intent(this, FullScreenAlarmActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION)
            putExtra("TASK_ID", taskId)
            putExtra("TASK_TITLE", taskTitle)
            putExtra("TASK_DESC", taskDesc)
        }
        startActivity(fullScreenIntent)
    }

    private fun startRingtone() {
        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        ringtone = RingtoneManager.getRingtone(applicationContext, alarmUri)
        ringtone?.audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        ringtone?.isLooping = true
        ringtone?.play()
    }

    private fun startVibration() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        val pattern = longArrayOf(0, 800, 400, 800, 400, 800, 400)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    private fun scheduleSnooze() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val snoozeIntent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("TASK_ID", taskId)
            putExtra("TASK_TITLE", taskTitle)
            putExtra("TASK_DESC", taskDesc)
            putExtra("TASK_REPEAT", taskRepeat)
            putExtra("TASK_START_HOUR", taskStartHour)
            putExtra("TASK_START_MIN", taskStartMin)
            putExtra("TASK_WEEK_DAYS", taskWeekDays)
            putExtra("TASK_DAY_OF_MONTH", taskDayOfMonth)
        }
        val pi = PendingIntent.getBroadcast(
            this,
            taskId + 50000,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val snoozeTime = System.currentTimeMillis() + 10 * 60 * 1000L
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, snoozeTime, pi)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, snoozeTime, pi)
        }
    }

    private fun stopAlarm() {
        autoStopHandler.removeCallbacksAndMessages(null)
        ringtone?.stop()
        vibrator?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildNotification(): Notification {
        val dismissIntent = Intent(this, DismissReceiver::class.java)
        val dismissPi = PendingIntent.getBroadcast(
            this, 0, dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val fullScreenIntent = Intent(this, FullScreenAlarmActivity::class.java).apply {
            putExtra("TASK_ID", taskId)
            putExtra("TASK_TITLE", taskTitle)
            putExtra("TASK_DESC", taskDesc)
        }
        val fullScreenPi = PendingIntent.getActivity(
            this, taskId, fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("⏰ $taskTitle")
            .setContentText(if (taskDesc.isNotEmpty()) taskDesc else "Tap OK to dismiss")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(fullScreenPi, true)
            .addAction(android.R.drawable.checkbox_on_background, "✅ OK - Dismiss", dismissPi)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Protocol Alarm",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Task reminders with alarm sound"
                setBypassDnd(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                enableVibration(false) // We handle vibration manually
                enableLights(true)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
