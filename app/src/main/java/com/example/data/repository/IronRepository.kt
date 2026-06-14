package com.example.data.repository

import com.example.data.database.*
import com.example.data.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.math.roundToInt

class IronRepository(private val ironDao: IronDao) {

    // --- Hardcoded/Loaded Static Database Foods ---
    val foodDatabase = listOf(
        // Pakistani Foods
        FoodItem("pk_roti", "Roti", "1 piece (40g)", 40.0, 120.0, 4.0, 23.0, 2.0, iron = 1.5, isPakistani = true),
        FoodItem("pk_chapati_maida", "Chapati Maida", "1 piece (35g)", 35.0, 110.0, 3.0, 22.0, 1.5, isPakistani = true),
        FoodItem("pk_paratha_plain", "Paratha Plain", "1 piece (80g)", 80.0, 280.0, 5.0, 35.0, 13.0, iron = 1.2, isPakistani = true),
        FoodItem("pk_paratha_aloo", "Paratha Aloo", "1 piece (120g)", 120.0, 320.0, 6.0, 45.0, 12.0, potassium = 380.0, isPakistani = true),
        FoodItem("pk_naan", "Naan", "1 piece (90g)", 90.0, 270.0, 9.0, 50.0, 4.0, iron = 2.0, isPakistani = true),
        FoodItem("pk_rice_white", "White Rice cooked", "1 cup (186g)", 186.0, 242.0, 4.0, 53.0, 0.4, isPakistani = true),
        FoodItem("pk_biryani_chicken", "Biryani Chicken", "1 plate (350g)", 350.0, 520.0, 28.0, 62.0, 16.0, zinc = 2.1, isPakistani = true),
        FoodItem("pk_chicken_karahi", "Chicken Karahi", "1 portion (300g)", 300.0, 420.0, 35.0, 8.0, 28.0, vitC = 15.0, isPakistani = true),
        FoodItem("pk_nihari", "Nihari beef", "1 portion (300g)", 300.0, 480.0, 32.0, 12.0, 34.0, iron = 4.5, zinc = 5.0, isPakistani = true),
        FoodItem("pk_haleem", "Haleem", "1 plate (250g)", 250.0, 350.0, 22.0, 28.0, 16.0, iron = 5.0, fiber = 6.0, isPakistani = true),
        FoodItem("pk_daal_chawal", "Daal Chawal", "1 plate (400g)", 400.0, 440.0, 18.0, 75.0, 8.0, iron = 6.0, fiber = 10.0, isPakistani = true),
        FoodItem("pk_palak_gosht", "Palak Gosht", "1 portion (280g)", 280.0, 380.0, 28.0, 10.0, 26.0, vitA = 4500.0, iron = 5.5, isPakistani = true),
        FoodItem("pk_saag", "Saag", "1 portion (200g)", 200.0, 160.0, 7.0, 14.0, 9.0, vitA = 6000.0, iron = 4.0, calcium = 200.0, isPakistani = true),
        FoodItem("pk_beef_lean", "Beef lean", "100g", 100.0, 250.0, 26.0, 0.0, 16.0, iron = 3.0, zinc = 5.5, vitB12 = 2.5, isPakistani = true),
        FoodItem("pk_mutton", "Mutton", "100g", 100.0, 258.0, 25.0, 0.0, 17.0, iron = 2.5, zinc = 4.5, isPakistani = true),
        FoodItem("pk_chicken_breast", "Chicken Breast", "100g", 100.0, 165.0, 31.0, 0.0, 3.6, vitB3 = 13.0, isPakistani = true),
        FoodItem("pk_egg_boiled", "Egg Boiled", "1 piece (50g)", 50.0, 78.0, 6.0, 0.6, 5.0, vitD = 44.0, isPakistani = true),
        FoodItem("pk_milk_full", "Milk Full Fat", "1 glass (240ml)", 240.0, 149.0, 8.0, 12.0, 8.0, calcium = 276.0, vitD = 98.0, isPakistani = true),
        FoodItem("pk_dahi", "Dahi", "100g", 100.0, 61.0, 3.5, 4.7, 3.3, calcium = 121.0, isPakistani = true),
        FoodItem("pk_mango_shake", "Mango Shake", "1 glass (300ml)", 300.0, 280.0, 5.0, 52.0, 7.0, vitA = 800.0, vitC = 30.0, isPakistani = true),
        FoodItem("pk_banana_shake", "Banana Shake", "1 glass (300ml)", 300.0, 310.0, 7.0, 55.0, 8.0, potassium = 600.0, vitB6 = 0.5, isPakistani = true),
        FoodItem("pk_chai", "Chai with Milk", "1 cup (200ml)", 200.0, 80.0, 3.0, 10.0, 3.5, calcium = 90.0, isPakistani = true),
        FoodItem("pk_samosa", "Samosa", "1 piece (60g)", 60.0, 180.0, 4.0, 22.0, 9.0, sodium = 280.0, isPakistani = true),
        FoodItem("pk_seekh_kebab", "Seekh Kebab", "1 piece (60g)", 60.0, 145.0, 14.0, 4.0, 8.0, iron = 1.8, isPakistani = true),
        FoodItem("pk_dates", "Dates", "3 pieces (24g)", 24.0, 66.0, 0.4, 18.0, 0.0, iron = 0.3, potassium = 167.0, isPakistani = true),
        FoodItem("pk_almonds", "Almonds", "10 pieces (12g)", 12.0, 69.0, 2.5, 2.5, 6.0, vitE = 3.5, magnesium = 30.0, isPakistani = true),
        FoodItem("pk_oats", "Oats dry", "50g", 50.0, 188.0, 7.0, 32.0, 3.5, fiber = 5.0, iron = 2.0, magnesium = 56.0, isPakistani = true),
        FoodItem("pk_peanut_butter", "Peanut Butter", "1 tbsp (16g)", 16.0, 94.0, 4.0, 3.5, 8.0, magnesium = 27.0, isPakistani = true),

        // International Foods
        FoodItem("int_banana", "Banana", "1 piece (118g)", 118.0, 105.0, 1.3, 27.0, 0.4, potassium = 422.0, vitC = 10.0, isPakistani = false),
        FoodItem("int_brown_rice", "Brown Rice cooked", "1 cup (195g)", 195.0, 216.0, 5.0, 45.0, 1.8, fiber = 3.5, magnesium = 84.0, isPakistani = false),
        FoodItem("int_chicken_breast", "Chicken Breast (Int)", "100g", 100.0, 165.0, 31.0, 0.0, 3.6, vitB3 = 13.0, isPakistani = false),
        FoodItem("int_salmon", "Salmon", "100g", 100.0, 208.0, 20.0, 0.0, 13.0, vitD = 526.0, isPakistani = false),
        FoodItem("int_greek_yogurt", "Greek Yogurt", "100g", 100.0, 59.0, 10.0, 3.6, 0.4, calcium = 111.0, isPakistani = false),
        FoodItem("int_sweet_potato", "Sweet Potato", "100g", 100.0, 86.0, 1.6, 20.0, 0.1, vitA = 19218.0, isPakistani = false),
        FoodItem("int_whey", "Whey Protein Shake", "1 scoop (250ml)", 250.0, 120.0, 24.0, 3.0, 2.0, calcium = 130.0, isPakistani = false)
    )

