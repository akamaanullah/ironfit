package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.CompanionOption
import com.example.data.models.FoodItem
import com.example.data.models.MealLog
import com.example.data.models.UserProfile
import com.example.ui.components.CurvedHeader
import com.example.ui.components.MacroRingTracker
import com.example.ui.theme.*
import com.example.ui.viewmodel.ActiveScreen
import com.example.ui.viewmodel.IronViewModel
import kotlin.math.roundToInt

@Composable
fun MealsScreen(
    viewModel: IronViewModel,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfile.collectAsState()
    val loggedMeals by viewModel.mealsToday.collectAsState()
    val preFilledPlans by viewModel.preFilledDietSuggestions.collectAsState()

    val textPrimary = if (isDark) TextPrimaryDark else TextPrimaryLight
    val textSecondary = if (isDark) TextSecondaryDark else TextSecondaryLight
    val cardBg = if (isDark) CardDark else CardLight
    val cardBorder = if (isDark) CardBorderDark else CardBorderLight

    val context = LocalContext.current

    // Expansion status for 6 slots
    var expandedSlots by remember { mutableStateOf(mapOf<String, Boolean>()) }
    val slots = listOf("Breakfast", "Lunch", "Dinner", "Snacks", "Pre-Workout", "Post-Workout")

    // Active View state: true for normal grid, false for detailed vitamins/minerals breakdown
    var showVitaminsMAndM by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDark) BackgroundDark else BackgroundLight)
    ) {
        CurvedHeader(
            title = "Calorie Wallet",
            subtitle = "Track raw Pakistani cuisine and macro splittings",
            isDark = isDark,
            onToggleTheme = { viewModel.toggleTheme() },
            trailingContent = {
                TextButton(
                    onClick = { showVitaminsMAndM = !showVitaminsMAndM },
                    colors = ButtonDefaults.textButtonColors(contentColor = PrimaryGreen)
                ) {
                    Text(
                        if (showVitaminsMAndM) "🥣 Slots" else "📊 Micro Report",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        )

        if (showVitaminsMAndM) {
            VitaminsMineralsReport(viewModel, isDark, textPrimary, textSecondary, cardBg, cardBorder)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Diet Plan generated suggestions card based on BMI
                item {
                    BMIPlanWidget(
                        preFilledPlans,
                        profile,
                        onClickQuickLog = { name, cal, prot, carb, fat, slot ->
                            viewModel.logDirectMealPlanSuggestion(name, cal, prot, carb, fat, slot)
                            Toast.makeText(context, "Log pre-filled: $name in $slot", Toast.LENGTH_SHORT).show()
                        },
                        isDark = isDark,
                        cardBg = cardBg,
                        cardBorder = cardBorder,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary
                    )
                }

                item {
                    Text(
                        text = "📅 Meal Log Slots Summary",
                        color = textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 6 Expandable slots
                items(slots) { slot ->
                    val mealsInSlot = loggedMeals.filter { it.mealSlot.equals(slot, ignoreCase = true) }
                    val totalSlotCal = mealsInSlot.sumOf { it.calories }
                    val isExpanded = expandedSlots[slot] ?: false

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("slot_card_$slot"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = BorderStroke(1.dp, cardBorder)
                    ) {
                        Column {
                            // Slot Banner
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        expandedSlots = expandedSlots
                                            .toMutableMap()
                                            .apply { put(slot, !isExpanded) }
                                    }
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = when (slot) {
                                            "Breakfast" -> "🍳"
                                            "Lunch" -> "🍛"
                                            "Dinner" -> "🍲"
                                            "Snacks" -> "🥜"
                                            "Pre-Workout" -> "☕"
                                            else -> "🍗"
                                        },
                                        fontSize = 18.sp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = slot,
                                            color = textPrimary,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${mealsInSlot.size} items logged • ${totalSlotCal.toInt()} kcal",
                                            color = textSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            viewModel.selectedMealSlot.value = slot
                                            viewModel.activeScreen.value = ActiveScreen.ADD_FOOD_FLOW
                                        },
                                        modifier = Modifier
                                            .testTag("add_food_btn_$slot")
                                            .size(36.dp)
                                            .background(
                                                PrimaryGreen.copy(alpha = 0.2f),
                                                RoundedCornerShape(8.dp)
                                            )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "add",
                                            tint = PrimaryGreen,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = "toggle",
                                        tint = textSecondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            // Expanded meal item rows list
                            AnimatedVisibility(visible = isExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (mealsInSlot.isEmpty()) {
                                        Text(
                                            text = "Empty slot. Log items using the '+' button! 🥪",
                                            color = textSecondary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(bottom = 10.dp)
                                        )
                                    } else {
                                        mealsInSlot.forEach { meal ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(
                                                        if (isDark) Color(0x0AFFFFFF) else Color(0x05000000),
                                                        RoundedCornerShape(10.dp)
                                                    )
                                                    .padding(10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = meal.name,
                                                        color = textPrimary,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = "${meal.servingDesc} • P: ${meal.protein.toInt()}g C: ${meal.carbs.toInt()}g F: ${meal.fat.toInt()}g",
                                                        color = textSecondary,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = "${meal.calories.toInt()} kcal",
                                                        color = textPrimary,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    IconButton(
                                                        onClick = { viewModel.deleteMeal(meal.id) },
                                                        modifier = Modifier
                                                            .testTag("delete_meal_${meal.id}")
                                                            .size(28.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Delete,
                                                            contentDescription = "delete",
                                                            tint = DangerColor,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}

@Composable
fun BMIPlanWidget(
    plan: Map<String, List<MealLog>>,
    profile: UserProfile?,
    onClickQuickLog: (String, Double, Double, Double, Double, String) -> Unit,
    isDark: Boolean,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    if (profile == null || plan.isEmpty()) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bmi_plans_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🤖 AI Custom Diet Planner",
                    color = textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Box(
                    modifier = Modifier
                        .background(PrimaryGreen, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "BMI PRESETS",
                        color = Color.Black,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pre-designed recipes configured automatically for healthy '${profile.goal}' target.",
                color = textSecondary,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Expandable/List of 3 meal recommendations
            val selectedSuggestions = plan.entries.take(3)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                selectedSuggestions.forEach { entry ->
                    val slot = entry.key
                    val foodList = entry.value

                    foodList.forEach { food ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isDark) Color(0x19FFFFFF) else Color(0x05000000),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "$slot: ${food.name}",
                                    color = textPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${food.calories.toInt()} kcal • P: ${food.protein.toInt()}g C: ${food.carbs.toInt()}g F: ${food.fat.toInt()}g",
                                    color = textSecondary,
                                    fontSize = 10.sp
                                )
                            }

                            Button(
                                onClick = {
                                    onClickQuickLog(food.name, food.calories, food.protein, food.carbs, food.fat, slot)
                                },
                                modifier = Modifier
                                    .testTag("quick_log_plan_${food.name.replace(" ", "_")}")
                                    .height(30.dp),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen, contentColor = Color.Black),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("Quick Log ✅", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ColumnScope.VitaminsMineralsReport(
    viewModel: IronViewModel,
    isDark: Boolean,
    textPrimary: Color,
    textSecondary: Color,
    cardBg: Color,
    cardBorder: Color
) {
    val meals by viewModel.mealsToday.collectAsState()
    val dbFoods = viewModel.foodDatabaseList

    // Sum up accurate micro value aggregates based on raw DB foods mapped
    var vitA = 0.0
    var vitC = 0.0
    var vitD = 0.0
    var vitE = 0.0
    var vitK = 0.0
    var b1 = 0.0
    var b2 = 0.0
    var b3 = 0.0
    var b6 = 0.0
    var b12 = 0.0

    var iron = 0.0
    var calcium = 0.0
    var potassium = 0.0
    var sodium = 0.0
    var magnesium = 0.0
    var zinc = 0.0
    var phosphorus = 0.0

    meals.forEach { logged ->
        val item = dbFoods.find { it.name == logged.name || logged.name.startsWith(it.name) }
        if (item != null) {
            val mult = logged.multiplier
            vitA += item.vitA * mult
            vitC += item.vitC * mult
            vitD += item.vitD * mult
            vitE += item.vitE * mult
            vitK += item.vitK * mult
            b1 += item.vitB1 * mult
            b2 += item.vitB2 * mult
            b3 += item.vitB3 * mult
            b6 += item.vitB6 * mult
            b12 += item.vitB12 * mult

            iron += item.iron * mult
            calcium += item.calcium * mult
            potassium += item.potassium * mult
            sodium += item.sodium * mult
            magnesium += item.magnesium * mult
            zinc += item.zinc * mult
            phosphorus += item.phosphorus * mult
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "🥦 Detailed Daily Micronutrients",
                color = textPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 10.dp)
            )
        }

        // Vitamins Category
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, cardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Vitamins Catalog", color = PrimaryGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    MicroUnitProgress(name = "Vitamin A (Target: 3000 IU)", current = vitA, target = 3000.0, unit = "IU", textPrimary = textPrimary)
                    MicroUnitProgress(name = "Vitamin C (Target: 90 mg)", current = vitC, target = 90.0, unit = "mg", textPrimary = textPrimary)
                    MicroUnitProgress(name = "Vitamin D (Target: 600 IU)", current = vitD, target = 600.0, unit = "IU", textPrimary = textPrimary)
                    MicroUnitProgress(name = "Vitamin E (Target: 15 mg)", current = vitE, target = 15.0, unit = "mg", textPrimary = textPrimary)
                    MicroUnitProgress(name = "Vitamin K (Target: 120 mcg)", current = vitK, target = 120.0, unit = "mcg", textPrimary = textPrimary)
                    MicroUnitProgress(name = "B1 (Thiamin) (Target: 1.2 mg)", current = b1, target = 1.2, unit = "mg", textPrimary = textPrimary)
                    MicroUnitProgress(name = "B2 (Riboflavin) (Target: 1.3 mg)", current = b2, target = 1.3, unit = "mg", textPrimary = textPrimary)
                    MicroUnitProgress(name = "B3 (Niacin) (Target: 16 mg)", current = b3, target = 16.0, unit = "mg", textPrimary = textPrimary)
                    MicroUnitProgress(name = "B6 (Pyridoxine) (Target: 1.5 mg)", current = b6, target = 1.5, unit = "mg", textPrimary = textPrimary)
                    MicroUnitProgress(name = "B12 (Cobalamin) (Target: 2.4 mcg)", current = b12, target = 2.4, unit = "mcg", textPrimary = textPrimary)
                }
            }
        }

        // Minerals Category
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, cardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Minerals Catalog", color = InfoColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    MicroUnitProgress(name = "Iron (Target: 18 mg)", current = iron, target = 18.0, unit = "mg", textPrimary = textPrimary)
                    MicroUnitProgress(name = "Calcium (Target: 1000 mg)", current = calcium, target = 1000.0, unit = "mg", textPrimary = textPrimary)
                    MicroUnitProgress(name = "Potassium (Target: 4700 mg)", current = potassium, target = 4700.0, unit = "mg", textPrimary = textPrimary)
                    MicroUnitProgress(name = "Sodium (Target: 2300 mg)", current = sodium, target = 2300.0, unit = "mg", textPrimary = textPrimary)
                    MicroUnitProgress(name = "Magnesium (Target: 400 mg)", current = magnesium, target = 400.0, unit = "mg", textPrimary = textPrimary)
                    MicroUnitProgress(name = "Zinc (Target: 11 mg)", current = zinc, target = 11.0, unit = "mg", textPrimary = textPrimary)
                    MicroUnitProgress(name = "Phosphorus (Target: 700 mg)", current = phosphorus, target = 700.0, unit = "mg", textPrimary = textPrimary)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun MicroUnitProgress(
    name: String,
    current: Double,
    target: Double,
    unit: String,
    textPrimary: Color
) {
    val pct = if (target > 0) (current / target).toFloat().coerceIn(0f, 1f) else 0f
    val barColor = when {
        pct >= 0.8f -> PrimaryGreen
        pct >= 0.4f -> WarningColor
        else -> DangerColor
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = name, color = textPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Text(
                text = "${(current * 10).roundToInt() / 10.0} / ${target.toInt()} $unit (${(pct * 100).toInt()}%)",
                color = barColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .background(Color.Gray.copy(alpha = 0.15f), RoundedCornerShape(2.5.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(pct)
                    .background(barColor, RoundedCornerShape(2.5.dp))
            )
        }
    }
}

@Composable
fun AddFoodFlowScreen(
    viewModel: IronViewModel,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val searchResults by viewModel.searchedFoods.collectAsState()
    val selectedSlot by viewModel.selectedMealSlot.collectAsState()
    val searchQuery by viewModel.foodSearchQuery.collectAsState()
    val activeCategory by viewModel.activeFoodCategory.collectAsState()

    val selectedFood by viewModel.selectedFoodForLog.collectAsState()
    val logMult by viewModel.logMultiplier.collectAsState()
    val checkedComps by viewModel.companionsChecked.collectAsState()

    val textPrimary = if (isDark) TextPrimaryDark else TextPrimaryLight
    val textSecondary = if (isDark) TextSecondaryDark else TextSecondaryLight
    val cardBg = if (isDark) CardDark else CardLight
    val cardBorder = if (isDark) CardBorderDark else CardBorderLight

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDark) BackgroundDark else BackgroundLight)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Adding to $selectedSlot",
                        color = textPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Build recipe combinations dynamically.",
                        color = textSecondary,
                        fontSize = 11.sp
                    )
                }

                IconButton(
                    onClick = {
                        viewModel.activeScreen.value = ActiveScreen.MAIN_TAB
                    },
                    modifier = Modifier.testTag("add_food_cancel")
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "cancel", tint = textPrimary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Step 1 Layout: Search and list results
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.foodSearchQuery.value = it },
                placeholder = { Text("Search paratha, biryani, chicken...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "search") },
                trailingIcon = {
                    IconButton(onClick = {}) { // Mock scanner
                        Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = "scanner", tint = PrimaryGreen)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = cardBorder,
                    focusedTextColor = textPrimary,
                    unfocusedTextColor = textPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_food_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category choice chips
            val categories = listOf("All", "Pakistani", "International")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.forEach { cat ->
                    val active = activeCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (active) PrimaryGreen else cardBg)
                            .border(1.dp, if (active) PrimaryGreen else cardBorder, RoundedCornerShape(8.dp))
                            .clickable { viewModel.activeFoodCategory.value = cat }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("food_cat_$cat")
                    ) {
                        Text(
                            text = cat,
                            color = if (active) Color.Black else textPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // List of searched foods
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(searchResults) { food ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(cardBg)
                            .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.setupFoodLoggingFlow(food, selectedSlot)
                            }
                            .padding(12.dp)
                            .testTag("search_food_row_${food.id}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = food.name,
                                color = textPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${food.servingSize} • P: ${food.protein.toInt()}g C: ${food.carbs.toInt()}g F: ${food.fat.toInt()}g",
                                color = textSecondary,
                                fontSize = 11.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${food.calories.toInt()} kcal",
                                color = textPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "add",
                                tint = PrimaryGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Step 2 Bottom Sheet Overlay (Pre-selected Food logs)
        if (selectedFood != null) {
            val food = selectedFood!!
            val companions = viewModel.getCompanionsForSelectedFood()
            val runningTriple = viewModel.getLiveTotalLoggingNutrition()
            val finalCal = runningTriple.first
            val (finalProt, finalCarb, finalFat) = runningTriple.second

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable { viewModel.selectedFoodForLog.value = null },
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { /* Prevent dismiss */ }
                        .testTag("logging_bottom_sheet"),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, cardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = food.name,
                                    color = textPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Base serving: ${food.servingSize}",
                                    color = textSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            IconButton(onClick = { viewModel.selectedFoodForLog.value = null }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "dismiss", tint = textPrimary)
                            }
                        }

                        // Servings quantity adjustor row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Quantity servings:",
                                color = textPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(PrimaryGreen.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                        .clickable {
                                            viewModel.logMultiplier.value = (logMult - 0.5).coerceAtLeast(0.5)
                                        }
                                        .testTag("multiply_minus"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("-", color = PrimaryGreen, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }

                                Text(
                                    text = "${logMult}x",
                                    color = textPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(36.dp),
                                    textAlign = TextAlign.Center
                                )

                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(PrimaryGreen, RoundedCornerShape(8.dp))
                                        .clickable {
                                            viewModel.logMultiplier.value = logMult + 0.5
                                        }
                                        .testTag("multiply_plus"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("+", color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // COMPANION SUGGESTIONS SECTION
                        if (companions.isNotEmpty()) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "🍳 Companion suggestions: What are you having with it?",
                                    color = textPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    companions.forEach { comp ->
                                        val checked = checkedComps[comp.name] ?: false
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (checked) PrimaryGreen.copy(alpha = 0.2f) else cardBg)
                                                .border(
                                                    1.dp,
                                                    if (checked) PrimaryGreen else cardBorder,
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .clickable { viewModel.setCompanionCheck(comp.name, !checked) }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                                .testTag("companion_${comp.name.replace(" ", "_")}")
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = comp.name,
                                                    color = if (checked) PrimaryGreen else textPrimary,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "(+${comp.calories.toInt()} kcal)",
                                                    color = textSecondary,
                                                    fontSize = 9.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Running Total Panel
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isDark) Color(0x0AFFFFFF) else Color(0x05000000), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("RUNNING SUM LIMITS", color = textSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Text("$finalCal kcal total", color = PrimaryGreen, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                                }
                                Text(
                                    text = "P: ${finalProt.toInt()}g • C: ${finalCarb.toInt()}g • F: ${finalFat.toInt()}g",
                                    color = textPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Log command Button
                        Button(
                            onClick = { viewModel.executeLogFood() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("confirm_add_food"),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen, contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Add to $selectedSlot Wallet 💰", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }
    }
}
