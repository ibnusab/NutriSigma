package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NutriBlue
import com.example.ui.theme.NutriBlueLight
import com.example.ui.theme.NutriGreen
import com.example.ui.theme.NutriOrange
import com.example.ui.theme.NutriRed
import com.example.util.NutritionCalculator
import com.example.viewmodel.NutriViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(viewModel: NutriViewModel) {
    val user by viewModel.currentUser.collectAsState()

    var ageStr by remember { mutableStateOf("") }
    var heightStr by remember { mutableStateOf("") }
    var weightStr by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Pria") }
    var selectedActivityMultiplier by remember { mutableStateOf(1.375) }
    var selectedGoal by remember { mutableStateOf("Menjaga Berat Badan") }

    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var calculationResult by remember { mutableStateOf<NutritionCalculator.CalculationResult?>(null) }
    var showResultsDialog by remember { mutableStateOf(false) }

    // Prefill form based on current user metrics if available
    LaunchedEffect(user) {
        if (user != null) {
            ageStr = user!!.age.toString()
            heightStr = user!!.height.toString()
            weightStr = user!!.weight.toString()
            gender = user!!.gender
            selectedActivityMultiplier = user!!.activityLevel
            selectedGoal = user!!.goalType
        }
    }

    val activityLevels = listOf(
        Pair(1.2, "Sangat Ringan (Kerja kantoran, sedikit olahraga)"),
        Pair(1.375, "Ringan (Olahraga ringan 1-3 kali/minggu)"),
        Pair(1.55, "Sedang (Olahraga sedang 3-5 kali/minggu)"),
        Pair(1.725, "Berat (Olahraga berat 6-7 kali/minggu)"),
        Pair(1.9, "Sangat Berat (Atlet, latihan berat, kerja fisik)")
    )

    val goals = listOf(
        "Menurunkan Berat Badan",
        "Menjaga Berat Badan",
        "Menambah Berat Badan",
        "Menambah Massa Otot"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("calculator_screen")
    ) {
        Text(
            text = "Kalkulator Gizi Sigma",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Hitung BMR, TDEE, BMI, dan makronutrisi harian berdasarkan algoritma klinis.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                // GENDER SELECTOR
                Text(
                    text = "Jenis Kelamin",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf("Pria", "Wanita").forEach { option ->
                        val isSelected = gender == option
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) NutriBlue.copy(alpha = 0.12f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .clickable { gender = option }
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (option == "Pria") Icons.Default.Male else Icons.Default.Female,
                                    contentDescription = null,
                                    tint = if (isSelected) NutriBlue else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = option,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) NutriBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // AGE INPUT
                OutlinedTextField(
                    value = ageStr,
                    onValueChange = {
                        ageStr = it
                        errors = errors - "age"
                    },
                    label = { Text("Umur (Tahun)") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = errors.containsKey("age"),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { errors["age"]?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // HEIGHT INPUT
                OutlinedTextField(
                    value = heightStr,
                    onValueChange = {
                        heightStr = it
                        errors = errors - "height"
                    },
                    label = { Text("Tinggi Badan (cm)") },
                    leadingIcon = { Icon(Icons.Default.Height, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = errors.containsKey("height"),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { errors["height"]?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // WEIGHT INPUT
                OutlinedTextField(
                    value = weightStr,
                    onValueChange = {
                        weightStr = it
                        errors = errors - "weight"
                    },
                    label = { Text("Berat Badan (kg)") },
                    leadingIcon = { Icon(Icons.Default.MonitorWeight, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = errors.containsKey("weight"),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { errors["weight"]?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ACTIVITY LEVEL SELECTOR
                Text(
                    text = "Tingkat Aktivitas Harian",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    activityLevels.forEach { item ->
                        val isSelected = selectedActivityMultiplier == item.first
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) NutriBlue.copy(alpha = 0.08f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                )
                                .clickable { selectedActivityMultiplier = item.first }
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedActivityMultiplier = item.first },
                                    colors = RadioButtonDefaults.colors(selectedColor = NutriBlue)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = item.second,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) NutriBlue else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // WEIGHT GOAL TYPE SELECTOR
                Text(
                    text = "Tujuan Berat Badan",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    goals.forEach { goal ->
                        val isSelected = selectedGoal == goal
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) NutriBlue.copy(alpha = 0.08f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                )
                                .clickable { selectedGoal = goal }
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedGoal = goal },
                                    colors = RadioButtonDefaults.colors(selectedColor = NutriBlue)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = goal,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) NutriBlue else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // HITUNG NUTRISI BUTTON
                Button(
                    onClick = {
                        // 1. Validation Logic
                        val age = ageStr.toIntOrNull()
                        val height = heightStr.toDoubleOrNull()
                        val weight = weightStr.toDoubleOrNull()

                        val newErrors = mutableMapOf<String, String>()
                        if (age == null || age <= 0 || age > 120) {
                            newErrors["age"] = "Masukkan umur yang masuk akal (1 - 120)."
                        }
                        if (height == null || height < 50 || height > 250) {
                            newErrors["height"] = "Masukkan tinggi badan yang masuk akal (50 - 250 cm)."
                        }
                        if (weight == null || weight < 10 || weight > 300) {
                            newErrors["weight"] = "Masukkan berat badan yang masuk akal (10 - 300 kg)."
                        }

                        if (newErrors.isNotEmpty()) {
                            errors = newErrors
                        } else {
                            errors = emptyMap()
                            // 2. Perform Calculation
                            val res = NutritionCalculator.calculate(
                                weight = weight!!,
                                height = height!!,
                                age = age!!,
                                gender = gender,
                                activityMultiplier = selectedActivityMultiplier,
                                goalType = selectedGoal
                            )
                            calculationResult = res
                            showResultsDialog = true

                            // 3. Save calculation into user profile & history database
                            viewModel.calculateAndSaveNutrition(
                                weight = weight,
                                height = height,
                                age = age,
                                gender = gender,
                                activityLevel = selectedActivityMultiplier,
                                goalType = selectedGoal
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = NutriBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("HITUNG NUTRISI", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }
            }
        }
    }

    // RESULTS DIALOG WITH CUSTOM MEAL RECOMMENDATIONS
    if (showResultsDialog && calculationResult != null) {
        val res = calculationResult!!
        val categoryColor = when {
            res.bmi < 18.5 -> NutriOrange
            res.bmi < 25.0 -> NutriGreen
            res.bmi < 27.0 -> NutriOrange
            else -> NutriRed
        }

        AlertDialog(
            onDismissRequest = { showResultsDialog = false },
            title = {
                Text(
                    text = "Hasil Analisis Gizi",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth()
                ) {
                    // BMI Score Badge
                    Card(
                        colors = CardDefaults.cardColors(containerColor = categoryColor.copy(alpha = 0.1f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Indeks Massa Tubuh (BMI)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = res.bmi.toString(),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = categoryColor
                            )
                            Text(
                                text = res.category,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = categoryColor,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Key metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricSmallCard(label = "BMR", value = "${res.bmr.toInt()} kkal", modifier = Modifier.weight(1f))
                        MetricSmallCard(label = "TDEE", value = "${res.tdee.toInt()} kkal", modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Daily Target Card
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Kebutuhan Harian untuk Target:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = selectedGoal,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = NutriBlue,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // Calories
                            MetricRowText("Kebutuhan Energi", "${res.targetCalorie.toInt()} kkal")
                            MetricRowText("Kebutuhan Air", "${res.targetWater.toInt()} ml")
                            Divider(modifier = Modifier.padding(vertical = 8.dp))

                            // Macronutrients distribution
                            Text("Target Makronutrien:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 6.dp))
                            MetricRowText("Protein (Pembangun)", "${res.targetProtein.toInt()} gram")
                            MetricRowText("Karbohidrat (Energi)", "${res.targetCarb.toInt()} gram")
                            MetricRowText("Lemak Sehat", "${res.targetFat.toInt()} gram")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // AUTOMATIC MEAL PLAN RECOMMENDATIONS
                    Text(
                        text = "Rekomendasi Menu Makan Harian",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val mealPlan = remember(selectedGoal) {
                        if (selectedGoal.contains("Menurunkan") || res.bmi >= 25.0) {
                            listOf(
                                Pair("🍳 Sarapan", "Putih telur rebus (2 butir) + Oatmeal dengan irisan pisang (1 mangkuk)."),
                                Pair("🍎 Snack Pagi", "Apel merah (1 buah) atau segelas teh hijau tanpa gula."),
                                Pair("🥗 Makan Siang", "Dada ayam panggang (120g) + Nasi merah (100g) + Sayur bayam rebus."),
                                Pair("🥜 Snack Sore", "Kacang almond (10 butir) atau alpukat sedang (setengah buah)."),
                                Pair("🐟 Makan Malam", "Ikan salmon panggang atau tahu tempe panggang + Salad sayur wortel & kol.")
                            )
                        } else {
                            listOf(
                                Pair("🍳 Sarapan", "Telur mata sapi (2 butir) + Roti gandum utuh (2 lembar) dengan mentega kacang."),
                                Pair("🍌 Snack Pagi", "Smoothie alpukat susu almond + Pisang ambon."),
                                Pair("🍛 Makan Siang", "Daging sapi tumis brokoli (150g) + Nasi putih (150g) + Sup tahu."),
                                Pair("🥪 Snack Sore", "Sandwich roti gandum isi keju & dada ayam iris."),
                                Pair("🐟 Makan Malam", "Ikan kembung bumbu kuning + Sup wortel kentang + Nasi putih.")
                            )
                        }
                    }

                    mealPlan.forEach { meal ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(text = meal.first, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NutriBlue)
                                Text(text = meal.second, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showResultsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = NutriBlue)
                ) {
                    Text("OK, Simpan", color = Color.White)
                }
            }
        )
    }
}

@Composable
private fun MetricSmallCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun MetricRowText(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}