    // Companion additions logic maps for step 2 meal builder
    val companionSuggestions = mapOf(
        "pk_roti" to listOf(
            CompanionOption("Anda (Egg) 🍳", 78.0, 6.0, 0.6, 5.0, "🍳"),
            CompanionOption("Dahi (Yogurt) 🥛", 61.0, 3.5, 4.7, 3.3, "🥛"),
            CompanionOption("Chai ☕", 80.0, 3.0, 10.0, 3.5, "☕"),
            CompanionOption("Butter 🧈", 100.0, 0.1, 0.1, 11.0, "🧈")
        ),
        "pk_paratha_plain" to listOf(
            CompanionOption("Anda (Egg) 🍳", 78.0, 6.0, 0.6, 5.0, "🍳"),
            CompanionOption("Chai ☕", 80.0, 3.0, 10.0, 3.5, "☕"),
            CompanionOption("Achar 🌶️", 40.0, 0.2, 2.0, 3.5, "🌶️"),
            CompanionOption("Dahi (Yogurt) 🥛", 61.0, 3.5, 4.7, 3.3, "🥛")
        ),
        "pk_naan" to listOf(
            CompanionOption("Chicken Karahi 🍗", 420.0, 35.0, 8.0, 28.0, "🍗"),
            CompanionOption("Nihari Beef 🍲", 480.0, 32.0, 12.0, 34.0, "🍲"),
            CompanionOption("Seekh Kebab 🍢", 145.0, 14.0, 4.0, 8.0, "🍢")
        )
    )

