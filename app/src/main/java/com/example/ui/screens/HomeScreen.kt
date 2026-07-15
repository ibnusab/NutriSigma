package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ProfileAvatar
import com.example.ui.theme.NutriBlue
import com.example.ui.theme.NutriBlueLight
import com.example.ui.theme.NutriGreen
import com.example.ui.theme.NutriOrange
import com.example.ui.theme.NutriRed
import com.example.util.NutritionCalculator
import com.example.viewmodel.NutriViewModel
import java.util.*

@Composable
fun HomeScreen(viewModel: NutriViewModel) {
    val user by viewModel.currentUser.collectAsState()
    val history by viewModel.historyList.collectAsState()
    val waterIntake by viewModel.waterIntakeMl.collectAsState()
    val loggedCalories by viewModel.loggedCalories.collectAsState()

    val scrollState = rememberScrollState()

    // Determine Greeting based on time
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 11 -> "Selamat Pagi"
            hour < 15 -> "Selamat Siang"
            hour < 19 -> "Selamat Sore"
            else -> "Selamat Malam"
        }
    }

    // Get current calculated metrics
    val latestRecord = history.firstOrNull()
    val userMetrics = remember(user, latestRecord) {
        if (latestRecord != null) {
            NutritionCalculator.CalculationResult(
                bmi = latestRecord.bmi,
                category = latestRecord.category,
                bmr = latestRecord.bmr,
                tdee = latestRecord.tdee,
                targetCalorie = latestRecord.targetCalorie,
                targetProtein = latestRecord.targetProtein,
                targetCarb = latestRecord.targetCarb,
                targetFat = latestRecord.targetFat,
                targetWater = latestRecord.targetWater,
                idealWeightMin = 18.5, // placeholder
                idealWeightMax = 24.9
            )
        } else if (user != null) {
            // Calculate using user profile defaults
            NutritionCalculator.calculate(
                weight = user!!.weight,
                height = user!!.height,
                age = user!!.age,
                gender = user!!.gender,
                activityMultiplier = user!!.activityLevel,
                goalType = user!!.goalType
            )
        } else {
            null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("home_screen")
    ) {
        // 1. GREETING HEADER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "$greeting,",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = user?.name ?: "Pengguna Nutri",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            // Date badge & Profile Avatar Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Hari Ini",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                ProfileAvatar(
                    avatarUri = user?.avatarUri,
                    name = user?.name ?: "Pengguna Nutri",
                    size = 40.dp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (userMetrics == null) {
            // First time empty state card
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Data Gizi Belum Tersedia",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Masuk ke tab 'Hitung' untuk menginput berat badan, tinggi badan, dan aktivitas Anda demi menghitung status gizi personal.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        } else {
            // 2. BMI CARD
            val categoryColor = when {
                userMetrics.bmi < 18.5 -> NutriOrange
                userMetrics.bmi < 25.0 -> NutriGreen
                userMetrics.bmi < 27.0 -> NutriOrange
                else -> NutriRed
            }

            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Status Indeks Massa Tubuh (BMI)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(categoryColor.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = userMetrics.category.split(" ").first(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = categoryColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = userMetrics.bmi.toString(),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = " kg/m²",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Minimal visual gauge
                    val progressValue = (userMetrics.bmi / 40.0).coerceIn(0.0, 1.0).toFloat()
                    val animatedProgress by animateFloatAsState(
                        targetValue = progressValue,
                        animationSpec = tween(1000),
                        label = "bmi"
                    )

                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = categoryColor,
                        trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("15.0 (Kurus)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("22.0 (Normal)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("35.0+ (Obesitas)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // 3. CALORIE & MACRO CARD
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Ringkasan Energi & Nutrisi Harian",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Circular Calorie Target Indicator
                        Box(
                            modifier = Modifier.size(110.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val calProgress = if (userMetrics.targetCalorie > 0) {
                                (loggedCalories.toFloat() / userMetrics.targetCalorie.toFloat()).coerceIn(0f, 1.0f)
                            } else 0f

                            val animatedCalProgress by animateFloatAsState(
                                targetValue = calProgress,
                                animationSpec = tween(1000),
                                label = "cal"
                            )

                            CircularProgressIndicator(
                                progress = { animatedCalProgress },
                                modifier = Modifier.fillMaxSize(),
                                color = NutriBlue,
                                strokeWidth = 10.dp,
                                trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = NutriOrange)
                                Text(
                                    text = "$loggedCalories",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "/${userMetrics.targetCalorie.toInt()} kkal",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(24.dp))

                        // Macronutrients sliders
                        Column(modifier = Modifier.weight(1f)) {
                            // Protein Progress
                            MacroRow(
                                name = "Protein",
                                amount = "${userMetrics.targetProtein.toInt()}g",
                                color = NutriBlueLight,
                                progress = 0.6f // simulated intake for ui layout
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            // Carbohydrate Progress
                            MacroRow(
                                name = "Karbohidrat",
                                amount = "${userMetrics.targetCarb.toInt()}g",
                                color = NutriOrange,
                                progress = 0.55f
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            // Fat Progress
                            MacroRow(
                                name = "Lemak",
                                amount = "${userMetrics.targetFat.toInt()}g",
                                color = NutriRed,
                                progress = 0.45f
                            )
                        }
                    }

                    // Log Calorie interactive dialog helper
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Catat konsumsi makanan hari ini:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { viewModel.addCalories(200) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("+200 kkal", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = { viewModel.addCalories(500) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("+500 kkal", fontSize = 11.sp)
                            }
                            TextButton(
                                onClick = { viewModel.resetDailyTrackers() }
                            ) {
                                Text("Reset", fontSize = 11.sp, color = NutriRed)
                            }
                        }
                    }
                }
            }

            // 4. WATER PROGRESS CARD
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Kebutuhan Air Putih Harian",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$waterIntake / ${userMetrics.targetWater.toInt()} ml",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = NutriBlueLight,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val waterProgress = if (userMetrics.targetWater > 0) {
                        (waterIntake.toFloat() / userMetrics.targetWater.toFloat()).coerceIn(0f, 1f)
                    } else 0f
                    val animatedWaterProgress by animateFloatAsState(
                        targetValue = waterProgress,
                        animationSpec = tween(800),
                        label = "water"
                    )

                    LinearProgressIndicator(
                        progress = { animatedWaterProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = NutriBlueLight,
                        trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Buttons to quickly log water
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.addWater(250) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NutriBlueLight.copy(alpha = 0.12f)),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = NutriBlueLight, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Gelas (250 ml)", color = NutriBlueLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = { viewModel.addWater(600) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NutriBlue.copy(alpha = 0.12f)),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = NutriBlue, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Botol (600 ml)", color = NutriBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 5. MOTIVATIONAL QUOTE CARD
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(NutriGreen.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💡", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Kiat Gizi Sigma",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NutriGreen
                        )
                        Text(
                            text = "Menghidrasi tubuh Anda 30 menit sebelum makan membantu memperlancar pencernaan dan mengendalikan nafsu makan berlebih.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MacroRow(
    name: String,
    amount: String,
    color: Color,
    progress: Float
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = name, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = amount, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape),
            color = color,
            trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
        )
    }
}
