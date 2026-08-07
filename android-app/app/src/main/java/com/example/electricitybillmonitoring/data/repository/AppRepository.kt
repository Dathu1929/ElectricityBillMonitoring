package com.example.electricitybillmonitoring.data.repository

import com.example.electricitybillmonitoring.data.local.*
import com.example.electricitybillmonitoring.data.model.*
import com.example.electricitybillmonitoring.data.network.ApiService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val userDao: UserDao
) {
    private var currentUser: User? = null

    fun getCurrentUser(): User? = currentUser

    suspend fun login(scNumber: String, mobile: String): User? {
        try {
            val response = apiService.login(mapOf("username" to scNumber, "password" to mobile))
            currentUser = response.user
            userDao.insertUser(response.user)
            return response.user
        } catch (e: Exception) {
            var user = userDao.getUserBySCAndMobile(scNumber, mobile)
            if (user == null) {
                val newUser = User(
                    fullName = "User",
                    mobileNumber = mobile,
                    serviceConnectionNumber = scNumber
                )
                val id = userDao.insertUser(newUser)
                user = newUser.copy(id = id.toInt())
            }
            currentUser = user
            return user
        }
    }

    suspend fun register(user: User): Long {
        try {
            apiService.register(mapOf(
                "full_name" to user.fullName,
                "email" to user.email,
                "mobile_number" to user.mobileNumber,
                "password" to user.password,
                "role" to user.role
            ))
        } catch (e: Exception) {}
        return userDao.insertUser(user)
    }

    suspend fun resetPassword(email: String, mobile: String, newPassword: String): Boolean {
        val user = if (email.isNotBlank()) userDao.getUserBySCNumber(email) else userDao.getUserByMobile(mobile)
        if (user != null) {
            userDao.updateUser(user.copy(password = newPassword))
            return true
        }
        return false
    }

    fun logout() {
        currentUser = null
    }

    suspend fun updateProfile(user: User) {
        try {
            apiService.updateSettings(Settings(
                appName = user.fullName,
                currency = "INR",
                language = "en",
                notificationsEnabled = 1,
                darkMode = 0
            ))
        } catch (e: Exception) {}
        userDao.updateUser(user)
        currentUser = user
    }
}

@Singleton
class BillingRepository @Inject constructor(
    private val apiService: ApiService,
    private val billDao: BillDao,
    private val paymentDao: PaymentDao
) {
    fun getBillsFlow(): Flow<List<Bill>> = billDao.getAllBillsFlow()

    fun getBillsBySCNumberFlow(scNumber: String): Flow<List<Bill>> =
        billDao.getBillsBySCNumberFlow(scNumber)

    suspend fun fetchBillsByConsumer(consumerNumber: String) {
        try {
            val response = apiService.getBillsByConsumer(consumerNumber)
            val list = response["bills"] ?: emptyList()
            list.forEach { billDao.insertBill(it) }
        } catch (e: Exception) {}
    }

    suspend fun getDashboardSummary(scNumber: String): DashboardSummary {
        try {
            val response = apiService.getDashboard()
            try {
                val billsRes = apiService.getBills()
                val list = billsRes["bills"] ?: emptyList()
                list.forEach { billDao.insertBill(it) }
            } catch (e: Exception) {}
            return response.dashboard
        } catch (e: Exception) {
            val bills = billDao.getBillsBySCNumber(scNumber)
            val pendingBills = bills.filter { it.status.lowercase() == "pending" }
            return DashboardSummary(
                totalBills = bills.size,
                totalDue = pendingBills.sumOf { it.totalAmountRs }
            )
        }
    }

    suspend fun addBill(bill: Bill) {
        billDao.insertBill(bill)
    }

    suspend fun updateBill(bill: Bill) {
        billDao.updateBill(bill)
    }

    suspend fun deleteBill(bill: Bill) {
        billDao.deleteBill(bill)
    }

    fun getPaymentsFlow(): Flow<List<Payment>> = paymentDao.getAllPaymentsFlow()

    suspend fun payBill(billId: Int, amount: Double, method: String) {
        try {
            apiService.createPayment(mapOf(
                "bill_id" to billId,
                "amount" to amount,
                "method" to method
            ))
        } catch (e: Exception) {}

        val bill = billDao.getBillById(billId)
        if (bill != null) {
            val updatedPaidAmount = bill.amountPaid + amount
            val newStatus = if (updatedPaidAmount >= bill.amountDue) "paid" else "pending"
            billDao.updateBill(bill.copy(amountPaid = updatedPaidAmount, status = newStatus))
            
            paymentDao.insertPayment(Payment(
                billId = billId,
                amount = amount,
                method = method,
                createdAt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            ))
        }
    }
}