    // --- Hardcoded Exercises ---
    val exerciseLibrary = listOf(
        Exercise("ex_bench_press", "Bench Press", "Chest", "Barbell", "Intermediate", "https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b", listOf("Lie flat on your back on a bench.", "Grip the barbell with hands slightly wider than shoulder-width.", "Brace your core and lower the bar slowly to your chest.", "Push the bar back up powerfully while expanding your chest."), "Chest & Triceps", listOf("Flaring elbows outward excessively", "Bouncing the bar off your sternum", "Lifting hips off the bench bench"), "4 sets x 10 reps"),
        Exercise("ex_squat", "Squat", "Legs", "Barbell", "Advanced", "https://images.unsplash.com/photo-1574680096145-d05b474e2155", listOf("Place barbell on lower traps.", "Position feet shoulder-width, toes slightly out.", "Inhale deep, brace core, and push hips back.", "Squat below parallel, keep knees in line with toes.", "Drive back up through mid-foot."), "Quads, Glutes & Hamstrings", listOf("Knees caving inwards", "Heels rising off the ground", "Lower back rounding at the bottom"), "4 sets x 8 reps"),
        Exercise("ex_deadlift", "Deadlift", "Back/Legs", "Barbell", "Advanced", "https://images.unsplash.com/photo-1517838277536-f5f99be501cd", listOf("Stand with feet hip-width under bar.", "Bend at hips and knees, grip bar outside shins.", "Flatten your back and engage lat muscles.", "Drive floor away with legs to stand up.", "Keep bar close to body throughout."), "Hamstrings, Glutes, Lower Back & Lats", listOf("Rounding your lumbar spine", "Bar drifting too far forward", "Jerking the barbell off the floor"), "3 sets x 5 reps"),
        Exercise("ex_overhead_press", "Overhead Press", "Shoulders", "Barbell", "Intermediate", "https://images.unsplash.com/photo-1541534741688-6078c6bfb5c5", listOf("Rack barbell at upper chest level.", "Grip bar just outside shoulders.", "Brace core, squeeze glutes, press bar straight up.", "Tilt head back slightly as bar passes your face.", "Lock out shoulders at the top."), "Anterior Deltoids, Triceps & Core", listOf("Hyperextending the lower back", "Not locking out elbows at the top", "Bouncing with knees"), "3 sets x 10 reps"),
        Exercise("ex_pullups", "Pull-ups", "Back", "Bodyweight", "Intermediate", "https://images.unsplash.com/photo-1598971639058-fab3c3109a00", listOf("Hang from a bar with palms facing away.", "Depress shoulder blades down.", "Pull chest up to the bar using elbow drive.", "Lower down slowly with full extension."), "Lats, Rhomboids & Biceps", listOf("Kicking legs for momentum (kipping)", "Not completing full range of motion", "Shrugging neck at the top"), "3 sets x max reps"),
        Exercise("ex_pushups", "Push-ups", "Chest", "Bodyweight", "Beginner", "https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b", listOf("Place hands shoulder-width on floor.", "Keep body in perfect straight line.", "Lower chest to ground keeping elbows 45°.", "Push floor away back to setup position."), "Chest, Shoulders & Triceps", listOf("Sagging lower back", "Elbows flared out 90 degrees", "Half reps"), "3 sets x 15 reps"),
        Exercise("ex_bicep_curls", "Bicep Curls", "Arms", "Dumbbell", "Beginner", "https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e", listOf("Hold dumbbells at sides with palms forward.", "Keep elbows pinned to ribcage.", "Curl weights up toward shoulders.", "Lower down slowly with squeeze."), "Bicep Brachii & Brachialis", listOf("Swinging upper body for leverage", "Elbows drifting forward", "Dropping weights fast"), "3 sets x 12 reps"),
        Exercise("ex_tricep_dips", "Tricep Dips", "Arms", "Bodyweight", "Beginner", "https://images.unsplash.com/photo-1534438327276-14e5300c3a48", listOf("Place hands behind hips on standard chair/bench.", "Extend legs forward and lift hips.", "Lower hips by bending elbows to 90°.", "Press back up to lock out triceps."), "Triceps & Anterior Deltoids", listOf("Shoulders rolling forward", "Going way too deep causing injury", "Not locking triceps out"), "3 sets x 12 reps"),
        Exercise("ex_db_rows", "Dumbbell Rows", "Back", "Dumbbell", "Beginner", "https://images.unsplash.com/photo-1605296867304-46d5465a25f1", listOf("Place one knee and hand on flat bench.", "Hold dumbbell in other hand hanging down.", "Pull dumbbell to hip keeping elbow close.", "Squeeze shoulder blade and return slow."), "Lats, Rear Delts & Biceps", listOf("Rounding upper back", "Twisting torso at the top", "Rushing tempo"), "3 sets x 12 reps"),
        Exercise("ex_lunge", "Lunge", "Legs", "Dumbbell", "Beginner", "https://images.unsplash.com/photo-1434608519344-49d77a699e1d", listOf("Stand tall, arms holding dumbbells.", "Take large step forward with one leg.", "Lower hips until back knee is near floor.", "Push back up off front heel."), "Quads, Glutes & Hamstrings", listOf("Step is too short (knee past toe)", "Torso leaning too far forwards", "Inward knee wobble"), "3 sets x 12 reps"),
        Exercise("ex_plank", "Plank", "Abs", "Bodyweight", "Beginner", "https://images.unsplash.com/photo-1517838277536-f5f99be501cd", listOf("Place forearms on floor, shoulders aligned.", "Extend legs behind, weight on toes.", "Drive elbows into floor, squeeze glutes.", "Maintain rigid straight line, breathe deeply."), "Rectus Abdominis, Obliques & Lower Back", listOf("Sagging hips toward floor", "Butt sticking high in air", "Holding breath"), "3 sets x 60s")
    )

