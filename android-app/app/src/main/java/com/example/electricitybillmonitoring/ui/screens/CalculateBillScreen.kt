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
import com.example.electricitybillmonitoring.data.model.Bill
import com.example.electricitybillmonitoring.ui.components.WaveHeader
import com.example.electricitybillmonitoring.ui.viewmodel.BillViewModel

@Composable
fun CalculateBillScreen(
    billViewModel: BillViewModel,
    onNavigateBack: () -> Unit
) {
    var meterNo by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var unitsConsumed by remember { mutableStateOf("") }
    var month by remember { mutableStateOf("August 2026") }

    var showResultDialog by remember { mutableStateOf(false) }
    var calculatedAmount by remember { mutableDoubleStateOf(0.0) }

    Box(modifier = Modifier.fillMaxSize()) {
        WaveHeader(
            primaryColor = Color(0xFF1E88E5),
            secondaryColor = Color(0xFF1565C0)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 110.dp, start = 24.dp, end = 24.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Calculate Electricity Bill",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1565C0)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = meterNo,
                        onValueChange = { meterNo = it },
                        label = { Text("Meter No") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
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
                        value = unitsConsumed,
                        onValueChange = { unitsConsumed = it },
                        label = { Text("Units Consumed") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = month,
                        onValueChange = { month = it },
                        label = { Text("Month") },
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
                                val units = unitsConsumed.toDoubleOrNull() ?: 0.0
                                val rate = 7.50
                                calculatedAmount = units * rate
                                
                                val newBill = Bill(
                                    serviceConnectionNumber = meterNo,
                                    ebBoard = "TNEB",
                                    fullName = name,
                                    mobileNumber = "9999999999",
                                    billingMonth = month,
                                    units = units,
                                    totalAmountRs = calculatedAmount,
                                    amountDue = calculatedAmount,
                                    amountPaid = 0.0,
                                    dueDate = "2026-09-10",
                                    status = "Pending"
                                )
                                billViewModel.addBill(newBill)
                                showResultDialog = true
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                            enabled = meterNo.isNotBlank() && name.isNotBlank() && unitsConsumed.isNotBlank()
                        ) {
                            Text("Submit")
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

    if (showResultDialog) {
        AlertDialog(
            onDismissRequest = { showResultDialog = false; onNavigateBack() },
            title = { Text("Calculation Complete") },
            text = {
                Column {
                    Text("Total Amount calculated for $unitsConsumed units:")
                    Text(
                        "₹ $calculatedAmount",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1565C0)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("The bill has been successfully added to pending bills.")
                }
            },
            confirmButton = {
                Button(onClick = { showResultDialog = false; onNavigateBack() }) {
                    Text("OK")
                }
            }
        )
    }
}
