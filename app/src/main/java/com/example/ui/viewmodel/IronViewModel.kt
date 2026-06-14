package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.IronDatabase
import com.example.data.models.*
import com.example.data.repository.IronRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

enum class ActiveScreen {
    SPLASH,
    ONBOARDING,
    MAIN_TAB,
    ACTIVE_WORKOUT,
    POST_WORKOUT_SUMMARY,
    ADD_FOOD_FLOW,
    NURTITION_DETAIL,
    NOTIFICATION_SETTINGS
}

class IronViewModel(application: Application) : AndroidViewModel(application) {

    private val database = IronDatabase.getDatabase(application)
    private val repository = IronRepository(database.ironDao())

    // --- Core Navigation States ---
    val activeScreen = MutableStateFlow(ActiveScreen.SPLASH)
    val currentTab = MutableStateFlow(0) // 0: Home, 1: Gym, 2: Meals, 3: Progress, 4: Profile

    // --- Onboarding Temporary State ---
    val onboardStep = MutableStateFlow(0) // 0 to 3
    val onboardAge = MutableStateFlow("24")
    val onboardWeight = MutableStateFlow("70")
    val onboardHeight = MutableStateFlow("175")
    val onboardIsMale = MutableStateFlow(true)
    val onboardActivity = MutableStateFlow("Moderate")
    val onboardGoal = MutableStateFlow("Muscle Gain")

    // --- Theme State (Default Dark first!) ---
    val isDarkTheme = MutableStateFlow(true)

    // --- Active Date State ---
    val currentDateString = MutableStateFlow("")