    // --- Gym Workout Plans ---
    val gymPlans = listOf(
        WorkoutPlan("plan_fullbody", "Full Body Split", "Great for general conditioning and efficiency.", "Beginner", listOf("ex_squat", "ex_bench_press", "ex_db_rows", "ex_plank")),
        WorkoutPlan("plan_pushpull", "Push Pull Legs", "Target push, pull, and leg muscles separated.", "Intermediate", listOf("ex_bench_press", "ex_overhead_press", "ex_squat", "ex_pullups", "ex_bicep_curls")),
        WorkoutPlan("plan_home", "Home Workout Momentum", "Zero equipment required to stay active.", "Beginner", listOf("ex_pushups", "ex_tricep_dips", "ex_lunge", "ex_plank"))
    )

    // --- SQLite Database Bindings via Flow Mapping ---
    val profile: Flow<UserProfile?> = ironDao.getProfile().map { entity ->
        entity?.let {
            UserProfile(
                id = it.id,
                age = it.age,
                weightKg = it.weightKg,
                heightCm = it.heightCm,
                isMale = it.isMale,
                activityLevel = it.activityLevel,
                goal = it.goal,
                bmr = it.bmr,
                tdee = it.tdee,
                calorieTarget = it.calorieTarget,
                proteinTargetG = it.proteinTargetG,
                carbTargetG = it.carbTargetG,
                fatTargetG = it.fatTargetG
            )
        }
    }

