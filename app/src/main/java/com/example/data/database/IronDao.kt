package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface IronDao {

    @Query("SELECT * FROM profile WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<ProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity)

    @Query("SELECT * FROM meals WHERE date = :date")
    fun getMealsForDate(date: String): Flow<List<MealLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealLogEntity)

    @Query("DELETE FROM meals WHERE id = :id")
    suspend fun deleteMeal(id: Int)

    @Query("SELECT * FROM gym_logs WHERE date = :date LIMIT 1")
    fun getGymLog(date: String): Flow<GymLogEntity?>

    @Query("SELECT * FROM gym_logs ORDER BY date DESC")
    fun getAllGymLogs(): Flow<List<GymLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGymLog(gymLog: GymLogEntity)

    @Query("SELECT * FROM water_logs WHERE date = :date LIMIT 1")
    fun getWaterLog(date: String): Flow<WaterLogEntity?>

    @Query("SELECT * FROM water_logs ORDER BY date DESC")
    fun getAllWaterLogs(): Flow<List<WaterLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterLog(waterLog: WaterLogEntity)

    @Query("SELECT * FROM supplements")
    fun getAllSupplements(): Flow<List<SupplementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplement(supplement: SupplementEntity)

    @Query("DELETE FROM supplements WHERE id = :id")
    suspend fun deleteSupplement(id: Int)
}
