package com.example.electricitybillmonitoring.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.electricitybillmonitoring.ui.viewmodel.ConnectionViewModel
import com.example.electricitybillmonitoring.ui.viewmodel.DashboardViewModel

@Composable
fun ConnectionsScreen(
    connectionViewModel: ConnectionViewModel,
    dashboardViewModel: DashboardViewModel
) {
    val connections by connectionViewModel.connections.collectAsState()
    val boards by connectionViewModel.boards.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedBoardId by remember { mutableIntStateOf(1) }
    var serviceNumber by remember { mutableStateOf("") }
    var consumerNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var meterType by remember { mutableStateOf("Smart Meter") }

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
            Text("Linked Meters", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Button(onClick = {
                if (boards.isNotEmpty()) {
                    selectedBoardId = boards.first().id
                }
                showAddDialog = true
            }) {
                Text("Link Meter")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (connections.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No linked meters found. Add a connection above.")
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(connections) { connection ->
                    val boardName = boards.find { it.id == connection.boardId }?.name ?: "Electricity Board"
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = boardName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(connection.connectionStatus.uppercase()) }
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Consumer ID: ${connection.consumerNumber}", style = MaterialTheme.typography.bodyMedium)
                            Text(text = "Service Code: ${connection.serviceNumber}", style = MaterialTheme.typography.bodyMedium)
                            Text(text = "Meter Type: ${connection.meterType}", style = MaterialTheme.typography.bodySmall)
                            Text(text = "Installation: ${connection.address}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var expandedBoard by remember { mutableStateOf(false) }
        var boardSearch by remember { mutableStateOf("") }
        val filteredBoards = boards.filter { it.name.contains(boardSearch, ignoreCase = true) || it.state.contains(boardSearch, ignoreCase = true) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Link Meter Connection") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Select Electricity Board:", style = MaterialTheme.typography.titleSmall)
                    
                    OutlinedTextField(
                        value = boardSearch,
                        onValueChange = { boardSearch = it },
                        label = { Text("Search Board or State") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { Icon(Icons.Default.Search, null) }
                    )

                    Box(modifier = Modifier.heightIn(max = 200.dp)) {
                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            items(filteredBoards) { b ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedBoardId = b.id }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedBoardId == b.id,
                                        onClick = { selectedBoardId = b.id }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(b.name, style = MaterialTheme.typography.bodyLarge)
                                        Text(b.state, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = serviceNumber,
                        onValueChange = { serviceNumber = it },
                        label = { Text("Service Number") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = consumerNumber,
                        onValueChange = { consumerNumber = it },
                        label = { Text("Consumer Number") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Installation Address") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = meterType,
                        onValueChange = { meterType = it },
                        label = { Text("Meter Type (e.g. Smart Meter, Digital)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        connectionViewModel.addConnection(
                            userId = dashboardViewModel.getUserId(),
                            boardId = selectedBoardId,
                            serviceNumber = serviceNumber,
                            consumerNumber = consumerNumber,
                            address = address,
                            meterType = meterType,
                            onSuccess = {
                                showAddDialog = false
                                dashboardViewModel.loadData()
                            }
                        )
                    },
                    enabled = serviceNumber.isNotBlank() && consumerNumber.isNotBlank() && address.isNotBlank()
                ) {
                    Text("Link Meter")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
