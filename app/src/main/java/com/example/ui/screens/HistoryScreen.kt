package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.example.ui.theme.NutriRed
import com.example.viewmodel.NutriViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: NutriViewModel) {
    val history by viewModel.historyList.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterCategory by remember { mutableStateOf("Semua") }
    var showDeleteConfirmDialog by remember { mutableStateOf<HistoryEntity?>(null) }
    var showClearAllConfirmDialog by remember { mutableStateOf(false) }

    val filterCategories = listOf("Semua", "Normal", "Kurus", "Overweight", "Obesitas")

    // Filtered computation list
    val filteredHistory = remember(history, searchQuery, selectedFilterCategory) {
        history.filter { record ->
            // Category Filter Check
            val matchesCategory = when (selectedFilterCategory) {
                "Semua" -> true
                "Normal" -> record.category.lowercase().contains("normal")
                "Kurus" -> record.category.lowercase().contains("kurus")
                "Overweight" -> record.category.lowercase().contains("overweight") || record.category.lowercase().contains("kelebihan")
                "Obesitas" -> record.category.lowercase().contains("obesitas")
                else -> true
            }

            // Search Query Check
            val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(record.timestamp))
            val matchesSearch = record.weight.toString().contains(searchQuery) ||
                    record.bmi.toString().contains(searchQuery) ||
                    record.goalType.lowercase().contains(searchQuery.lowercase()) ||
                    dateStr.lowercase().contains(searchQuery.lowercase())

            matchesCategory && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("history_screen")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Riwayat Gizi",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Lacak hasil perbandingan parameter gizi Anda dari waktu ke waktu.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar Row
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Cari berdasarkan berat, BMI, tanggal, atau target...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NutriBlue),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filterCategories.forEach { category ->
                val isSelected = selectedFilterCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilterCategory = category },
                    label = { Text(category, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NutriBlue.copy(alpha = 0.12f),
                        selectedLabelColor = NutriBlue
                    )
                )
            }
        }

        // Hapus Semua trigger
        if (history.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { showClearAllConfirmDialog = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = NutriRed)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Bersihkan Semua Riwayat", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredHistory.isEmpty()) {
            // Empty State
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Riwayat Kosong",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Belum ada riwayat perhitungan yang cocok. Lakukan kalkulasi baru atau sesuaikan filter pencarian Anda.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            // Log History Column
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredHistory, key = { it.id }) { record ->
                    HistoryCard(
                        record = record,
                        onDeleteClick = { showDeleteConfirmDialog = record }
                    )
                }
            }
        }
    }

    // Individual Delete Dialog
    if (showDeleteConfirmDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text("Hapus Catatan Riwayat?", fontWeight = FontWeight.Bold) },
            text = { Text("Tindakan ini tidak bisa dibatalkan. Riwayat gizi pada tanggal ini akan terhapus selamanya.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteHistoryRecord(showDeleteConfirmDialog!!)
                        showDeleteConfirmDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NutriRed)
                ) {
                    Text("Hapus", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text("Batal")
                }
            }
        )
    }

    // Clear All Dialog
    if (showClearAllConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirmDialog = false },
            title = { Text("Bersihkan Semua Riwayat?", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin menghapus seluruh riwayat gizi Anda? Data ini penting untuk statistik perkembangan berat badan Anda.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearAllConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NutriRed)
                ) {
                    Text("Bersihkan Semua", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllConfirmDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun HistoryCard(
    record: HistoryEntity,
    onDeleteClick: () -> Unit
) {
    val dateStr = remember(record.timestamp) {
        val date = Date(record.timestamp)
        SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("in", "ID")).format(date)
    }

    val categoryColor = when {
        record.bmi < 18.5 -> NutriOrange
        record.bmi < 25.0 -> NutriGreen
        record.bmi < 27.0 -> NutriOrange
        else -> NutriRed
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateStr,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("delete_history_item")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus Catatan",
                        tint = NutriRed.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // BMI and Weight Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${record.weight.toInt()}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(text = " kg", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 3.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "${record.height.toInt()}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(text = " cm", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 3.dp))
                    }
                    Text(
                        text = record.goalType,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NutriBlue,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "BMI: ${record.bmi}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = categoryColor
                    )
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(categoryColor.copy(alpha = 0.1f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = record.category,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = categoryColor
                        )
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            // Sub nutrition targets Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                NutrientMiniBadge(label = "Kalori", value = "${record.targetCalorie.toInt()} kkal", icon = "🔥", color = NutriOrange)
                NutrientMiniBadge(label = "Protein", value = "${record.targetProtein.toInt()}g", icon = "🥩", color = NutriBlueLight)
                NutrientMiniBadge(label = "Air", value = "${record.targetWater.toInt()} ml", icon = "💧", color = NutriBlue)
            }
        }
    }
}

@Composable
private fun NutrientMiniBadge(label: String, value: String, icon: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.06f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(icon, fontSize = 12.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
