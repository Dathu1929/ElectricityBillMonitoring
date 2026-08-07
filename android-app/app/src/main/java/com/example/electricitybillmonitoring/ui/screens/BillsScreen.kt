package com.example.electricitybillmonitoring.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.electricitybillmonitoring.data.model.Bill
import com.example.electricitybillmonitoring.ui.viewmodel.BillViewModel

@Composable
fun BillsScreen(
    viewModel: BillViewModel
) {
    val bills by viewModel.bills.collectAsState()
    var selectedBill by remember { mutableStateOf<Bill?>(null) }
    var filterText by remember { mutableStateOf("") }

    val filteredBills = bills.filter {
        it.serviceConnectionNumber.contains(filterText, ignoreCase = true) ||
                it.billingMonth.contains(filterText, ignoreCase = true) ||
                it.status.contains(filterText, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Billing Statements", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = filterText,
            onValueChange = { filterText = it },
            label = { Text("Search by consumer / month / status") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (filteredBills.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No bills match the search criteria.")
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredBills) { bill ->
                    BillItemCard(bill = bill, onClick = { selectedBill = bill })
                }
            }
        }
    }

    // Detail Dialog
    selectedBill?.let { bill ->
        AlertDialog(
            onDismissRequest = { selectedBill = null },
            title = { Text("Statement Detail - ${bill.billingMonth}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailRow(label = "Consumer No:", value = bill.serviceConnectionNumber)
                    DetailRow(label = "Units Consumed:", value = "${bill.units} kWh")
                    DetailRow(label = "Rate/Unit:", value = "₹ ${if (bill.units > 0) String.format("%.2f", bill.totalAmountRs / bill.units) else "0.00"}")
                    DetailRow(label = "Due Date:", value = bill.dueDate)
                    HorizontalDivider()
                    DetailRow(label = "Total Amount Due:", value = "₹ ${bill.amountDue}", isBold = true)
                    DetailRow(label = "Amount Paid:", value = "₹ ${bill.amountPaid}")
                    DetailRow(label = "Outstanding:", value = "₹ ${bill.amountDue - bill.amountPaid}", isBold = true)
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedBill = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun BillItemCard(bill: Bill, onClick: () -> Unit) {
    val statusColor = when (bill.status) {
        "paid" -> MaterialTheme.colorScheme.primary
        "pending" -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.error
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Month: ${bill.billingMonth}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Consumer ID: ${bill.serviceConnectionNumber}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Due: ${bill.dueDate}", style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹ ${bill.amountDue}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                SuggestionChip(
                    onClick = {},
                    label = { Text(bill.status.uppercase()) }
                )
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
    }
}