    suspend fun saveProfile(profile: UserProfile) {
        ironDao.insertProfile(
            ProfileEntity(
                id = 1,
                age = profile.age,
                weightKg = profile.weightKg,
                heightCm = profile.heightCm,
                isMale = profile.isMale,
                activityLevel = profile.activityLevel,
                goal = profile.goal,
                bmr = profile.bmr,
                tdee = profile.tdee,
                calorieTarget = profile.calorieTarget,
                proteinTargetG = profile.proteinTargetG,
                carbTargetG = profile.carbTargetG,
                fatTargetG = profile.fatTargetG
            )
        )
    }

    fun getMealsForDate(date: String): Flow<List<MealLog>> {
        return ironDao.getMealsForDate(date).map { list ->
            list.map {
                MealLog(
                    id = it.id,
                    date = it.date,
                    mealSlot = it.mealSlot,
                    name = it.name,
                    calories = it.calories,
                    protein = it.protein,
                    carbs = it.carbs,
                    fat = it.fat,
                    multiplier = it.multiplier,
                    servingDesc = it.servingDesc
                )
            }
        }
    }

    suspend fun logMeal(meal: MealLog) {
        ironDao.insertMeal(
            MealLogEntity(
                id = meal.id,
                date = meal.date,
                mealSlot = meal.mealSlot,
                name = meal.name,
                calories = meal.calories,
                protein = meal.protein,
                carbs = meal.carbs,
                fat = meal.fat,
                multiplier = meal.multiplier,
                servingDesc = meal.servingDesc
            )
        )
    }

    suspend fun deleteMeal(id: Int) {
        ironDao.deleteMeal(id)
    }

    fun getGymLog(date: String): Flow<GymLog?> {
        return ironDao.getGymLog(date).map { entity ->
            entity?.let {
                GymLog(
                    date = it.date,
                    didGo = it.didGo,
                    durationMinutes = it.durationMinutes,
                    workoutId = it.workoutPlanName,
                    volumeKb = it.volumeKg,
                    exercisesCount = it.exercisesCount
                )
            }
        }
    }

    fun getAllGymLogs(): Flow<List<GymLog>> {
        return ironDao.getAllGymLogs().map { list ->
            list.map {
                GymLog(
                    date = it.date,
                    didGo = it.didGo,
                    durationMinutes = it.durationMinutes,
                    workoutId = it.workoutPlanName,
                    volumeKb = it.volumeKg,
                    exercisesCount = it.exercisesCount
                )
            }
        }
    }

    suspend fun saveGymLog(log: GymLog) {
        ironDao.insertGymLog(
            GymLogEntity(
                date = log.date,
                didGo = log.didGo,
                durationMinutes = log.durationMinutes,
                workoutPlanName = log.workoutId,
                volumeKg = log.volumeKb,
                exercisesCount = log.exercisesCount
            )
        )
    }

    fun getWaterLog(date: String): Flow<WaterLog?> {
        return ironDao.getWaterLog(date).map { entity ->
            entity?.let {
                WaterLog(
                    date = it.date,
                    amountMl = it.amountMl,
                    goalMl = it.goalMl
                )
            }
        }
    }

    suspend fun saveWaterLog(log: WaterLog) {
        ironDao.insertWaterLog(
            WaterLogEntity(
                date = log.date,
                amountMl = log.amountMl,
                goalMl = log.goalMl
            )
        )
    }

    fun getAllSupplements(): Flow<List<Supplement>> {
        return ironDao.getAllSupplements().map { list ->
            list.map {
                Supplement(
                    id = it.id,
                    name = it.name,
                    dosage = it.dosage,
                    time = it.time,
                    isTaken = it.isTaken,
                    streak = it.streak,
                    lastTakenDate = it.lastTakenDate
                )
            }
        }
    }

