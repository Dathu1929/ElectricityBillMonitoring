package com.example.electricitybillmonitoring.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electricitybillmonitoring.data.model.*
import com.example.electricitybillmonitoring.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _userState = MutableStateFlow<User?>(null)
    val userState: StateFlow<User?> = _userState

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun login(scNumber: String, mobile: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val user = authRepository.login(scNumber, mobile)
                if (user != null) {
                    _userState.value = user
                    onSuccess()
                } else {
                    _error.value = "Login failed"
                }
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Login failed"
            } finally {
                _loading.value = false
            }
        }
    }

    fun register(user: User, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                authRepository.register(user)
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Registration failed"
            } finally {
                _loading.value = false
            }
        }
    }

    fun resetPassword(email: String, mobile: String, newPass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val success = authRepository.resetPassword(email, mobile, newPass)
                if (success) onSuccess()
                else _error.value = "User not found"
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Reset failed"
            } finally {
                _loading.value = false
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _userState.value = null
    }
    
    fun updateProfile(user: User) {
        viewModelScope.launch {
            authRepository.updateProfile(user)
            _userState.value = user
        }
    }
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val billingRepository: BillingRepository,
    private val notificationRepository: NotificationRepository,
    private val settingsRepository: SettingsRepository,
    private val connectionRepository: ConnectionRepository,
    private val authRepository: AuthRepository,
    private val reminderRepository: ReminderRepository
) : ViewModel() {

    private val _summary = MutableStateFlow(DashboardSummary(0, 0.0))
    val summary: StateFlow<DashboardSummary> = _summary

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications

    private val _settings = MutableStateFlow(Settings())
    val settings: StateFlow<Settings> = _settings

    private val _connections = MutableStateFlow<List<ConsumerConnection>>(emptyList())
    val connections: StateFlow<List<ConsumerConnection>> = _connections

    init {
        loadData()
    }

    fun loadData() {
        val scNumber = getSCNumber()
        viewModelScope.launch {
            _summary.value = billingRepository.getDashboardSummary(scNumber)
            
            notificationRepository.getNotificationsFlow().collectLatest {
                _notifications.value = it
            }
        }
        viewModelScope.launch {
            val userId = getUserId()
            settingsRepository.getSettingsFlow(userId).collectLatest {
                if (it != null) _settings.value = it
            }
        }
        viewModelScope.launch {
            connectionRepository.getConnectionsFlow().collectLatest {
                _connections.value = it
            }
        }
        analyzePaymentPatterns()
    }

    private fun analyzePaymentPatterns() {
        viewModelScope.launch {
            val userId = getUserId()
            val bills = billingRepository.getBillsFlow().first()
            val payments = billingRepository.getPaymentsFlow().first()
            val settings = reminderRepository.getReminderSettingsFlow(userId).first() ?: ReminderSettings(userId = userId)

            if (payments.isEmpty()) return@launch

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            var totalDaysDiff = 0
            var count = 0

            for (payment in payments) {
                val bill = bills.find { it.id == payment.billId } ?: continue
                try {
                    val dueDate = sdf.parse(bill.dueDate)
                    val payDate = sdf.parse(payment.createdAt) // Assuming createdAt is yyyy-MM-dd
                    if (dueDate != null && payDate != null) {
                        val diff = dueDate.time - payDate.time
                        val daysBefore = (diff / (1000 * 60 * 60 * 24)).toInt()
                        totalDaysDiff += daysBefore
                        count++
                    }
                } catch (e: Exception) { }
            }

            if (count > 0) {
                val avgDaysBefore = totalDaysDiff / count
                val newFrequency = when {
                    avgDaysBefore > 7 -> "Every Week"
                    avgDaysBefore > 3 -> "Every 2 Days"
                    else -> "Every Day"
                }

                if (newFrequency != settings.reminderFrequency) {
                    reminderRepository.updateReminderSettings(settings.copy(reminderFrequency = newFrequency))
                    
                    // Add a notification about AI adjustment
                    notificationRepository.addNotification(Notification(
                        userId = userId,
                        title = "AI Smart Schedule",
                        message = "Based on your prompt payment history, we've adjusted your reminder frequency to $newFrequency.",
                        createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
                    ))
                }
            }
        }
    }

    fun markNotificationAsRead(id: Int) {
        viewModelScope.launch {
            notificationRepository.markRead(id)
        }
    }

    fun updateSettings(appName: String, currency: String, language: String, nEnabled: Boolean, dark: Boolean) {
        viewModelScope.launch {
            val currentSettings = _settings.value
            val newSettings = currentSettings.copy(
                appName = appName,
                currency = currency,
                language = language,
                notificationsEnabled = if (nEnabled) 1 else 0,
                darkMode = if (dark) 1 else 0
            )
            settingsRepository.updateSettings(newSettings)
        }
    }

    fun getUserName(): String = authRepository.getCurrentUser()?.fullName ?: "User"

    fun getSCNumber(): String = authRepository.getCurrentUser()?.serviceConnectionNumber ?: ""

    fun getMobileNumber(): String = authRepository.getCurrentUser()?.mobileNumber ?: ""

    fun getUserId(): Int = authRepository.getCurrentUser()?.id ?: 1
}

