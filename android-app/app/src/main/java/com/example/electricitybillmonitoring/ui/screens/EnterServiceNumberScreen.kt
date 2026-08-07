package com.example.electricitybillmonitoring.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.electricitybillmonitoring.data.model.Bill
import com.example.electricitybillmonitoring.ui.viewmodel.BillViewModel
import com.example.electricitybillmonitoring.ui.viewmodel.ConnectionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnterServiceNumberScreen(
    billViewModel: BillViewModel,
    connectionViewModel: ConnectionViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToBills: () -> Unit,
    onNavigateToPayments: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val bills by billViewModel.bills.collectAsState()
    var serviceNumberInput by remember { mutableStateOf("") }
    var searchClicked by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var selectedState by remember { mutableStateOf("Andhra Pradesh") }
    var selectedBoard by remember { mutableStateOf("APSPDCL") }

    val matchedBill = if (searchClicked) {
        bills.find { it.serviceConnectionNumber.equals(serviceNumberInput, ignoreCase = true) }
    } else {
        null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Enter Service Number", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Info Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF0D47A1), Color(0xFF1976D2))
                        ),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Enter Service Number",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Enter your electricity service number to view your account details and manage your bill.",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(30.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            // Input Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Service Number",
                        color = Color(0xFF121D3A),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )

                    OutlinedTextField(
                        value = serviceNumberInput,
                        onValueChange = { serviceNumberInput = it },
                        placeholder = { Text("Enter your 10-12 digit service number") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Scan",
                                tint = Color.Gray
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = {
                            billViewModel.fetchBillsByConsumer(serviceNumberInput) {
                                searchClicked = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1))
                    ) {
                        Text("View Account Details", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(Color.LightGray.copy(alpha = 0.5f))
                        )
                        Text(
                            text = " OR ",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(Color.LightGray.copy(alpha = 0.5f))
                        )
                    }

                    OutlinedButton(
                        onClick = { showLocationDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0D47A1))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.LocationOn, null, tint = Color.Gray)
                                Text("Select Service Location", color = Color(0xFF121D3A))
                            }
                            Icon(Icons.Default.PlayArrow, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Results details block
            if (searchClicked) {
                Text(
                    text = "Linked Account Details",
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF121D3A),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                if (matchedBill != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(Color(0xFFE3F2FD), RoundedCornerShape(20.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Person, null, tint = Color(0xFF0D47A1))
                                    }
                                    Column {
                                        Text(matchedBill.fullName, fontWeight = FontWeight.Bold, color = Color(0xFF121D3A), fontSize = 16.sp)
                                        Text(matchedBill.serviceConnectionNumber, color = Color.Gray, fontSize = 12.sp)
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFE3F2FD), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Domestic", color = Color(0xFF0D47A1), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "12-3-45, Green Street, Vijayawada,\nAndhra Pradesh - 520001",
                                        color = Color.Gray,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Due Amount", color = Color.Gray, fontSize = 12.sp)
                                    Text(
                                        "₹ ${String.format("%,.2f", matchedBill.amountDue)}",
                                        color = Color(0xFFD32F2F),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text("Due Date: ${matchedBill.dueDate}", color = Color.Gray, fontSize = 11.sp)
                                }
                            }

                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Consumer ID", color = Color.Gray, fontSize = 11.sp)
                                    Text("1234567890", color = Color(0xFF121D3A), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Column {
                                    Text("Billing Month", color = Color.Gray, fontSize = 11.sp)
                                    Text(matchedBill.billingMonth, color = Color(0xFF121D3A), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Column {
                                    Text("Units Consumed", color = Color.Gray, fontSize = 11.sp)
                                    Text("${matchedBill.units.toInt()} kWh", color = Color(0xFF121D3A), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    Text(
                        text = "What would you like to do?",
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF121D3A),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GridOptionCard(Modifier.weight(1f), Icons.AutoMirrored.Filled.List, "View Bill", "Check current\nbill details", onNavigateToBills)
                            GridOptionCard(Modifier.weight(1f), Icons.Default.PlayArrow, "Pay Bill", "Make payment\ninstantly", onNavigateToPayments)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GridOptionCard(Modifier.weight(1f), Icons.Default.Info, "Usage History", "Check usage\nhistory", onNavigateToAnalytics)
                            GridOptionCard(Modifier.weight(1f), Icons.AutoMirrored.Filled.List, "Bill History", "View all past\nbills", onNavigateToBills)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GridOptionCard(Modifier.weight(1f), Icons.Default.Person, "Update Details", "Update your\naccount details", onNavigateToProfile)
                            GridOptionCard(Modifier.weight(1f), Icons.Default.Warning, "Complaints", "Raise or track\ncomplaint", {})
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GridOptionCard(Modifier.weight(1f), Icons.Default.Notifications, "Notifications", "View alerts and\nreminders", onNavigateToReminders)
                            GridOptionCard(Modifier.weight(1f), Icons.Default.MoreVert, "More Options", "Explore more\nservices", {})
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, null, tint = Color(0xFFD32F2F))
                            Text("Account not found. Please verify the service number and try again.", color = Color(0xFFD32F2F), fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }

    if (showLocationDialog) {
        var stateExpanded by remember { mutableStateOf(false) }
        var boardExpanded by remember { mutableStateOf(false) }
        val statesList = listOf("Andhra Pradesh", "Tamil Nadu", "Telangana", "Maharashtra")
        val boardsMap = mapOf(
            "Andhra Pradesh" to listOf("APSPDCL"),
            "Tamil Nadu" to listOf("TANGEDCO"),
            "Telangana" to listOf("TGSPDCL"),
            "Maharashtra" to listOf("MSEB")
        )

        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            title = { Text("Select Service Location", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box {
                        OutlinedButton(
                            onClick = { stateExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("State: $selectedState", color = Color(0xFF121D3A))
                                Icon(Icons.Default.ArrowDropDown, null, tint = Color.Gray)
                            }
                        }
                        DropdownMenu(
                            expanded = stateExpanded,
                            onDismissRequest = { stateExpanded = false }
                        ) {
                            statesList.forEach { stateName ->
                                DropdownMenuItem(
                                    text = { Text(stateName) },
                                    onClick = {
                                        selectedState = stateName
                                        selectedBoard = boardsMap[stateName]?.firstOrNull() ?: ""
                                        stateExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Box {
                        OutlinedButton(
                            onClick = { boardExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Board: $selectedBoard", color = Color(0xFF121D3A))
                                Icon(Icons.Default.ArrowDropDown, null, tint = Color.Gray)
                            }
                        }
                        DropdownMenu(
                            expanded = boardExpanded,
                            onDismissRequest = { boardExpanded = false }
                        ) {
                            val boardsList = boardsMap[selectedState] ?: emptyList()
                            boardsList.forEach { boardName ->
                                DropdownMenuItem(
                                    text = { Text(boardName) },
                                    onClick = {
                                        selectedBoard = boardName
                                        boardExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        serviceNumberInput = when (selectedBoard) {
                            "APSPDCL" -> "0101234567890"
                            "TANGEDCO" -> "091234567890"
                            "TGSPDCL" -> "1234567890123"
                            "MSEB" -> "MCN-654321"
                            else -> ""
                        }
                        showLocationDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1))
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLocationDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun GridOptionCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF0D47A1), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, fontWeight = FontWeight.Bold, color = Color(0xFF121D3A), fontSize = 12.sp)
            Text(subtitle, color = Color.Gray, fontSize = 9.sp, textAlign = TextAlign.Center, lineHeight = 11.sp)
        }
    }
}
