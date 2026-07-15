package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HistoryEntity
import com.example.ui.theme.NutriBlue
import com.example.ui.theme.NutriBlueLight
import com.example.ui.theme.NutriGreen
import com.example.ui.theme.NutriOrange
import com.example.viewmodel.NutriViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatsScreen(viewModel: NutriViewModel) {
    val history by viewModel.historyList.collectAsState()
    val user by viewModel.currentUser.collectAsState()

    val scrollState = rememberScrollState()

    // Sort calculations chronologically for statistics
    val chronologicalHistory = remember(history) {
        history.sortedBy { it.timestamp }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("stats_screen")
    ) {
        Text(
            text = "Analisis Statistik",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Evaluasi tren kemajuan fisik dan nutrisi Anda secara komprehensif.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        if (chronologicalHistory.size < 2) {
            // Need at least 2 points to draw progress charts
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = null,
                        tint = NutriBlue,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Grafik Belum Siap",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Nutri Sigma memerlukan minimal 2 catatan riwayat kalkulasi untuk memplot grafik tren kemajuan fisik Anda. Silakan masukkan data baru Anda di tab 'Hitung'!",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        } else {
            // Summary Info Badges Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val firstWeight = chronologicalHistory.first().weight
                val lastWeight = chronologicalHistory.last().weight
                val diff = lastWeight - firstWeight
                val isLoss = diff < 0

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isLoss) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = if (isLoss) NutriGreen else NutriBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tren Berat", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${if (diff > 0) "+" else ""}${String.format("%.1f", diff)} kg",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLoss) NutriGreen else NutriBlue
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Sesi Kalkulasi", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${chronologicalHistory.size} Sesi",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // 1. WEIGHT GRAPH CARD
            GraphCard(
                title = "Tren Perkembangan Berat Badan",
                subtitle = "Melacak berat badan Anda dalam kg",
                points = chronologicalHistory.map { it.weight.toFloat() },
                timestamps = chronologicalHistory.map { it.timestamp },
                lineColor = NutriBlue
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. BMI GRAPH CARD
            GraphCard(
                title = "Tren Indeks Massa Tubuh (BMI)",
                subtitle = "Memantau skor BMI Anda",
                points = chronologicalHistory.map { it.bmi.toFloat() },
                timestamps = chronologicalHistory.map { it.timestamp },
                lineColor = NutriGreen
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. CALORIES TARGET GRAPH CARD
            GraphCard(
                title = "Tren Target Kalori Harian",
                subtitle = "Fluktuasi asupan kkal yang disarankan",
                points = chronologicalHistory.map { it.targetCalorie.toFloat() },
                timestamps = chronologicalHistory.map { it.timestamp },
                lineColor = NutriOrange
            )
        }
    }
}

@Composable
private fun GraphCard(
    title: String,
    subtitle: String,
    points: List<Float>,
    timestamps: List<Long>,
    lineColor: Color
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 16.dp))

            // Graph canvas area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .padding(vertical = 8.dp)
            ) {
                val minPoint = points.minOrNull() ?: 0f
                val maxPoint = points.maxOrNull() ?: 100f
                val range = (maxPoint - minPoint).coerceAtLeast(1f)

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val stepX = width / (points.size - 1).coerceAtLeast(1)

                    val path = Path()
                    val fillPath = Path()

                    points.forEachIndexed { index, value ->
                        // Calculate coordinates
                        val x = index * stepX
                        // normalize Y coordinate to canvas height
                        val ratio = (value - minPoint) / range
                        val y = height - (ratio * height * 0.8f) - (height * 0.1f) // 10% padding top & bottom

                        if (index == 0) {
                            path.moveTo(x, y)
                            fillPath.moveTo(x, height)
                            fillPath.lineTo(x, y)
                        } else {
                            path.lineTo(x, y)
                            fillPath.lineTo(x, y)
                        }

                        if (index == points.size - 1) {
                            fillPath.lineTo(x, height)
                            fillPath.close()
                        }

                        // Draw Point Marker Dots
                        drawCircle(
                            color = lineColor,
                            radius = 4.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }

                    // Draw Line Path
                    drawPath(
                        path = path,
                        color = lineColor,
                        style = Stroke(width = 3.dp.toPx())
                    )

                    // Draw Gradient Fill under line
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                lineColor.copy(alpha = 0.35f),
                                lineColor.copy(alpha = 0.0f)
                            )
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // X-Axis Date markers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val firstDate = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(timestamps.first()))
                val lastDate = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(timestamps.last()))

                Text(firstDate, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Garis Waktu Perkembangan", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                Text(lastDate, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