@HiltViewModel
class BillViewModel @Inject constructor(
    private val billingRepository: BillingRepository
) : ViewModel() {

    private val _bills = MutableStateFlow<List<Bill>>(emptyList())
    val bills: StateFlow<List<Bill>> = _bills

    init {
        loadBills()
    }

    fun loadBills() {
        viewModelScope.launch {
            billingRepository.getBillsFlow().collectLatest {
                _bills.value = it
            }
        }
    }

    fun addBill(bill: Bill) {
        viewModelScope.launch { billingRepository.addBill(bill) }
    }

    fun updateBill(bill: Bill) {
        viewModelScope.launch { billingRepository.updateBill(bill) }
    }

    fun deleteBill(bill: Bill) {
        viewModelScope.launch { billingRepository.deleteBill(bill) }
    }

    fun fetchBillsByConsumer(consumerNumber: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            billingRepository.fetchBillsByConsumer(consumerNumber)
            onComplete()
        }
    }
}

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val billingRepository: BillingRepository
) : ViewModel() {

    private val _payments = MutableStateFlow<List<Payment>>(emptyList())
    val payments: StateFlow<List<Payment>> = _payments

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    init {
        viewModelScope.launch {
            billingRepository.getPaymentsFlow().collectLatest {
                _payments.value = it
            }
        }
    }

    fun makePayment(billId: Int, amount: Double, method: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                billingRepository.payBill(billId, amount, method)
                onSuccess()
            } finally {
                _loading.value = false
            }
        }
    }
}

@HiltViewModel
class ConnectionViewModel @Inject constructor(
    private val connectionRepository: ConnectionRepository
) : ViewModel() {

    private val _connections = MutableStateFlow<List<ConsumerConnection>>(emptyList())
    val connections: StateFlow<List<ConsumerConnection>> = _connections

    private val _boards = MutableStateFlow<List<ElectricityBoard>>(emptyList())
    val boards: StateFlow<List<ElectricityBoard>> = _boards

    init {
        viewModelScope.launch {
            connectionRepository.getConnectionsFlow().collectLatest {
                _connections.value = it
            }
        }
        viewModelScope.launch {
            _boards.value = connectionRepository.getBoards()
        }
    }

    fun addConnection(
        userId: Int,
        boardId: Int,
        serviceNumber: String,
        consumerNumber: String,
        address: String,
        meterType: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            connectionRepository.addConnection(
                ConsumerConnection(
                    userId = userId,
                    boardId = boardId,
                    serviceNumber = serviceNumber,
                    consumerNumber = consumerNumber,
                    address = address,
                    meterType = meterType
                )
            )
            onSuccess()
        }
    }
}

@HiltViewModel
class ReminderViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _reminders = MutableStateFlow<List<Reminder>>(emptyList())
    val reminders: StateFlow<List<Reminder>> = _reminders

    private val _settings = MutableStateFlow(ReminderSettings())
    val settings: StateFlow<ReminderSettings> = _settings

    private val _history = MutableStateFlow<List<ReminderHistory>>(emptyList())
    val history: StateFlow<List<ReminderHistory>> = _history

    init {
        val userId = authRepository.getCurrentUser()?.id ?: 1
        viewModelScope.launch {
            reminderRepository.getRemindersFlow().collectLatest {
                _reminders.value = it
            }
        }
        viewModelScope.launch {
            reminderRepository.getReminderSettingsFlow(userId).collectLatest {
                if (it != null) _settings.value = it
            }
        }
        viewModelScope.launch {
            reminderRepository.getReminderHistoryFlow(userId).collectLatest {
                _history.value = it
            }
        }
    }

    fun updateSettings(settings: ReminderSettings, context: android.content.Context) {
        viewModelScope.launch {
            reminderRepository.updateReminderSettings(settings)
            val timeParts = settings.reminderTime.split(":")
            if (timeParts.size == 2) {
                val hour = timeParts[0].toIntOrNull() ?: 8
                val minute = timeParts[1].toIntOrNull() ?: 0
                com.example.electricitybillmonitoring.util.ReminderScheduler.scheduleDailyReminder(context, hour, minute)
            }
        }
    }

    fun sendTestReminder(context: android.content.Context) {
        val intent = android.content.Intent(context, com.example.electricitybillmonitoring.receiver.ReminderReceiver::class.java).apply {
            action = "com.example.electricitybillmonitoring.TEST_NOTIFICATION"
        }
        context.sendBroadcast(intent)
        android.widget.Toast.makeText(context, "Test reminder triggered!", android.widget.Toast.LENGTH_SHORT).show()
    }

    fun addReminder(reminder: Reminder) {
        viewModelScope.launch { reminderRepository.addReminder(reminder) }
    }

    fun clearHistory() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUser()?.id ?: 1
            reminderRepository.deleteHistory(userId)
        }
    }
}
