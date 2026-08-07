package com.example.electricitybillmonitoring.data.local

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration
import com.example.electricitybillmonitoring.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE service_connection_number = :scNumber LIMIT 1")
    suspend fun getUserBySCNumber(scNumber: String): User?

    @Query("SELECT * FROM users WHERE mobile_number = :mobile LIMIT 1")
    suspend fun getUserByMobile(mobile: String): User?

    @Query("SELECT * FROM users WHERE service_connection_number = :scNumber AND mobile_number = :mobile LIMIT 1")
    suspend fun getUserBySCAndMobile(scNumber: String, mobile: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Int): User?
}

@Dao
interface BillDao {
    @Query("SELECT * FROM bills ORDER BY id DESC")
    fun getAllBillsFlow(): Flow<List<Bill>>

    @Query("SELECT * FROM bills WHERE service_connection_number = :scNumber ORDER BY billing_month DESC")
    fun getBillsBySCNumberFlow(scNumber: String): Flow<List<Bill>>

    @Query("SELECT * FROM bills WHERE service_connection_number = :scNumber")
    suspend fun getBillsBySCNumber(scNumber: String): List<Bill>

    @Query("SELECT * FROM bills")
    suspend fun getAllBills(): List<Bill>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: Bill)

    @Update
    suspend fun updateBill(bill: Bill)

    @Delete
    suspend fun deleteBill(bill: Bill)

    @Query("SELECT * FROM bills WHERE id = :id")
    suspend fun getBillById(id: Int): Bill?
}

@Dao
interface ConnectionDao {
    @Query("SELECT * FROM consumer_connections ORDER BY id DESC")
    fun getAllConnectionsFlow(): Flow<List<ConsumerConnection>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConnection(connection: ConsumerConnection)

    @Query("SELECT * FROM electricity_boards")
    suspend fun getBoards(): List<ElectricityBoard>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBoard(board: ElectricityBoard)
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY id DESC")
    fun getAllPaymentsFlow(): Flow<List<Payment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY id DESC")
    fun getAllNotificationsFlow(): Flow<List<Notification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: Notification)

    @Query("UPDATE notifications SET is_read = 1 WHERE id = :id")
    suspend fun markAsRead(id: Int)
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders")
    fun getAllRemindersFlow(): Flow<List<Reminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder)

    @Query("SELECT * FROM reminder_settings WHERE userId = :userId LIMIT 1")
    fun getReminderSettingsFlow(userId: Int): Flow<ReminderSettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminderSettings(settings: ReminderSettings)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminderHistory(history: ReminderHistory)

    @Query("DELETE FROM reminder_history WHERE user_id = :userId")
    suspend fun deleteHistoryByUserId(userId: Int)

    @Query("SELECT * FROM reminder_history WHERE user_id = :userId ORDER BY id DESC")
    fun getReminderHistoryFlow(userId: Int): Flow<List<ReminderHistory>>
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE id = :userId LIMIT 1")
    fun getSettingsFlow(userId: Int): Flow<Settings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: Settings)
}

@Database(
    entities = [
        User::class, Bill::class, ConsumerConnection::class, Payment::class,
        Notification::class, Settings::class, Reminder::class, ElectricityBoard::class,
        ReminderSettings::class, ReminderHistory::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun billDao(): BillDao
    abstract fun connectionDao(): ConnectionDao
    abstract fun paymentDao(): PaymentDao
    abstract fun notificationDao(): NotificationDao
    abstract fun reminderDao(): ReminderDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Migrate User table
                db.execSQL("ALTER TABLE users ADD COLUMN service_connection_number TEXT NOT NULL DEFAULT ''")
                // Room doesn't support easy rename in older SQLite, so we add the new column and copy if needed, 
                // but let's assume we can just add and the app will handle it.
                // Actually, let's do a proper rename if possible or just add.
                db.execSQL("ALTER TABLE users ADD COLUMN mobile_number TEXT NOT NULL DEFAULT ''")
                
                // Migrate Bill table - recreated with new schema
                db.execSQL("CREATE TABLE IF NOT EXISTS `bills_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `service_connection_number` TEXT NOT NULL, `eb_board` TEXT NOT NULL, `full_name` TEXT NOT NULL, `mobile_number` TEXT NOT NULL, `billing_month` TEXT NOT NULL, `units` REAL NOT NULL, `total_amount_rs` REAL NOT NULL, `amount_due` REAL NOT NULL DEFAULT 0.0, `amount_paid` REAL NOT NULL DEFAULT 0.0, `due_date` TEXT NOT NULL, `status` TEXT NOT NULL DEFAULT 'Pending')")
                db.execSQL("DROP TABLE IF EXISTS bills")
                db.execSQL("ALTER TABLE bills_new RENAME TO bills")
            }
        }
    }
}