@Singleton
class ConnectionRepository @Inject constructor(
    private val apiService: ApiService,
    private val connectionDao: ConnectionDao
) {
    fun getConnectionsFlow(): Flow<List<ConsumerConnection>> = connectionDao.getAllConnectionsFlow()

    suspend fun addConnection(connection: ConsumerConnection) {
        try {
            apiService.createConnection(mapOf(
                "board_id" to connection.boardId,
                "service_number" to connection.serviceNumber,
                "consumer_number" to connection.consumerNumber,
                "address" to connection.address,
                "meter_type" to connection.meterType
            ))
        } catch (e: Exception) {}
        connectionDao.insertConnection(connection)
    }

    suspend fun getBoards(): List<ElectricityBoard> {
        try {
            val boardsRes = apiService.getBoards()
            val boardsList = boardsRes["boards"] ?: emptyList()
            if (boardsList.isNotEmpty()) {
                boardsList.forEach { connectionDao.insertBoard(it) }
                return boardsList
            }
        } catch (e: Exception) {}

        val boards = connectionDao.getBoards()
        if (boards.isEmpty()) {
            val initialBoards = listOf(
                ElectricityBoard(name = "APCPDCL", code = "AP01", state = "Andhra Pradesh"),
                ElectricityBoard(name = "APEPDCL", code = "AP02", state = "Andhra Pradesh"),
                ElectricityBoard(name = "APDCL", code = "AS01", state = "Assam"),
                ElectricityBoard(name = "NBPDCL", code = "BR01", state = "Bihar"),
                ElectricityBoard(name = "SBPDCL", code = "BR02", state = "Bihar"),
                ElectricityBoard(name = "CSPDCL", code = "CG01", state = "Chhattisgarh"),
                ElectricityBoard(name = "TPDDL", code = "DL01", state = "Delhi"),
                ElectricityBoard(name = "BSES Rajdhani", code = "DL02", state = "Delhi"),
                ElectricityBoard(name = "BSES Yamuna", code = "DL03", state = "Delhi"),
                ElectricityBoard(name = "DGVCL", code = "GJ01", state = "Gujarat"),
                ElectricityBoard(name = "MGVCL", code = "GJ02", state = "Gujarat"),
                ElectricityBoard(name = "PGVCL", code = "GJ03", state = "Gujarat"),
                ElectricityBoard(name = "UGVCL", code = "GJ04", state = "Gujarat"),
                ElectricityBoard(name = "DHBVN", code = "HR01", state = "Haryana"),
                ElectricityBoard(name = "UHBVN", code = "HR02", state = "Haryana"),
                ElectricityBoard(name = "HPSEB", code = "HP01", state = "Himachal Pradesh"),
                ElectricityBoard(name = "JBVNL", code = "JH01", state = "Jharkhand"),
                ElectricityBoard(name = "BESCOM", code = "KA01", state = "Karnataka"),
                ElectricityBoard(name = "HESCOM", code = "KA02", state = "Karnataka"),
                ElectricityBoard(name = "GESCOM", code = "KA03", state = "Karnataka"),
                ElectricityBoard(name = "MESCOM", code = "KA04", state = "Karnataka"),
                ElectricityBoard(name = "KSEB", code = "KL01", state = "Kerala"),
                ElectricityBoard(name = "MPPKVVCL", code = "MP01", state = "Madhya Pradesh"),
                ElectricityBoard(name = "MAHADISCOM", code = "MH01", state = "Maharashtra"),
                ElectricityBoard(name = "BEST", code = "MH02", state = "Maharashtra"),
                ElectricityBoard(name = "Adani Electricity", code = "MH03", state = "Maharashtra"),
                ElectricityBoard(name = "TPCODL", code = "OR01", state = "Odisha"),
                ElectricityBoard(name = "PSPCL", code = "PB01", state = "Punjab"),
                ElectricityBoard(name = "JVVNL", code = "RJ01", state = "Rajasthan"),
                ElectricityBoard(name = "AVVNL", code = "RJ02", state = "Rajasthan"),
                ElectricityBoard(name = "JDVVNL", code = "RJ03", state = "Rajasthan"),
                ElectricityBoard(name = "TANGEDCO", code = "TN01", state = "Tamil Nadu"),
                ElectricityBoard(name = "TSSPDCL", code = "TG01", state = "Telangana"),
                ElectricityBoard(name = "TSNPDCL", code = "TG02", state = "Telangana"),
                ElectricityBoard(name = "UPPCL", code = "UP01", state = "Uttar Pradesh"),
                ElectricityBoard(name = "BOARD_PVVNL", code = "UP02", state = "Uttar Pradesh"),
                ElectricityBoard(name = "WBSEDCL", code = "WB01", state = "West Bengal"),
                ElectricityBoard(name = "CESC", code = "WB02", state = "West Bengal")
            )
            initialBoards.forEach { connectionDao.insertBoard(it) }
            return initialBoards
        }
        return boards
    }
}

