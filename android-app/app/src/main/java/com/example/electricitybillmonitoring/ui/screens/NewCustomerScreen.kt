package com.example.electricitybillmonitoring.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.electricitybillmonitoring.ui.components.WaveHeader
import com.example.electricitybillmonitoring.ui.viewmodel.ConnectionViewModel
import com.example.electricitybillmonitoring.ui.viewmodel.DashboardViewModel
import com.example.electricitybillmonitoring.ui.viewmodel.BillViewModel
import com.example.electricitybillmonitoring.data.model.Bill

@Composable
fun NewCustomerScreen(
    connectionViewModel: ConnectionViewModel,
    dashboardViewModel: DashboardViewModel,
    billViewModel: BillViewModel,
    onNavigateBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var meterNo by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    
    var showSuccess by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        WaveHeader(
            primaryColor = Color(0xFF00ACC1),
            secondaryColor = Color(0xFF00838F)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 100.dp, start = 24.dp, end = 24.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "New Customer",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00838F)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Customer Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = meterNo,
                        onValueChange = { meterNo = it },
                        label = { Text("Meter No") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = state,
                        onValueChange = { state = it },
                        label = { Text("State") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = {
                                connectionViewModel.addConnection(
                                    userId = dashboardViewModel.getUserId(),
                                    boardId = 1, 
                                    serviceNumber = meterNo,
                                    consumerNumber = meterNo,
                                    address = "$address, $city, $state",
                                    meterType = "Digital Meter",
                                    onSuccess = {
                                        // Auto-generate matching pending bill record for dynamic lookups
                                        billViewModel.addBill(
                                            Bill(
                                                id = (1000..9999).random(),
                                                serviceConnectionNumber = meterNo,
                                                ebBoard = "TANGEDCO",
                                                fullName = name,
                                                mobileNumber = phoneNumber,
                                                billingMonth = "August 2026",
                                                units = 186.0,
                                                totalAmountRs = 1248.0,
                                                amountDue = 1248.0,
                                                amountPaid = 0.0,
                                                dueDate = "20 Aug 2026",
                                                status = "Pending"
                                            )
                                        )
                                        showSuccess = true
                                    }
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00838F)),
                            enabled = name.isNotBlank() && meterNo.isNotBlank()
                        ) {
                            Text("Next")
                        }

                        Button(
                            onClick = onNavigateBack,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                        ) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }

    if (showSuccess) {
        AlertDialog(
            onDismissRequest = { showSuccess = false; onNavigateBack() },
            title = { Text("Success") },
            text = { Text("Customer and meter connection registered successfully!") },
            confirmButton = {
                Button(onClick = { showSuccess = false; onNavigateBack() }) {
                    Text("OK")
                }
            }
        )
    }
}
