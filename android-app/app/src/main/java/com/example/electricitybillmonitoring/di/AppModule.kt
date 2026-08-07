package com.example.electricitybillmonitoring.di

import android.content.Context
import androidx.room.Room
import com.example.electricitybillmonitoring.data.local.*
import com.example.electricitybillmonitoring.data.network.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val BASE_URL = "http://10.0.2.2/electricitybillmonitoring/backend/public/"

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "electricity_bill_monitoring_db"
        )
            .addMigrations(AppDatabase.MIGRATION_3_4)
            .fallbackToDestructiveMigration(true)
            .addCallback(object : androidx.room.RoomDatabase.Callback() {
                override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    super.onCreate(db)
                    db.execSQL("INSERT INTO users (id, full_name, mobile_number, service_connection_number, email, password, role) VALUES (4, 'Ramesh Kumar', '9876543210', '123456789012', 'ramesh@example.com', '12345678', 'consumer')")
                    db.execSQL("INSERT INTO electricity_boards (id, name, code, state, country, connector_class) VALUES (1, 'Tamil Nadu Electricity Board', 'TNEB', 'Tamil Nadu', 'India', '')")
                    db.execSQL("INSERT INTO electricity_boards (id, name, code, state, country, connector_class) VALUES (2, 'Maharashtra State Electricity Board', 'MSEB', 'Maharashtra', 'India', '')")
                    db.execSQL("INSERT INTO electricity_boards (id, name, code, state, country, connector_class) VALUES (3, 'APSPDCL', 'AP01', 'Andhra Pradesh', 'India', '')")
                    db.execSQL("INSERT INTO electricity_boards (id, name, code, state, country, connector_class) VALUES (4, 'TGSPDCL', 'TG01', 'Telangana', 'India', '')")
                    db.execSQL("INSERT INTO consumer_connections (id, user_id, board_id, service_number, consumer_number, address, meter_type, connection_status) VALUES (3, 4, 3, '123456789012', '1234567890', '12-3-45, Green Street, Vijayawada, Andhra Pradesh - 520001', 'Domestic', 'active')")
                    db.execSQL("INSERT INTO consumer_connections (id, user_id, board_id, service_number, consumer_number, address, meter_type, connection_status) VALUES (4, 4, 1, '091234567890', 'MT123456789', '45, Anna Salai, Chennai, Tamil Nadu - 600002', 'Domestic', 'active')")
                    db.execSQL("INSERT INTO consumer_connections (id, user_id, board_id, service_number, consumer_number, address, meter_type, connection_status) VALUES (5, 4, 3, '0101234567890', 'APM1234567', '78, Temple Road, Nellore, Andhra Pradesh - 524001', 'Domestic', 'active')")
                    db.execSQL("INSERT INTO consumer_connections (id, user_id, board_id, service_number, consumer_number, address, meter_type, connection_status) VALUES (6, 4, 4, '1234567890123', 'TSM9876543', '101, Lake View, Hyderabad, Telangana - 500001', 'Domestic', 'active')")
                    db.execSQL("INSERT INTO bills (id, service_connection_number, eb_board, full_name, mobile_number, billing_month, units, total_amount_rs, amount_due, amount_paid, due_date, status) VALUES (3, '123456789012', 'APSPDCL', 'Ramesh Kumar', '9876543210', 'May 2025', 265.0, 1265.0, 1265.0, 0.0, '25 May 2025', 'Pending')")
                    db.execSQL("INSERT INTO bills (id, service_connection_number, eb_board, full_name, mobile_number, billing_month, units, total_amount_rs, amount_due, amount_paid, due_date, status) VALUES (4, '091234567890', 'TANGEDCO', 'Ramesh Kumar', '9876543210', 'August 2026', 186.0, 1248.0, 1248.0, 0.0, '20 Aug 2026', 'Pending')")
                    db.execSQL("INSERT INTO bills (id, service_connection_number, eb_board, full_name, mobile_number, billing_month, units, total_amount_rs, amount_due, amount_paid, due_date, status) VALUES (5, '0101234567890', 'APSPDCL', 'Ramesh Kumar', '9876543210', 'August 2026', 210.0, 1575.0, 1575.0, 0.0, '20 Aug 2026', 'Pending')")
                    db.execSQL("INSERT INTO bills (id, service_connection_number, eb_board, full_name, mobile_number, billing_month, units, total_amount_rs, amount_due, amount_paid, due_date, status) VALUES (6, '1234567890123', 'TGSPDCL', 'Ramesh Kumar', '9876543210', 'August 2026', 240.0, 1800.0, 1800.0, 0.0, '20 Aug 2026', 'Pending')")
                }
            })
            .build()
    }

    @Provides
    fun provideBillDao(db: AppDatabase): BillDao = db.billDao()

    @Provides
    fun provideConnectionDao(db: AppDatabase): ConnectionDao = db.connectionDao()

    @Provides
    fun providePaymentDao(db: AppDatabase): PaymentDao = db.paymentDao()

    @Provides
    fun provideNotificationDao(db: AppDatabase): NotificationDao = db.notificationDao()

    @Provides
    fun provideSettingsDao(db: AppDatabase): SettingsDao = db.settingsDao()

    @Provides
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides
    fun provideReminderDao(db: AppDatabase): ReminderDao = db.reminderDao()
}
