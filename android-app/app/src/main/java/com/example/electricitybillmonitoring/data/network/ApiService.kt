package com.example.electricitybillmonitoring.data.network

import com.example.electricitybillmonitoring.data.model.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body credentials: Map<String, String>): LoginResponse

    @POST("auth/register")
    suspend fun register(@Body data: Map<String, String>): Map<String, String>

    @GET("dashboard")
    suspend fun getDashboard(): DashboardResponse

    @GET("bills")
    suspend fun getBills(): Map<String, List<Bill>>

    @GET("bills/by_consumer")
    suspend fun getBillsByConsumer(@Query("consumer_number") consumerNumber: String): Map<String, List<Bill>>

    @GET("payments")
    suspend fun getPayments(): Map<String, List<Payment>>

    @POST("payments/create")
    suspend fun createPayment(@Body paymentData: Map<String, Any>): Map<String, Any>

    @GET("connections")
    suspend fun getConnections(): Map<String, List<ConsumerConnection>>

    @POST("connections/create")
    suspend fun createConnection(@Body connectionData: Map<String, Any>): Map<String, Any>

    @GET("notifications")
    suspend fun getNotifications(): Map<String, List<Notification>>

    @POST("notifications/read")
    suspend fun markNotificationRead(@Body payload: Map<String, Int>): Map<String, String>

    @GET("settings")
    suspend fun getSettings(): SettingsResponse

    @POST("settings/update")
    suspend fun updateSettings(@Body settingsData: Settings): Map<String, String>

    @GET("profile")
    suspend fun getProfile(): ProfileResponse

    @GET("boards")
    suspend fun getBoards(): Map<String, List<ElectricityBoard>>
}
