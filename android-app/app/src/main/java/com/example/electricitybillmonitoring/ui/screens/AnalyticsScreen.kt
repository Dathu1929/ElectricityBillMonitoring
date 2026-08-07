package com.example.electricitybillmonitoring.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.electricitybillmonitoring.ui.viewmodel.BillViewModel

@Composable
fun AnalyticsScreen(
    billViewModel: BillViewModel
) {
    val bills by billViewModel.bills.collectAsState()

    // Take recent units used for analysis chart
    val historyData = bills.map { it.billingMonth to it.units.toFloat() }.reversed()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Usage Analytics", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        if (historyData.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No consumption data available yet.")
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Energy Consumption (kWh)", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        val width = size.width
                        val height = size.height
                        val padding = 40f
                        val graphWidth = width - 2 * padding
                        val graphHeight = height - 2 * padding

                        val maxVal = (historyData.maxOfOrNull { it.second } ?: 100f).coerceAtLeast(100f) * 1.2f

                        // Draw background lines
                        val steps = 4
                        for (i in 0..steps) {
                            val y = padding + graphHeight * (1 - i.toFloat() / steps)
                            drawLine(
                                color = Color.Gray.copy(alpha = 0.3f),
                                start = Offset(padding, y),
                                end = Offset(width - padding, y),
                                strokeWidth = 2f
                            )
                        }

                        // Draw Bars
                        val barSpacing = 30f
                        val totalBars = historyData.size
                        val totalSpacing = barSpacing * (totalBars - 1)
                        val barWidth = (graphWidth - totalSpacing) / totalBars

                        historyData.forEachIndexed { index, (label, value) ->
                            val x = padding + index * (barWidth + barSpacing)
                            val barHeight = (value / maxVal) * graphHeight
                            val y = height - padding - barHeight

                            // Draw Bar
                            drawRect(
                                color = Color(0xFF6200EE),
                                topLeft = Offset(x, y),
                                size = Size(barWidth, barHeight)
                            )

                            // Label
                            drawContext.canvas.nativeCanvas.drawText(
                                label,
                                x + barWidth / 2f - 20f,
                                height - padding + 30f,
                                android.graphics.Paint().apply {
                                    color = android.graphics.Color.DKGRAY
                                    textSize = 24f
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Conservation Tips list
        Text("Energy Conservation Tips", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("💡 LED Lighting Upgrade", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("Replace old incandescent bulbs with ENERGY STAR certified LED bulbs to use up to 75% less energy.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            item {
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("🔌 Unplug Idle Devices", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("Electronics draw standby power when plugged in. Use smart power strips to shut down power to multiple devices.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            item {
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("🌡️ Optimize Thermostat Settings", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("Set your thermostat a few degrees higher in summer or lower in winter to dramatically lower monthly heating and cooling bills.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
