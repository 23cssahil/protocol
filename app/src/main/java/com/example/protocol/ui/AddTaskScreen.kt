package com.example.protocol.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.protocol.data.RepeatType
import com.example.protocol.data.Task

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    existingTask: Task? = null,
    onSave: (title: String, desc: String,
             startH: Int, startM: Int,
             endH: Int, endM: Int,
             repeat: RepeatType, weekDays: String, dayOfMonth: Int) -> Unit,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf(existingTask?.title ?: "") }
    var description by remember { mutableStateOf(existingTask?.description ?: "") }

    var startHour by remember { mutableIntStateOf(existingTask?.startHour ?: 6) }
    var startMinute by remember { mutableIntStateOf(existingTask?.startMinute ?: 0) }
    var endHour by remember { mutableIntStateOf(existingTask?.endHour ?: 8) }
    var endMinute by remember { mutableIntStateOf(existingTask?.endMinute ?: 0) }

    var repeatType by remember { mutableStateOf(existingTask?.repeatType ?: RepeatType.DAILY) }
    var weekDays by remember { mutableStateOf(existingTask?.weekDays ?: "1,2,3,4,5") }
    var dayOfMonth by remember { mutableIntStateOf(existingTask?.dayOfMonth ?: 1) }

    var titleError by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (existingTask == null) "Naya Task" else "Task Edit Karo",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7C2D12)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color(0xFF7C2D12))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFFF8F0))
            )
        },
        containerColor = Color(0xFFFFF8F0)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Title
            SectionCard {
                Text("📝 Task ka naam", fontWeight = FontWeight.SemiBold, color = Color(0xFF374151))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; titleError = false },
                    label = { Text("e.g. Backlog Subject 1 + Theory") },
                    isError = titleError,
                    supportingText = if (titleError) {{ Text("Title zaroori hai", color = Color.Red) }} else null,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Description
            SectionCard {
                Text("📄 Activity / Description", fontWeight = FontWeight.SemiBold, color = Color(0xFF374151))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("e.g. Chapter 5 complete karna") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    colors = textFieldColors(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Time Range
            SectionCard {
                Text("⏰ Time Range", fontWeight = FontWeight.SemiBold, color = Color(0xFF374151))
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Start time
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Shuru", fontSize = 12.sp, color = Color(0xFF6B7280))
                        Spacer(modifier = Modifier.height(4.dp))
                        TimePickerBox(
                            hour = startHour,
                            minute = startMinute,
                            onClick = { showStartTimePicker = true }
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Khatam", fontSize = 12.sp, color = Color(0xFF6B7280))
                        Spacer(modifier = Modifier.height(4.dp))
                        TimePickerBox(
                            hour = endHour,
                            minute = endMinute,
                            onClick = { showEndTimePicker = true }
                        )
                    }
                }
            }

            // Repeat
            SectionCard {
                Text("🔁 Repetition", fontWeight = FontWeight.SemiBold, color = Color(0xFF374151))
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RepeatType.entries.forEach { type ->
                        val selected = repeatType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) Color(0xFF7C2D12) else Color(0xFFF3F4F6))
                                .clickable { repeatType = type }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (type) {
                                    RepeatType.DAILY -> "Daily"
                                    RepeatType.WEEKLY -> "Weekly"
                                    RepeatType.MONTHLY -> "Monthly"
                                    RepeatType.NONE -> "Once"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selected) Color.White else Color(0xFF374151)
                            )
                        }
                    }
                }

                // Weekly - day selector
                if (repeatType == RepeatType.WEEKLY) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Din select karo:", fontSize = 13.sp, color = Color(0xFF6B7280))
                    Spacer(modifier = Modifier.height(8.dp))
                    WeekDaySelector(
                        selectedDays = weekDays.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet(),
                        onSelectionChange = { days ->
                            weekDays = days.sorted().joinToString(",")
                        }
                    )
                }

                // Monthly - day of month
                if (repeatType == RepeatType.MONTHLY) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Mahine ki tarikh:", fontSize = 13.sp, color = Color(0xFF6B7280))
                    Spacer(modifier = Modifier.height(8.dp))
                    DayOfMonthPicker(
                        selectedDay = dayOfMonth,
                        onDaySelected = { dayOfMonth = it }
                    )
                }
            }

            // Save button
            Button(
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                        return@Button
                    }
                    onSave(title, description, startHour, startMinute, endHour, endMinute, repeatType, weekDays, dayOfMonth)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C2D12)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (existingTask == null) "✅ Task Save Karo" else "✅ Update Karo",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Time pickers
    if (showStartTimePicker) {
        TimePickerDialog(
            initialHour = startHour,
            initialMinute = startMinute,
            onDismiss = { showStartTimePicker = false },
            onConfirm = { h, m -> startHour = h; startMinute = m; showStartTimePicker = false }
        )
    }
    if (showEndTimePicker) {
        TimePickerDialog(
            initialHour = endHour,
            initialMinute = endMinute,
            onDismiss = { showEndTimePicker = false },
            onConfirm = { h, m -> endHour = h; endMinute = m; showEndTimePicker = false }
        )
    }
}

@Composable
fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun TimePickerBox(hour: Int, minute: Int, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFED7AA))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = formatTime(hour, minute),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF7C2D12)
        )
    }
}

@Composable
fun WeekDaySelector(selectedDays: Set<Int>, onSelectionChange: (Set<Int>) -> Unit) {
    val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        dayLabels.forEachIndexed { index, label ->
            val day = index + 1
            val selected = day in selectedDays
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selected) Color(0xFF7C2D12) else Color(0xFFF3F4F6))
                    .clickable {
                        val newDays = selectedDays.toMutableSet()
                        if (selected) newDays.remove(day) else newDays.add(day)
                        onSelectionChange(newDays)
                    }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (selected) Color.White else Color(0xFF6B7280)
                )
            }
        }
    }
}

@Composable
fun DayOfMonthPicker(selectedDay: Int, onDaySelected: (Int) -> Unit) {
    var day by remember { mutableIntStateOf(selectedDay) }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        IconButton(onClick = { if (day > 1) { day--; onDaySelected(day) } }) {
            Text("◀", fontSize = 20.sp)
        }
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF7C2D12)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "$day", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        IconButton(onClick = { if (day < 28) { day++; onDaySelected(day) } }) {
            Text("▶", fontSize = 20.sp)
        }
        Text("Tarikh har mahine", fontSize = 13.sp, color = Color(0xFF6B7280))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    val state = rememberTimePickerState(initialHour = initialHour, initialMinute = initialMinute, is24Hour = false)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Time select karo") },
        text = {
            TimePicker(state = state)
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) {
                Text("OK", color = Color(0xFF7C2D12), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color(0xFF7C2D12),
    focusedLabelColor = Color(0xFF7C2D12),
    cursorColor = Color(0xFF7C2D12)
)
