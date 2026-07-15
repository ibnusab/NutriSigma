package com.example.util

import com.example.data.model.HistoryEntity
import kotlin.math.roundToInt

object NutritionCalculator {

    data class CalculationResult(
        val bmi: Double,
        val category: String,
        val bmr: Double,
        val tdee: Double,
        val targetCalorie: Double,
        val targetProtein: Double, // in grams
        val targetCarb: Double,    // in grams
        val targetFat: Double,     // in grams
        val targetWater: Double,   // in ml
        val idealWeightMin: Double,
        val idealWeightMax: Double
    )

    fun calculate(
        weight: Double,
        height: Double,
        age: Int,
        gender: String, // "Pria" or "Wanita"
        activityMultiplier: Double, // 1.2, 1.375, 1.55, 1.725, 1.9
        goalType: String // "Menurunkan Berat Badan", "Menjaga Berat Badan", "Menambah Berat Badan", "Menambah Massa Otot"
    ): CalculationResult {
        // 1. BMI Calculation
        val heightInMeters = height / 100.0
        val bmi = if (heightInMeters > 0) weight / (heightInMeters * heightInMeters) else 0.0

        val category = when {
            bmi < 18.5 -> "Kurus (Underweight)"
            bmi < 25.0 -> "Normal"
            bmi < 27.0 -> "Kelebihan Berat Badan (Overweight)"
            bmi < 30.0 -> "Obesitas Tingkat I"
            else -> "Obesitas Tingkat II"
        }

        // 2. BMR (Mifflin-St Jeor)
        val bmr = if (gender.lowercase() == "pria") {
            (10.0 * weight) + (6.25 * height) - (5.0 * age) + 5.0
        } else {
            (10.0 * weight) + (6.25 * height) - (5.0 * age) - 161.0
        }

        // 3. TDEE
        val tdee = bmr * activityMultiplier

        // 4. Target Calories based on Goal
        val targetCalorie = when (goalType) {
            "Menurunkan Berat Badan" -> (tdee - 500.0).coerceAtLeast(bmr * 0.9)
            "Menambah Berat Badan" -> tdee + 400.0
            "Menambah Massa Otot" -> tdee + 500.0
            else -> tdee // "Menjaga Berat Badan"
        }

        // 5. Protein target (grams per kg body weight)
        val proteinMultiplier = when (goalType) {
            "Menurunkan Berat Badan" -> 1.6
            "Menambah Berat Badan" -> 1.8
            "Menambah Massa Otot" -> 2.2
            else -> 1.4 // "Menjaga Berat Badan"
        }
        val targetProtein = weight * proteinMultiplier

        // 6. Fat target (25% of target calories, 9 calories/gram)
        val targetFat = (targetCalorie * 0.25) / 9.0

        // 7. Carb target (Remaining calories, 4 calories/gram)
        val proteinCalories = targetProtein * 4.0
        val fatCalories = targetFat * 9.0
        val carbCalories = (targetCalorie - proteinCalories - fatCalories).coerceAtLeast(50.0)
        val targetCarb = carbCalories / 4.0

        // 8. Water target (35 ml * weight in kg)
        val targetWater = 35.0 * weight

        // 9. Ideal weight calculation (BMI 18.5 - 24.9 range)
        val idealWeightMin = 18.5 * (heightInMeters * heightInMeters)
        val idealWeightMax = 24.9 * (heightInMeters * heightInMeters)

        return CalculationResult(
            bmi = (bmi * 10.0).roundToInt() / 10.0,
            category = category,
            bmr = bmr.roundToInt().toDouble(),
            tdee = tdee.roundToInt().toDouble(),
            targetCalorie = targetCalorie.roundToInt().toDouble(),
            targetProtein = targetProtein.roundToInt().toDouble(),
            targetCarb = targetCarb.roundToInt().toDouble(),
            targetFat = targetFat.roundToInt().toDouble(),
            targetWater = targetWater.roundToInt().toDouble(),
            idealWeightMin = (idealWeightMin * 10.0).roundToInt() / 10.0,
            idealWeightMax = (idealWeightMax * 10.0).roundToInt() / 10.0
        )
    }
}