@Singleton
class NotificationRepository @Inject constructor(
    private val apiService: ApiService,
    private val notificationDao: NotificationDao
) {
    fun getNotificationsFlow(): Flow<List<Notification>> = notificationDao.getAllNotificationsFlow()

    suspend fun addNotification(notification: Notification) {
        notificationDao.insertNotification(notification)
    }

    suspend fun markRead(id: Int) {
        try {
            apiService.markNotificationRead(mapOf("id" to id))
        } catch (e: Exception) {}
        notificationDao.markAsRead(id)
    }
}

@Singleton
class SettingsRepository @Inject constructor(
    private val apiService: ApiService,
    private val settingsDao: SettingsDao
) {
    fun getSettingsFlow(userId: Int): Flow<Settings?> = settingsDao.getSettingsFlow(userId)

    suspend fun updateSettings(settings: Settings) {
        try {
            apiService.updateSettings(settings)
        } catch (e: Exception) {}
        settingsDao.insertSettings(settings)
    }
}

@Singleton
class ReminderRepository @Inject constructor(
    private val reminderDao: ReminderDao
) {
    fun getRemindersFlow(): Flow<List<Reminder>> = reminderDao.getAllRemindersFlow()

    suspend fun addReminder(reminder: Reminder) {
        reminderDao.insertReminder(reminder)
    }

    fun getReminderSettingsFlow(userId: Int): Flow<ReminderSettings?> =
        reminderDao.getReminderSettingsFlow(userId)

    suspend fun updateReminderSettings(settings: ReminderSettings) {
        reminderDao.insertReminderSettings(settings)
    }

    suspend fun addReminderHistory(history: ReminderHistory) {
        reminderDao.insertReminderHistory(history)
    }

    fun getReminderHistoryFlow(userId: Int): Flow<List<ReminderHistory>> =
        reminderDao.getReminderHistoryFlow(userId)

    suspend fun deleteHistory(userId: Int) {
        reminderDao.deleteHistoryByUserId(userId)
    }
}
