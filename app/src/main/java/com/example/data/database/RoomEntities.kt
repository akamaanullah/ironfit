package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val id: Int = 1,
    val age: Int,
    val weightKg: Double,
    val heightCm: Double,
    val isMale: Boolean,
    val activityLevel: String,
    val goal: String,
    val bmr: Double,
    val tdee: Double,
    val calorieTarget: Double,
    val proteinTargetG: Double,
    val carbTargetG: Double,
    val fatTargetG: Double
)

@Entity(tableName = "meals")
data class MealLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // YYYY-MM-DD
    val mealSlot: String, // Breakfast, Lunch, Dinner, Snacks, Pre-Workout, Post-Workout
    val name: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val multiplier: Double,
    val servingDesc: String
)

@Entity(tableName = "gym_logs")
data class GymLogEntity(
    @PrimaryKey val date: String, // YYYY-MM-DD
    val didGo: String, // YES, NO, REST
    val durationMinutes: Int,
    val workoutPlanName: String,
    val exercisesCount: Int,
    val volumeKg: Double
)

@Entity(tableName = "water_logs")
data class WaterLogEntity(
    @PrimaryKey val date: String, // YYYY-MM-DD
    val amountMl: Int,
    val goalMl: Int
)

@Entity(tableName = "supplements")
data class SupplementEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val dosage: String,
    val time: String,
    val isTaken: Boolean,
    val streak: Int,
    val lastTakenDate: String
)