    // --- Reactive Data Flows ---
    val userProfile: StateFlow<UserProfile?> = repository.profile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val mealsToday: StateFlow<List<MealLog>> = currentDateString.flatMapLatest { date ->
        if (date.isEmpty()) flowOf<List<MealLog>>(emptyList()) else repository.getMealsForDate(date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val waterToday: StateFlow<WaterLog?> = currentDateString.flatMapLatest { date ->
        if (date.isEmpty()) flowOf<WaterLog?>(null) else repository.getWaterLog(date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val gymLogs: StateFlow<List<GymLog>> = repository.getAllGymLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val supplements: StateFlow<List<Supplement>> = repository.getAllSupplements()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Food Database and Meals Flow ---
    val foodDatabaseList = repository.foodDatabase
    val selectedMealSlot = MutableStateFlow("Breakfast")
    val foodSearchQuery = MutableStateFlow("")
    val activeFoodCategory = MutableStateFlow("All") // "All", "Pakistani", "International"

    // Search results
    val searchedFoods: StateFlow<List<FoodItem>> = combine(
        foodSearchQuery,
        activeFoodCategory
    ) { query, category ->
        var list = foodDatabaseList
        if (category == "Pakistani") {
            list = list.filter { it.isPakistani }
        } else if (category == "International") {
            list = list.filter { !it.isPakistani }
        }

        if (query.trim().isNotEmpty()) {
            list = list.filter { it.name.contains(query, ignoreCase = true) }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), foodDatabaseList)

    // Step 2 Meal logging
    val selectedFoodForLog = MutableStateFlow<FoodItem?>(null)
    val logMultiplier = MutableStateFlow(1.0)
    val companionsChecked = MutableStateFlow<Map<String, Boolean>>(emptyMap())

    // --- Active Workout Stopwatch State ---
    val activeWorkoutPlan = MutableStateFlow<WorkoutPlan?>(null)
    val isWorkoutActive = MutableStateFlow(false)
    val workoutTimerSeconds = MutableStateFlow(0)
    private var timerJob: Job? = null

    // Logging list of exercises during active session
    val activeSessionExercises = MutableStateFlow<List<Pair<Exercise, List<SetLog>>>>(emptyList())
    val activeExerciseIndex = MutableStateFlow(0)

    // Rest timer countdown
    val restTimerSeconds = MutableStateFlow(0)
    private var restTimerJob: Job? = null

    // Summary results
    var summaryDuration = ""
    var summaryWorkoutName = ""
    var summaryVolumeKg = 0.0
    var summaryExercisesCount = 0
    var summaryCaloriesBurned = 0.0

    // Notification settings
    val notificationsSettings = MutableStateFlow(NotificationSettings())

    init {
        // Initialize Date Today
        val sdf = SimpleDateFormat("yyyy-MM-DD", Locale.US)
        currentDateString.value = sdf.format(Date())

        viewModelScope.launch {
            delay(1800) // Splash delay
            userProfile.collectLatest { profile ->
                if (activeScreen.value == ActiveScreen.SPLASH) {
                    if (profile == null) {
                        activeScreen.value = ActiveScreen.ONBOARDING
                    } else {
                        activeScreen.value = ActiveScreen.MAIN_TAB
                    }
                }
            }
        }

        // Preseed supplements if empty
        viewModelScope.launch {
            supplements.collectLatest { list ->
                if (list.isEmpty()) {
                    repository.saveSupplement(Supplement(0, "Omega-3 Fish Oil", "1 capsule", "08:00", streak = 5))
                    repository.saveSupplement(Supplement(0, "Multivitamin", "1 tablet", "09:00", streak = 12))
                    repository.saveSupplement(Supplement(0, "Creatine Monohydrate", "5 grams (1 scoop)", "18:00", streak = 8))
                }
            }
        }
    }

    // --- Change Dates for Logs Mocking ---
    fun setDate(date: String) {
        currentDateString.value = date
    }

    fun toggleTheme() {
        isDarkTheme.value = !isDarkTheme.value
    }

    // --- Onboarding Flow Operations ---
    fun nextOnboardStep() {
        if (onboardStep.value < 3) {
            onboardStep.value += 1
        } else {
            completeOnboarding()
        }
    }

    fun prevOnboardStep() {
        if (onboardStep.value > 0) {
            onboardStep.value -= 1
        }
    }

    private fun completeOnboarding() {
        viewModelScope.launch {
            val ageVal = onboardAge.value.toIntOrNull() ?: 24
            val weightVal = onboardWeight.value.toDoubleOrNull() ?: 70.0
            val heightVal = onboardHeight.value.toDoubleOrNull() ?: 175.0
            val isMaleVal = onboardIsMale.value
            val activityVal = onboardActivity.value
            val goalVal = onboardGoal.value

            val (bmr, tdee) = repository.calculateBmrAndTdee(weightVal, heightVal, ageVal, isMaleVal, activityVal)
            val (calTarget, protTarget, macroTriple) = repository.generateTargets(weightVal, tdee, goalVal)
            val carbTarget = macroTriple.first
            val fatTarget = macroTriple.second

            val profile = UserProfile(
                id = 1,
                age = ageVal,
                weightKg = weightVal,
                heightCm = heightVal,
                isMale = isMaleVal,
                activityLevel = activityVal,
                goal = goalVal,
                bmr = bmr,
                tdee = tdee,
                calorieTarget = calTarget,
                proteinTargetG = protTarget,
                carbTargetG = carbTarget,
                fatTargetG = fatTarget
            )

            repository.saveProfile(profile)
            // Initialize Water for modern display compatibility
            repository.saveWaterLog(WaterLog(currentDateString.value, 0, 3000))

            activeScreen.value = ActiveScreen.MAIN_TAB
        }
    }

    fun updateProfile(age: Int, weight: Double, height: Double, isMale: Boolean, activity: String, goal: String) {
        viewModelScope.launch {
            val (bmr, tdee) = repository.calculateBmrAndTdee(weight, height, age, isMale, activity)
            val (calTarget, protTarget, macroTriple) = repository.generateTargets(weight, tdee, goal)
            val profile = UserProfile(
                id = 1,
                age = age,
                weightKg = weight,
                heightCm = height,
                isMale = isMale,
                activityLevel = activity,
                goal = goal,
                bmr = bmr,
                tdee = tdee,
                calorieTarget = calTarget,
                proteinTargetG = protTarget,
                carbTargetG = macroTriple.first,
                fatTargetG = macroTriple.second
            )
            repository.saveProfile(profile)
        }
    }

    // --- Gym Module Operations ---
    fun selectWorkoutPlanAndStart(plan: WorkoutPlan) {
        activeWorkoutPlan.value = plan
        workoutTimerSeconds.value = 0
        isWorkoutActive.value = true
        activeExerciseIndex.value = 0

        val sessionExs = plan.exerciseIds.mapNotNull { id ->
            repository.exerciseLibrary.find { it.id == id }
        }.map { exercise ->
            // Pre-seed 3 empty logs
            val defaultSets = listOf(
                SetLog(1, 0.0, 10, false),
                SetLog(2, 0.0, 10, false),
                SetLog(3, 0.0, 10, false)
            )
            Pair(exercise, defaultSets)
        }

        activeSessionExercises.value = sessionExs
        activeScreen.value = ActiveScreen.ACTIVE_WORKOUT

        // Start timer
        startStopwatch()
    }

    private fun startStopwatch() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isWorkoutActive.value) {
                delay(1000)
                workoutTimerSeconds.value += 1
            }
        }
    }

    fun toggleSetCollected(exerciseId: String, setIndex: Int) {
        val updated = activeSessionExercises.value.map { entry ->
            if (entry.first.id == exerciseId) {
                val sets = entry.second.mapIndexed { index, setLog ->
                    if (index == setIndex) {
                        val isNowChecked = !setLog.isDone
                        if (isNowChecked) {
                            startRestTimer(45) // Start a 45 second rest timer
                        }
                        setLog.copy(isDone = isNowChecked)
                    } else {
                        setLog
                    }
                }
                entry.copy(second = sets)
            } else {
                entry
            }
        }
        activeSessionExercises.value = updated
    }

    fun updateSetLogValue(exerciseId: String, setIndex: Int, weight: Double?, reps: Int?) {
        val updated = activeSessionExercises.value.map { entry ->
            if (entry.first.id == exerciseId) {
                val sets = entry.second.mapIndexed { index, setLog ->
                    if (index == setIndex) {
                        setLog.copy(
                            weight = weight ?: setLog.weight,
                            reps = reps ?: setLog.reps
                        )
                    } else {
                        setLog
                    }
                }
                entry.copy(second = sets)
            } else {
                entry
            }
        }
        activeSessionExercises.value = updated
    }

    fun startRestTimer(duration: Int) {
        restTimerSeconds.value = duration
        restTimerJob?.cancel()
        restTimerJob = viewModelScope.launch {
            while (restTimerSeconds.value > 0) {
                delay(1000)
                restTimerSeconds.value -= 1
            }
        }
    }

    fun skipRestTimer() {
        restTimerSeconds.value = 0
        restTimerJob?.cancel()
    }

    fun cancelWorkout() {
        isWorkoutActive.value = false
        timerJob?.cancel()
        restTimerJob?.cancel()
        activeScreen.value = ActiveScreen.MAIN_TAB
        currentTab.value = 1 // Retain Gym
    }

    fun completeWorkout() {
        isWorkoutActive.value = false
        timerJob?.cancel()
        restTimerJob?.cancel()

        // Gather metrics
        val durationMins = (workoutTimerSeconds.value / 60.0).roundToInt().coerceAtLeast(1)
        val plan = activeWorkoutPlan.value
        val name = plan?.name ?: "Custom Workout"

        var totalVolume = 0.0
        var totalCount = 0
        activeSessionExercises.value.forEach { entry ->
            var exercisesLogged = false
            entry.second.forEach { set ->
                if (set.isDone) {
                    totalVolume += (set.weight * set.reps)
                    exercisesLogged = true
                }
            }
            if (exercisesLogged) totalCount++
        }

        // MET based calories (avg 6 METs for lifting)
        val profile = userProfile.value
        val weight = profile?.weightKg ?: 70.0
        val calories = (6.0 * 3.5 * weight / 200.0) * durationMins

        summaryDuration = formatWorkoutTimer(workoutTimerSeconds.value)
        summaryWorkoutName = name
        summaryVolumeKg = totalVolume
        summaryExercisesCount = totalCount
        summaryCaloriesBurned = (calories * 10).roundToInt() / 10.0

        viewModelScope.launch {
            repository.saveGymLog(
                GymLog(
                    date = currentDateString.value,
                    didGo = "YES",
                    workoutId = name,
                    durationMinutes = durationMins,
                    volumeKb = totalVolume,
                    exercisesCount = totalCount
                )
            )
        }

        activeScreen.value = ActiveScreen.POST_WORKOUT_SUMMARY
    }

    fun formatWorkoutTimer(seconds: Int): String {
        val hrs = seconds / 3600
        val mins = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hrs > 0) {
            String.format("%02d:%02d:%02d", hrs, mins, secs)
        } else {
            String.format("%02d:%02d", mins, secs)
        }
    }

