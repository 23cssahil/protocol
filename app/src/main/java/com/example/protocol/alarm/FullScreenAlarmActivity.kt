package com.example.protocol.alarm

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

class FullScreenAlarmActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show over lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val taskTitle = intent.getStringExtra("TASK_TITLE") ?: "Task Reminder"
        val taskDesc = intent.getStringExtra("TASK_DESC") ?: ""

        setContent {
            AlarmScreen(
                taskTitle = taskTitle,
                taskDesc = taskDesc,
                onDismiss = {
                    sendBroadcast(Intent(this, DismissReceiver::class.java))
                    finish()
                }
            )
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Block back button
    }
}

@Composable
fun AlarmScreen(taskTitle: String, taskDesc: String, onDismiss: () -> Unit) {
    val currentTime = remember {
        SimpleDateFormat("hh:mm", Locale.getDefault()).format(Date())
    }
    val amPm = remember {
        SimpleDateFormat("a", Locale.getDefault()).format(Date())
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFF8F0),
                        Color(0xFFFFEDD5),
                        Color(0xFFFED7AA)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top: Time Display
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(48.dp))
                Text(
                    text = currentTime,
                    fontSize = 80.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7C2D12),
                    letterSpacing = (-2).sp
                )
                Text(
                    text = amPm,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFB45309)
                )
            }

            // Middle: Task Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.7f))
                    .padding(32.dp)
            ) {
                Text(
                    text = "⏰",
                    fontSize = 48.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = taskTitle,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7C2D12),
                    textAlign = TextAlign.Center
                )
                if (taskDesc.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = taskDesc,
                        fontSize = 16.sp,
                        color = Color(0xFF92400E),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Auto-stops in 1 minute",
                    fontSize = 13.sp,
                    color = Color(0xFFB45309).copy(alpha = 0.7f)
                )
            }

            // Bottom: OK Button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7C2D12)
                    ),
                    shape = CircleShape
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "✓", fontSize = 32.sp, color = Color.White)
                        Text(text = "OK", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Press OK to dismiss\nOr it will snooze for 10 min",
                    fontSize = 13.sp,
                    color = Color(0xFF92400E).copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
