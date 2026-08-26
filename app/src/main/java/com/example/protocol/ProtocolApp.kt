package com.example.protocol

import android.app.Application
import com.example.protocol.data.AppDatabase
import com.example.protocol.data.AlarmScheduler

class ProtocolApp : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val alarmScheduler by lazy { AlarmScheduler(this) }
}