    fun checkInGymToday(didGo: String) {
        viewModelScope.launch {
            if (didGo == "YES") {
                // Navigate to Gym selection
                currentTab.value = 1
                activeScreen.value = ActiveScreen.MAIN_TAB
            } else {
                repository.saveGymLog(
                    GymLog(
                        date = currentDateString.value,
                        didGo = didGo,
                        workoutId = if (didGo == "REST") "Rest Day" else "Missed Gym",
                        durationMinutes = 0,
                        volumeKb = 0.0,
                        exercisesCount = 0
                    )
                )
            }
        }
    }

    fun getGymLog(date: String): Flow<GymLog?> = repository.getGymLog(date)

    // get companion suggestion options for the currently selected logging food
    fun getCompanionsForSelectedFood(): List<CompanionOption> {
        val food = selectedFoodForLog.value ?: return emptyList()
        return repository.companionSuggestions[food.id] ?: emptyList()
    }

    fun setupFoodLoggingFlow(food: FoodItem, slot: String) {
        selectedFoodForLog.value = food
        selectedMealSlot.value = slot
        logMultiplier.value = 1.0

        val companions = repository.companionSuggestions[food.id] ?: emptyList()
        val checkedMap = mutableMapOf<String, Boolean>()
        companions.forEach {
            checkedMap[it.name] = false
        }
        companionsChecked.value = checkedMap
        activeScreen.value = ActiveScreen.ADD_FOOD_FLOW
    }

