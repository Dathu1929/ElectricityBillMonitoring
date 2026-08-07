package com.example.electricitybillmonitoring.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.electricitybillmonitoring.ui.viewmodel.AuthViewModel
import com.example.electricitybillmonitoring.ui.viewmodel.DashboardViewModel

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    dashboardViewModel: DashboardViewModel,
    onLogout: () -> Unit
) {
    val user by authViewModel.userState.collectAsState()
    val settings by dashboardViewModel.settings.collectAsState()

    var appName by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("INR") }
    var language by remember { mutableStateOf("en") }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var darkMode by remember { mutableStateOf(false) }

    LaunchedEffect(settings) {
        settings?.let {
            appName = it.appName
            currency = it.currency
            language = it.language
            notificationsEnabled = it.notificationsEnabled == 1
            darkMode = it.darkMode == 1
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Profile & Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        // User profile Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = user?.fullName ?: "Ava Sharma", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Email: ${user?.email ?: "ava@example.com"}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Role: ${user?.role?.uppercase() ?: "CONSUMER"}", style = MaterialTheme.typography.bodySmall)
            }
        }

        Text("App Preferences", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = appName,
            onValueChange = { appName = it },
            label = { Text("Application Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Currency Setting")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = currency == "INR",
                    onClick = { currency = "INR" },
                    label = { Text("INR (₹)") }
                )
                FilterChip(
                    selected = currency == "USD",
                    onClick = { currency = "USD" },
                    label = { Text("USD ($)") }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Language Setting")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = language == "en",
                    onClick = { language = "en" },
                    label = { Text("English") }
                )
                FilterChip(
                    selected = language == "hi",
                    onClick = { language = "hi" },
                    label = { Text("Hindi") }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Enable Notifications")
            Switch(
                checked = notificationsEnabled,
                onCheckedChange = { notificationsEnabled = it }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Dark Mode Theme")
            Switch(
                checked = darkMode,
                onCheckedChange = { darkMode = it }
            )
        }

        Button(
            onClick = {
                dashboardViewModel.updateSettings(
                    appName = appName,
                    currency = currency,
                    language = language,
                    nEnabled = notificationsEnabled,
                    dark = darkMode
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Preferences")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                authViewModel.logout()
                onLogout()
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout")
        }
    }
}
