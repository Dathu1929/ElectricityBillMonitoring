package com.example.electricitybillmonitoring.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.electricitybillmonitoring.data.model.Bill
import com.example.electricitybillmonitoring.ui.viewmodel.BillViewModel
import com.example.electricitybillmonitoring.ui.viewmodel.PaymentViewModel
import com.example.electricitybillmonitoring.ui.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsScreen(
    paymentViewModel: PaymentViewModel,
    billViewModel: BillViewModel,
    dashboardViewModel: DashboardViewModel
) {
    val payments by paymentViewModel.payments.collectAsState()
    val bills by billViewModel.bills.collectAsState()
    val loading by paymentViewModel.loading.collectAsState()

    var paymentStep by remember { mutableStateOf("history") } // history, make_payment, success
    var selectedBill by remember { mutableStateOf<Bill?>(null) }
    var selectedMethod by remember { mutableStateOf("UPI") }

    val pendingBills = bills.filter { it.status.lowercase() != "paid" }

    when (paymentStep) {
        "history" -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Transactions", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Button(
                        onClick = {
                            if (pendingBills.isNotEmpty()) {
                                selectedBill = pendingBills.first()
                                paymentStep = "make_payment"
                            }
                        },
                        enabled = pendingBills.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1))
                    ) {
                        Text("Pay Bill")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                if (payments.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No transaction history found.")
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(payments) { payment ->
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Bill Ref ID: #${payment.billId}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(text = "Method: ${payment.method}", style = MaterialTheme.typography.bodyMedium)
                                        Text(text = "Date: ${payment.createdAt}", style = MaterialTheme.typography.bodySmall)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "₹ ${payment.amount}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0D47A1)
                                        )
                                        Text(
                                            text = payment.status.uppercase(),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (payment.status == "captured") Color(0xFF2E7D32) else Color(0xFFC62828)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        "make_payment" -> {
            selectedBill?.let { bill ->
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Make Payment", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                            navigationIcon = {
                                IconButton(onClick = { paymentStep = "history" }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.White
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D47A1))
                        )
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFF5F8FD))
                            .padding(innerPadding)
                            .padding(24.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Amount Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(24.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("Amount to Pay", color = Color.Gray, fontSize = 14.sp)
                                Text(
                                    text = "₹ ${String.format("%,.2f", bill.amountDue - bill.amountPaid)}",
                                    color = Color(0xFF121D3A),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Consumer No: ${bill.serviceConnectionNumber}",
                                    color = Color.Gray,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        // Payment Methods
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Select Payment Method",
                                color = Color(0xFF121D3A),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )

                            // UPI
                            PaymentMethodItem(
                                title = "UPI",
                                subtitle = "PhonePe / GPay / Paytm",
                                icon = Icons.Default.PlayArrow, // placeholder
                                isSelected = selectedMethod == "UPI",
                                onClick = { selectedMethod = "UPI" }
                            )

                            // Card
                            PaymentMethodItem(
                                title = "Debit / Credit Card",
                                subtitle = "",
                                icon = Icons.Default.Info, // placeholder
                                isSelected = selectedMethod == "Card",
                                onClick = { selectedMethod = "Card" }
                            )

                            // Net Banking
                            PaymentMethodItem(
                                title = "Net Banking",
                                subtitle = "",
                                icon = Icons.Default.Home, // placeholder
                                isSelected = selectedMethod == "Net Banking",
                                onClick = { selectedMethod = "Net Banking" }
                            )

                            // Wallets
                            PaymentMethodItem(
                                title = "Wallets",
                                subtitle = "",
                                icon = Icons.Default.AccountBox, // placeholder
                                isSelected = selectedMethod == "Wallets",
                                onClick = { selectedMethod = "Wallets" }
                            )
                        }

                        // Bottom Actions
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = {
                                    paymentViewModel.makePayment(
                                        billId = bill.id,
                                        amount = bill.amountDue - bill.amountPaid,
                                        method = selectedMethod,
                                        onSuccess = {
                                            billViewModel.loadBills()
                                            paymentStep = "success"
                                        }
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1)),
                                enabled = !loading
                            ) {
                                if (loading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                                } else {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Lock, null, modifier = Modifier.size(18.dp))
                                        Text("Pay Securely", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "B",
                                    color = Color(0xFFE65100),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Powered by Bharat BillPay",
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        "success" -> {
            selectedBill?.let { bill ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(20.dp))

                    // Confetti and Success Check
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(140.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                // Draw green success circle
                                drawCircle(
                                    color = Color(0xFF4CAF50),
                                    radius = size.width * 0.35f
                                )
                                // Draw Confetti lines around
                                val center = Offset(size.width / 2, size.height / 2)
                                val radius = size.width * 0.45f
                                for (i in 0 until 8) {
                                    val angle = (i * 45) * (Math.PI / 180)
                                    val startX = (center.x + (radius - 15) * Math.cos(angle)).toFloat()
                                    val startY = (center.y + (radius - 15) * Math.sin(angle)).toFloat()
                                    val endX = (center.x + radius * Math.cos(angle)).toFloat()
                                    val endY = (center.y + radius * Math.sin(angle)).toFloat()
                                    drawLine(
                                        color = if (i % 2 == 0) Color(0xFFFFD54F) else Color(0xFF4CAF50),
                                        start = Offset(startX, startY),
                                        end = Offset(endX, endY),
                                        strokeWidth = 6f
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(54.dp)
                            )
                        }

                        Text(
                            text = "Payment Successful!",
                            color = Color(0xFF2E7D32),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Your electricity bill has been paid.",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }

                    // Receipt Details Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F8FD))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Amount Paid", color = Color.Gray, fontSize = 13.sp)
                                Text(
                                    text = "₹ ${String.format("%,.2f", bill.amountDue - bill.amountPaid)}",
                                    color = Color(0xFF121D3A),
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Divider(color = Color.LightGray.copy(alpha = 0.5f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Consumer No.", color = Color.Gray, fontSize = 13.sp)
                                Text(bill.serviceConnectionNumber, color = Color(0xFF121D3A), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Transaction ID", color = Color.Gray, fontSize = 13.sp)
                                Text("BILLPAY58273942", color = Color(0xFF121D3A), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Paid On", color = Color.Gray, fontSize = 13.sp)
                                Text("08 Aug 2026, 10:24 AM", color = Color(0xFF121D3A), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }

                    // Success Action Buttons
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { /* Handle receipt download */ },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1))
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(18.dp))
                                Text("Download Receipt", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = { paymentStep = "history" },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF0D47A1))
                        ) {
                            Text("Back to Home", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D47A1))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentMethodItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .border(
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) Color(0xFF0D47A1) else Color.LightGray.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                color = if (isSelected) Color(0xFFE3F2FD).copy(alpha = 0.5f) else Color.White,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF0D47A1),
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        text = title,
                        color = Color(0xFF121D3A),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    if (subtitle.isNotEmpty()) {
                        Text(
                            text = subtitle,
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF0D47A1))
            )
        }
    }
}