    suspend fun saveSupplement(supp: Supplement) {
        ironDao.insertSupplement(
            SupplementEntity(
                id = supp.id,
                name = supp.name,
                dosage = supp.dosage,
                time = supp.time,
                isTaken = supp.isTaken,
                streak = supp.streak,
                lastTakenDate = supp.lastTakenDate
            )
        )
    }

    suspend fun deleteSupplement(id: Int) {
        ironDao.deleteSupplement(id)
    }

    // --- BMI & Diet Planner Logic ---
    fun calculateBMI(weightKg: Double, heightCm: Double): Double {
        val heightM = heightCm / 100.0
        if (heightM == 0.0) return 0.0
        val bmi = weightKg / (heightM * heightM)
        return (bmi * 10.0).roundToInt() / 10.0
    }

    fun getBMICategory(bmi: Double): String {
        return when {
            bmi < 18.5 -> "Underweight"
            bmi < 25.0 -> "Normal"
            bmi < 30.0 -> "Overweight"
            else -> "Obese"
        }
    }

    fun calculateBmrAndTdee(weightKg: Double, heightCm: Double, age: Int, isMale: Boolean, activityLevel: String): Pair<Double, Double> {
        // Mifflin-St Jeor Formula
        val bmr = if (isMale) {
            (10.0 * weightKg) + (6.25 * heightCm) - (5.0 * age) + 5.0
        } else {
            (10.0 * weightKg) + (6.25 * heightCm) - (5.0 * age) - 161.0
        }

        val multiplier = when (activityLevel) {
            "Sedentary" -> 1.2
            "Light" -> 1.375
            "Moderate" -> 1.55
            "Active" -> 1.725
            "Athlete" -> 1.9
            else -> 1.2
        }

        val tdee = bmr * multiplier
        return Pair(bmr.roundToInt().toDouble(), tdee.roundToInt().toDouble())
    }

    fun generateTargets(weightKg: Double, tdee: Double, goal: String): Triple<Double, Double, Triple<Double, Double, Double>> {
        val calorieTarget = when (goal) {
            "Weight Loss" -> tdee - 500.0
            "Muscle Gain" -> tdee + 300.0
            "Body Recomposition" -> tdee
            "Weight Gain" -> tdee + 500.0
            else -> tdee
        }

        // Macro Split: Protein 2.2g/kg (loss), 1.8g/kg (gain), 2.0g/kg (recomp), 22g/kg (gain/loss rest)
        val proteinG = when (goal) {
            "Weight Loss" -> 2.2 * weightKg
            "Muscle Gain" -> 1.8 * weightKg
            "Body Recomposition" -> 2.0 * weightKg
            "Weight Gain" -> 1.8 * weightKg
            else -> 2.0 * weightKg
        }.roundToInt().toDouble()

        // Fat is 25% of calories (recomp is 30%)
        val fatPercent = if (goal == "Body Recomposition") 0.3 else 0.25
        val fatG = ((calorieTarget * fatPercent) / 9.0).roundToInt().toDouble()

        // Carbs are the remaining calories
        val remainingCal = calorieTarget - (proteinG * 4.0) - (fatG * 9.0)
        val carbsG = (if (remainingCal > 0) remainingCal / 4.0 else 50.0).roundToInt().toDouble()

        return Triple(calorieTarget, proteinG, Triple(carbsG, fatG, calorieTarget))
    }

