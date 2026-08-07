package com.example.electricitybillmonitoring.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import com.example.electricitybillmonitoring.data.local.BillDao
import com.example.electricitybillmonitoring.data.local.ReminderDao
import com.example.electricitybillmonitoring.data.local.UserDao
import com.example.electricitybillmonitoring.data.model.ReminderHistory
import com.example.electricitybillmonitoring.util.NotificationUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject lateinit var billDao: BillDao
    @Inject lateinit var reminderDao: ReminderDao
    @Inject lateinit var userDao: UserDao

    private var tts: TextToSpeech? = null

    override fun onReceive(context: Context, intent: Intent) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            if (intent.action == "com.example.electricitybillmonitoring.TEST_NOTIFICATION") {
                showTestNotification(context)
                return@launch
            }

            val bills = billDao.getAllBills()
            val pendingBills = bills.filter { it.status == "pending" || it.status == "overdue" }
            
            if (pendingBills.isEmpty()) return@launch

            // Assuming user ID 1 for simplicity in this demo, or we could fetch from settings
            val settings = reminderDao.getReminderSettingsFlow(1).first() ?: return@launch
            
            // Re-schedule for the next day
            reschedule(context, settings.reminderTime)

            if (!settings.notificationEnabled) return@launch

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = Calendar.getInstance()

            for (bill in pendingBills) {
                try {
                    val dueDate = Calendar.getInstance().apply {
                        time = sdf.parse(bill.dueDate) ?: return@apply
                    }
                    
                    val diff = dueDate.timeInMillis - today.timeInMillis
                    val daysRemaining = (diff / (1000 * 60 * 60 * 24)).toInt()

                    val shouldRemind = when (settings.reminderFrequency) {
                        "Every Day" -> true
                        "Every 2 Days" -> daysRemaining % 2 == 0
                        "Every Week" -> daysRemaining % 7 == 0
                        "Only Before Due Date" -> daysRemaining >= 0
                        else -> true
                    }

                    if (shouldRemind || daysRemaining <= 0) { // Always remind if overdue
                        // AI logic: If overdue for more than 5 days, use a stronger message
                        val isLatePayer = daysRemaining < -5
                        val message = when {
                            daysRemaining > 0 -> "Your bill of ₹${bill.amountDue} is due in $daysRemaining days (${bill.dueDate})."
                            daysRemaining == 0 -> "URGENT: Your bill of ₹${bill.amountDue} is due TODAY!"
                            isLatePayer -> "CRITICAL: You have missed the due date by ${-daysRemaining} days. Pay TODAY to avoid disconnection!"
                            else -> "Warning: Your bill of ₹${bill.amountDue} is OVERDUE by ${-daysRemaining} days!"
                        }

                        NotificationUtils.showNotification(
                            context,
                            "Electricity Bill Reminder",
                            message,
                            bill.id
                        )

                        reminderDao.insertReminderHistory(
                            ReminderHistory(
                                userId = 1,
                                reminderType = "Push Notification",
                                sentTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
                                status = "Sent"
                            )
                        )

                        if (settings.voiceEnabled) {
                            speak(context, message, settings.language)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun reschedule(context: Context, time: String) {
        val timeParts = time.split(":")
        if (timeParts.size == 2) {
            val hour = timeParts[0].toIntOrNull() ?: 8
            val minute = timeParts[1].toIntOrNull() ?: 0
            com.example.electricitybillmonitoring.util.ReminderScheduler.scheduleDailyReminder(context, hour, minute)
        }
    }

    private suspend fun showTestNotification(context: Context) {
        val settings = reminderDao.getReminderSettingsFlow(1).first() ?: return
        val message = "This is a test bill reminder. Your notification and TTS settings are working correctly!"
        
        NotificationUtils.showNotification(
            context,
            "Test Reminder",
            message,
            999
        )

        if (settings.voiceEnabled) {
            speak(context, message, settings.language)
        }
    }

    private fun speak(context: Context, text: String, language: String) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val locale = when (language.lowercase()) {
                    "hindi" -> Locale("hi", "IN")
                    "tamil" -> Locale("ta", "IN")
                    "telugu" -> Locale("te", "IN")
                    "kannada" -> Locale("kn", "IN")
                    "malayalam" -> Locale("ml", "IN")
                    "marathi" -> Locale("mr", "IN")
                    "bengali" -> Locale("bn", "IN")
                    "gujarati" -> Locale("gu", "IN")
                    "punjabi" -> Locale("pa", "IN")
                    "odia" -> Locale("or", "IN")
                    else -> Locale.US
                }
                tts?.language = locale
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }
    }
}