    fun setCompanionCheck(name: String, checked: Boolean) {
        val copy = companionsChecked.value.toMutableMap()
        copy[name] = checked
        companionsChecked.value = copy
    }

    // Updates live total in bottom sheet
    fun getLiveTotalLoggingNutrition(): Pair<Double, Triple<Double, Double, Double>> {
        val food = selectedFoodForLog.value ?: return Pair(0.0, Triple(0.0, 0.0, 0.0))
        var cal = food.calories * logMultiplier.value
        var prot = food.protein * logMultiplier.value
        var carb = food.carbs * logMultiplier.value
        var fat = food.fat * logMultiplier.value

        val companions = getCompanionsForSelectedFood()
        val checkedState = companionsChecked.value

        companions.forEach { comp ->
            if (checkedState[comp.name] == true) {
                // companion options are 1 standard serving
                cal += comp.calories
                prot += comp.protein
                carb += comp.carbs
                fat += comp.fat
            }
        }

        return Pair(
            (cal * 10.0).roundToInt() / 10.0,
            Triple(
                (prot * 10.0).roundToInt() / 10.0,
                (carb * 10.0).roundToInt() / 10.0,
                (fat * 10.0).roundToInt() / 10.0
            )
        )
    }

    fun executeLogFood() {
        val food = selectedFoodForLog.value ?: return
        val slot = selectedMealSlot.value
        val (totalCal, macroTriple) = getLiveTotalLoggingNutrition()
        val (totalProt, totalCarb, totalFat) = macroTriple

        val finalServingDesc = if (logMultiplier.value == 1.0) {
            food.servingSize
        } else {
            "${logMultiplier.value}x ${food.servingSize}"
        }

        val checkedCompanionsList = getCompanionsForSelectedFood().filter {
            companionsChecked.value[it.name] == true
        }.map { it.name }

        val finalName = if (checkedCompanionsList.isNotEmpty()) {
            "${food.name} (with ${checkedCompanionsList.joinToString(", ")})"
        } else {
            food.name
        }

        viewModelScope.launch {
            repository.logMeal(
                MealLog(
                    id = 0,
                    date = currentDateString.value,
                    mealSlot = slot,
                    name = finalName,
                    calories = totalCal,
                    protein = totalProt,
                    carbs = totalCarb,
                    fat = totalFat,
                    multiplier = logMultiplier.value,
                    servingDesc = finalServingDesc
                )
            )

            // Reset Search states and go back
            foodSearchQuery.value = ""
            selectedFoodForLog.value = null
            activeScreen.value = ActiveScreen.MAIN_TAB
            currentTab.value = 2 // Meal Tab
        }
    }

    fun logDirectMealPlanSuggestion(suggestionName: String, calories: Double, protein: Double, carbs: Double, fat: Double, slot: String) {
        viewModelScope.launch {
            repository.logMeal(
                MealLog(
                    id = 0,
                    date = currentDateString.value,
                    mealSlot = slot,
                    name = suggestionName,
                    calories = calories,
                    protein = protein,
                    carbs = carbs,
                    fat = fat,
                    multiplier = 1.0,
                    servingDesc = "1 serving (Plan)"
                )
            )
        }
    }

    fun deleteMeal(id: Int) {
        viewModelScope.launch {
            repository.deleteMeal(id)
        }
    }

    // --- Water Reminder Toggles ---
    fun quickLogWater(incrementMl: Int) {
        viewModelScope.launch {
            val currentLog = waterToday.value
            val goal = currentLog?.goalMl ?: 3000
            val newAmount = (currentLog?.amountMl ?: 0) + incrementMl
            repository.saveWaterLog(
                WaterLog(
                    date = currentDateString.value,
                    amountMl = newAmount,
                    goalMl = goal
                )
            )
        }
    }