    // Suggested diet plan items by category
    fun getBMIPreFilledSuggestedMeals(bmiCategory: String, goal: String): Map<String, List<MealLog>> {
        // Returns 6 slots with suggestions
        val isUnderweight = bmiCategory == "Underweight"
        val isOverweight = bmiCategory == "Overweight" || bmiCategory == "Obese"

        val breakfast = if (isUnderweight) {
            listOf(
                MealLog(0, "", "Breakfast", "Paratha Plain + 2 Boiled Eggs", 436.0, 17.0, 36.0, 23.0),
                MealLog(0, "", "Breakfast", "Whole Milk (Full Fat) 240ml", 149.0, 8.0, 12.0, 8.0)
            )
        } else if (isOverweight) {
            listOf(
                MealLog(0, "", "Breakfast", "Oats dry (50g) + Water", 188.0, 7.0, 32.0, 3.5),
                MealLog(0, "", "Breakfast", "2 Boiled Eggs (Remove 1 Yolk)", 110.0, 9.0, 0.6, 5.0)
            )
        } else { // Normal & Muscle Gain
            listOf(
                MealLog(0, "", "Breakfast", "Oats (50g) + Full Fat Milk", 337.0, 15.0, 44.0, 11.5),
                MealLog(0, "", "Breakfast", "2 Boiled Eggs", 156.0, 12.0, 1.2, 10.0)
            )
        }

        val lunch = if (isOverweight) {
            listOf(MealLog(0, "", "Lunch", "Roti (1 piece, 40g) + Chicken Karahi (150g, Skinned)", 330.0, 21.0, 27.0, 16.0))
        } else if (isUnderweight) {
            listOf(
                MealLog(0, "", "Lunch", "Biryani Chicken (large portion, 350g)", 520.0, 28.0, 62.0, 16.0),
                MealLog(0, "", "Lunch", "Dahi (100g) with cucumber", 61.0, 3.5, 4.7, 3.3)
            )
        } else {
            listOf(MealLog(0, "", "Lunch", "Roti (1) + Palak Gosht (280g) + Dahi (100g)", 561.0, 35.0, 37.7, 31.3))
        }

        val snacks = if (isUnderweight) {
            listOf(
                MealLog(0, "", "Snacks", "10 Almonds + 3 Dates", 135.0, 2.9, 20.5, 6.0),
                MealLog(0, "", "Snacks", "Peanut Butter (1tbsp)", 94.0, 4.0, 3.5, 8.0)
            )
        } else if (isOverweight) {
            listOf(MealLog(0, "", "Snacks", "Green Yogurt (100g)", 59.0, 10.0, 3.6, 0.4))
        } else {
            listOf(MealLog(0, "", "Snacks", "10 Almonds + 3 Dates", 135.0, 2.9, 20.5, 6.0))
        }

        val preWorkout = listOf(
            MealLog(0, "", "Pre-Workout", "Oats (50g) + Banana Combo", 293.0, 8.3, 59.0, 3.9)
        )

        val postWorkout = if (goal == "Muscle Gain" || isUnderweight) {
            listOf(
                MealLog(0, "", "Post-Workout", "Whey Protein Shake", 120.0, 24.0, 3.0, 2.0),
                MealLog(0, "", "Post-Workout", "Banana Shake (300ml)", 310.0, 7.0, 55.0, 8.0)
            )
        } else {
            listOf(
                MealLog(0, "", "Post-Workout", "Chicken Breast (150g grilled)", 247.0, 46.5, 0.0, 5.4),
                MealLog(0, "", "Post-Workout", "Boiled Egg", 78.0, 6.0, 0.6, 5.0)
            )
        }

        val dinner = if (isOverweight) {
            listOf(MealLog(0, "", "Dinner", "Chicken Breast (150g grilled) + Saag (100g)", 327.0, 50.0, 7.0, 9.9))
        } else if (isUnderweight) {
            listOf(MealLog(0, "", "Dinner", "Daal Chawal (400g) + Seekh Kebab (60g)", 585.0, 32.0, 79.0, 16.0))
        } else {
            listOf(MealLog(0, "", "Dinner", "Roti (1) + Chicken Karahi (200g) + Salad", 400.0, 27.3, 28.3, 20.6))
        }

        return mapOf(
            "Breakfast" to breakfast,
            "Lunch" to lunch,
            "Snacks" to snacks,
            "Pre-Workout" to preWorkout,
            "Post-Workout" to postWorkout,
            "Dinner" to dinner
        )
    }
}
