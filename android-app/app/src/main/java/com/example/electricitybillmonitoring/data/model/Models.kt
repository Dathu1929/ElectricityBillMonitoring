package com.example.electricitybillmonitoring.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "full_name") val fullName: String,
    @ColumnInfo(name = "mobile_number") val mobileNumber: String,
    @ColumnInfo(name = "service_connection_number") val serviceConnectionNumber: String = "",
    val email: String = "",
    val password: String = "",
    @ColumnInfo(name = "consumer_number") val consumerNumber: String = "",
    @ColumnInfo(name = "meter_number") val meterNumber: String = "",
    val state: String = "",
    val board: String = "",
    val address: String = "",
    val role: String = "consumer"
)

data class LoginResponse(
    val message: String,
    val user: User,
    val token: String
)

@Entity(tableName = "electricity_boards")
data class ElectricityBoard(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val code: String,
    val state: String,
    val country: String = "India",
    @SerializedName("connector_class") @ColumnInfo(name = "connector_class") val connectorClass: String = ""
)

@Entity(tableName = "consumer_connections")
data class ConsumerConnection(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @SerializedName("user_id") @ColumnInfo(name = "user_id") val userId: Int,
    @SerializedName("board_id") @ColumnInfo(name = "board_id") val boardId: Int,
    @SerializedName("service_number") @ColumnInfo(name = "service_number") val serviceNumber: String,
    @SerializedName("consumer_number") @ColumnInfo(name = "consumer_number") val consumerNumber: String,
    val address: String,
    @SerializedName("meter_type") @ColumnInfo(name = "meter_type") val meterType: String,
    @SerializedName("connection_status") @ColumnInfo(name = "connection_status") val connectionStatus: String = "active"
)

@Entity(tableName = "bills")
data class Bill(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "service_connection_number") val serviceConnectionNumber: String,
    @ColumnInfo(name = "eb_board") val ebBoard: String,
    @ColumnInfo(name = "full_name") val fullName: String,
    @ColumnInfo(name = "mobile_number") val mobileNumber: String,
    @ColumnInfo(name = "billing_month") val billingMonth: String,
    val units: Double,
    @ColumnInfo(name = "total_amount_rs") val totalAmountRs: Double,
    @ColumnInfo(name = "amount_due") val amountDue: Double = 0.0,
    @ColumnInfo(name = "amount_paid") val amountPaid: Double = 0.0,
    @ColumnInfo(name = "due_date") val dueDate: String,
    val status: String = "Pending" // Paid / Pending
)

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @SerializedName("bill_id") @ColumnInfo(name = "bill_id") val billId: Int,
    val amount: Double,
    val method: String,
    @SerializedName("gateway_reference") @ColumnInfo(name = "gateway_reference") val gatewayReference: String? = "dummy_ref",
    val status: String = "captured",
    @SerializedName("created_at") @ColumnInfo(name = "created_at") val createdAt: String = ""
)

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @SerializedName("user_id") @ColumnInfo(name = "user_id") val userId: Int,
    val title: String,
    val message: String,
    @SerializedName("is_read") @ColumnInfo(name = "is_read") val isRead: Int = 0,
    @SerializedName("created_at") @ColumnInfo(name = "created_at") val createdAt: String = ""
)

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "bill_id") val billId: Int,
    @ColumnInfo(name = "reminder_date") val reminderDate: String,
    @ColumnInfo(name = "notification_status") val notificationStatus: Int = 0
)

@Entity(tableName = "reminder_settings")
data class ReminderSettings(
    @PrimaryKey val userId: Int = 1,
    @ColumnInfo(name = "notification_enabled") val notificationEnabled: Boolean = true,
    @ColumnInfo(name = "email_enabled") val emailEnabled: Boolean = false,
    @ColumnInfo(name = "sms_enabled") val smsEnabled: Boolean = false,
    @ColumnInfo(name = "voice_enabled") val voiceEnabled: Boolean = false,
    @ColumnInfo(name = "reminder_time") val reminderTime: String = "08:00",
    val language: String = "en",
    val theme: String = "Blue",
    val vibration: Boolean = true,
    @ColumnInfo(name = "reminder_frequency") val reminderFrequency: String = "Every Day"
)

@Entity(tableName = "reminder_history")
data class ReminderHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "user_id") val userId: Int,
    @ColumnInfo(name = "reminder_type") val reminderType: String,
    @ColumnInfo(name = "sent_time") val sentTime: String,
    val status: String
)

data class DashboardSummary(
    @SerializedName("total_bills") val totalBills: Int,
    @SerializedName("total_due") val totalDue: Double
)

data class DashboardResponse(
    val dashboard: DashboardSummary
)

@Entity(tableName = "settings")
data class Settings(
    @PrimaryKey val id: Int = 1, // Change this to userId if needed, but project seems to use 1
    @SerializedName("app_name") @ColumnInfo(name = "app_name") val appName: String = "Electricity Bill Monitoring",
    val currency: String = "INR",
    val language: String = "en",
    @SerializedName("notifications_enabled") @ColumnInfo(name = "notifications_enabled") val notificationsEnabled: Int = 1,
    @SerializedName("dark_mode") @ColumnInfo(name = "dark_mode") val darkMode: Int = 0
)

data class SettingsResponse(
    val settings: Settings
)

data class ProfileResponse(
    val profile: User
)
