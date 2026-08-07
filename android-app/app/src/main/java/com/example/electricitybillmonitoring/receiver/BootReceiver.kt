package com.example.electricitybillmonitoring.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.electricitybillmonitoring.data.local.ReminderDao
import com.example.electricitybillmonitoring.util.ReminderScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    
    @Inject lateinit var reminderDao: ReminderDao

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                val settings = reminderDao.getReminderSettingsFlow(1).first()
                if (settings != null) {
                    val timeParts = settings.reminderTime.split(":")
                    if (timeParts.size == 2) {
                        val hour = timeParts[0].toIntOrNull() ?: 8
                        val minute = timeParts[1].toIntOrNull() ?: 0
                        ReminderScheduler.scheduleDailyReminder(context, hour, minute)
                    }
                } else {
                    ReminderScheduler.scheduleDailyReminder(context, 8, 0)
                }
            }
        }
    }
}
