package com.example.data.models

data class UserProfile(
    val id: Int = 1,
    val age: Int = 24,
    val weightKg: Double = 70.0,
    val heightCm: Double = 175.0,
    val isMale: Boolean = true,
    val activityLevel: String = "Moderate", // "Sedentary", "Light", "Moderate", "Active", "Athlete"
    val goal: String = "Muscle Gain", // "Weight Loss", "Muscle Gain", "Body Recomposition", "Weight Gain"
    val bmr: Double = 1650.0,
    val tdee: Double = 2500.0,
    val calorieTarget: Double = 2800.0,
    val proteinTargetG: Double = 140.0,
    val carbTargetG: Double = 330.0,
    val fatTargetG: Double = 77.0
)

data class FoodItem(
    val id: String,
    val name: String,
    val servingSize: String,
    val servingWeightG: Double,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double = 0.0,
    val sugar: Double = 0.0,
    // Vitamins
    val vitA: Double = 0.0, // IU
    val vitC: Double = 0.0, // mg
    val vitD: Double = 0.0, // IU
    val vitE: Double = 0.0, // mg
    val vitK: Double = 0.0, // mcg
    val vitB1: Double = 0.0, // mg
    val vitB2: Double = 0.0, // mg
    val vitB3: Double = 0.0, // mg
    val vitB6: Double = 0.0, // mg
    val vitB12: Double = 0.0, // mcg
    // Minerals
    val iron: Double = 0.0, // mg
    val calcium: Double = 0.0, // mg
    val potassium: Double = 0.0, // mg
    val sodium: Double = 0.0, // mg
    val magnesium: Double = 0.0, // mg
    val zinc: Double = 0.0, // mg
    val phosphorus: Double = 0.0, // mg
    val isPakistani: Boolean = true
)

data class CompanionOption(
    val name: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val icon: String
)

data class SetLog(
    val setNum: Int,
    var weight: Double,
    var reps: Int,
    var isDone: Boolean = false
)

data class Exercise(
    val id: String,
    val name: String,
    val muscleGroup: String,
    val equipment: String,
    val difficulty: String, // "Beginner", "Intermediate", "Advanced"
    val thumbnail: String,
    val instructions: List<String>,
    val muscleMap: String,
    val commonMistakes: List<String>,
    val setsByGoal: String = "3 sets x 10 reps"
)

data class WorkoutPlan(
    val id: String,
    val name: String,
    val description: String,
    val difficulty: String,
    val exerciseIds: List<String>
)

data class BodyWeightLog(
    val date: String, // YYYY-MM-DD
    val weightKg: Double,
    val notes: String = ""
)

data class NotificationSettings(
    val gymAlarmTime: String = "17:30",
    val gymAlarmEnabled: Boolean = true,
    val mealAlarmEnabled: Boolean = true,
    val mealAlarmTime: String = "13:00",
    val waterIntervalHours: Int = 2,
    val supplementsEnabled: Boolean = true,
    val weeklyWeighInEnabled: Boolean = true,
    val dailySummaryEnabled: Boolean = true,
    val morningMotivationEnabled: Boolean = true
)

data class MealLog(
    val id: Int = 0,
    val date: String,
    val mealSlot: String,
    val name: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val multiplier: Double = 1.0,
    val servingDesc: String = "1 serving"
)

data class GymLog(
    val date: String,
    val didGo: String,
    val durationMinutes: Int,
    val workoutId: String,
    val volumeKb: Double,
    val exercisesCount: Int
)

data class WaterLog(
    val date: String,
    val amountMl: Int,
    val goalMl: Int
)

data class Supplement(
    val id: Int = 0,
    val name: String,
    val dosage: String,
    val time: String,
    val isTaken: Boolean = false,
    val streak: Int = 0,
    val lastTakenDate: String = ""
)

