package com.example.electricitybillmonitoring.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.electricitybillmonitoring.data.model.Reminder
import com.example.electricitybillmonitoring.ui.viewmodel.ReminderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(viewModel: ReminderViewModel) {
    val reminders by viewModel.reminders.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val history by viewModel.history.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Active", "Settings", "History")

    Scaffold(
        topBar = {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Reminder")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                0 -> ActiveRemindersTab(reminders)
                1 -> ReminderSettingsTab(
                    settings = settings,
                    onSettingsChanged = { viewModel.updateSettings(it, context) },
                    onTestReminder = { viewModel.sendTestReminder(context) }
                )
                2 -> ReminderHistoryTab(history, onClearHistory = { viewModel.clearHistory() })
            }
        }
    }

    if (showAddDialog) {
        AddReminderDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { billId, date ->
                viewModel.addReminder(Reminder(billId = billId, reminderDate = date))
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ActiveRemindersTab(reminders: List<Reminder>) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            "Upcoming Reminders",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (reminders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No reminders set. Tap + to add one.")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(reminders) { reminder ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(text = "Bill ID: ${reminder.billId}", style = MaterialTheme.typography.titleMedium)
                                Text(text = "Remind on: ${reminder.reminderDate}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReminderSettingsTab(
    settings: com.example.electricitybillmonitoring.data.model.ReminderSettings,
    onSettingsChanged: (com.example.electricitybillmonitoring.data.model.ReminderSettings) -> Unit,
    onTestReminder: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Notification Preferences", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        
        ToggleSetting("Enable Notifications", settings.notificationEnabled) { 
            onSettingsChanged(settings.copy(notificationEnabled = it)) 
        }
        ToggleSetting("Email Reminders", settings.emailEnabled) { 
            onSettingsChanged(settings.copy(emailEnabled = it)) 
        }
        ToggleSetting("SMS Reminders", settings.smsEnabled) { 
            onSettingsChanged(settings.copy(smsEnabled = it)) 
        }
        ToggleSetting("Voice Reminders (TTS)", settings.voiceEnabled) { 
            onSettingsChanged(settings.copy(voiceEnabled = it)) 
        }

        HorizontalDivider()
        Text("Schedule & Language", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        DropdownSetting("Reminder Frequency", settings.reminderFrequency, listOf("Every Day", "Every 2 Days", "Every Week", "Only Before Due Date")) {
            onSettingsChanged(settings.copy(reminderFrequency = it))
        }

        DropdownSetting("Reminder Time", settings.reminderTime, listOf("06:00", "08:00", "10:00", "12:00", "15:00", "18:00", "20:00")) {
            onSettingsChanged(settings.copy(reminderTime = it))
        }

        DropdownSetting("Language", settings.language, listOf("English", "Telugu", "Tamil", "Hindi", "Kannada", "Malayalam", "Marathi", "Bengali", "Gujarati", "Punjabi", "Odia")) {
            onSettingsChanged(settings.copy(language = it))
        }

        ToggleSetting("Vibration", settings.vibration) {
            onSettingsChanged(settings.copy(vibration = it))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onTestReminder,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Icon(Icons.Default.Notifications, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Send Test Notification Now")
        }
    }
}

@Composable
fun ReminderHistoryTab(
    history: List<com.example.electricitybillmonitoring.data.model.ReminderHistory>,
    onClearHistory: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Delivery History", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            if (history.isNotEmpty()) {
                TextButton(onClick = onClearHistory) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (history.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No history available.")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(history) { item ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(item.reminderType, fontWeight = FontWeight.Bold)
                                Text(item.status, color = if (item.status == "Sent") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                            }
                            Text(item.sentTime, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ToggleSetting(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSetting(label: String, selectedValue: String, options: List<String>, onValueChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(label, style = MaterialTheme.typography.bodySmall)
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = selectedValue,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AddReminderDialog(onDismiss: () -> Unit, onConfirm: (Int, String) -> Unit) {
    var billId by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Manual Reminder") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = billId, onValueChange = { billId = it }, label = { Text("Bill ID") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                val bId = billId.toIntOrNull() ?: 0
                if (bId > 0 && date.isNotBlank()) onConfirm(bId, date)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