    // --- Supplements Toggles ---
    fun toggleSupplementTaken(supp: Supplement) {
        viewModelScope.launch {
            val isTakenNow = !supp.isTaken
            val newStreak = if (isTakenNow) supp.streak + 1 else (supp.streak - 1).coerceAtLeast(0)
            repository.saveSupplement(
                supp.copy(
                    isTaken = isTakenNow,
                    streak = newStreak,
                    lastTakenDate = if (isTakenNow) currentDateString.value else supp.lastTakenDate
                )
            )
        }
    }

    fun deleteSupplement(id: Int) {
        viewModelScope.launch {
            repository.deleteSupplement(id)
        }
    }

    fun addSupplement(name: String, dosage: String, time: String) {
        viewModelScope.launch {
            repository.saveSupplement(
                Supplement(
                    id = 0,
                    name = name,
                    dosage = dosage,
                    time = time,
                    isTaken = false,
                    streak = 0
                )
            )
        }
    }

    // --- Notifications logic ---
    fun updateNotificationSetting(updater: (NotificationSettings) -> NotificationSettings) {
        notificationsSettings.update(updater)
    }

    // --- AI tips engine based on profile metrics and food logs ---
    fun getAITips(): List<String> {
        val tips = mutableListOf<String>()
        val profile = userProfile.value ?: return listOf("Log your onboarding metrics to see custom tips!")
        val meals = mealsToday.value
        val water = waterToday.value

        val totalCalories = meals.sumOf { it.calories }
        val totalProtein = meals.sumOf { it.protein }
        val totalFat = meals.sumOf { it.fat }

        // Rule 1: Protein < 60% by 2pm (representing 2pm as mock trigger if logged items represent lunch/breakfast)
        val lunchLogged = meals.any { it.mealSlot == "Lunch" }
        if (lunchLogged && totalProtein < (profile.proteinTargetG * 0.6)) {
            tips.add("🍳 Protein is low today. Add boiled eggs, grilled chicken breast, or fresh dahi to your next meal.")
        }

        // Rule 2: Fat > 120% goal
        if (totalFat > (profile.fatTargetG * 1.2)) {
            tips.add("⚠️ Fat is 120% over your daily limit. Prefer grilled over oil-fried dishes and try skipping butter on your Paratha tomorrow.")
        }

        // Rule 3: Calories < 50% by evening
        if (meals.isNotEmpty() && totalCalories < (profile.calorieTarget * 0.5)) {
            tips.add("🥪 Energy alert: You are significantly under-eating today. Consider adding a wholesome pre-workout snack or full dinner.")
        }

        // Rule 4: Water check
        val waterAmt = water?.amountMl ?: 0
        if (waterAmt < 1500) {
            tips.add("💧 Hydration alert: You have only logged ${waterAmt}ml of water. Drink 2 full glasses right now to keep muscle recovery high!")
        }

        // Rule 5: Missed gym days
        val activeGymStreakLogs = gymLogs.value
        val skippedCount = activeGymStreakLogs.filter { it.didGo == "NO" }.size
        if (skippedCount >= 2) {
            tips.add("🔥 gym momentum: You've missed multiple gym sessions. Even a 20-minute bodyweight home workout keeps the engine running!")
        }

        // Rule 6: Minerals & Vitamins checklist (e.g. Iron < 8mg)
        // Find iron in meals (mock or sum up accurately from database foods multiplier)
        var totalIron = 0.0
        val dbFoods = repository.foodDatabase
        meals.forEach { meal ->
            val match = dbFoods.find { it.name == meal.name || meal.name.startsWith(it.name) }
            if (match != null) {
                totalIron += match.iron * meal.multiplier
            }
        }
        if (meals.isNotEmpty() && totalIron < 8.0) {
            tips.add("🥩 Micronutrient: Iron intake is slightly low today. Incorporate lean beef chunks, fresh palak, haleem, or dates tomorrow.")
        }

        // Standard positive tips
        if (tips.isEmpty()) {
            tips.add("✨ Masterful job! Your calories, hydration, and gym check-ins are perfectly aligned with your ${profile.goal} plan.")
            tips.add("🥦 Tip: Raw greens like cucumber and leafy spinach can keep micronutrient values high without spiking calories.")
        }

        return tips
    }

    // Get suggestions list based on BMI
    val preFilledDietSuggestions: StateFlow<Map<String, List<MealLog>>> = userProfile.map { profile ->
        if (profile == null) {
            emptyMap()
        } else {
            val bmi = repository.calculateBMI(profile.weightKg, profile.heightCm)
            val category = repository.getBMICategory(bmi)
            repository.getBMIPreFilledSuggestedMeals(category, profile.goal)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())
}
