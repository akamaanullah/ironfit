package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import kotlinx.coroutines.flow.flatMapLatest
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.WaterDrop
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
import com.example.data.models.Supplement
import com.example.ui.components.CurvedHeader
import com.example.ui.components.MacroRingTracker
import com.example.ui.theme.*
import com.example.ui.viewmodel.IronViewModel

@Composable
fun DashboardScreen(
    viewModel: IronViewModel,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfile.collectAsState()
    val meals by viewModel.mealsToday.collectAsState()
    val water by viewModel.waterToday.collectAsState()
    val supplementsList by viewModel.supplements.collectAsState()

    val textPrimary = if (isDark) TextPrimaryDark else TextPrimaryLight
    val textSecondary = if (isDark) TextSecondaryDark else TextSecondaryLight
    val cardBg = if (isDark) CardDark else CardLight
    val cardBorder = if (isDark) CardBorderDark else CardBorderLight

    // Calculate daily sums
    val totalCalories = meals.sumOf { it.calories }
    val totalProtein = meals.sumOf { it.protein }
    val totalCarbs = meals.sumOf { it.carbs }
    val totalFat = meals.sumOf { it.fat }

    val calorieTarget = profile?.calorieTarget ?: 2000.0
    val caloriesLeft = (calorieTarget - totalCalories).coerceAtLeast(0.0)

    val proteinTarget = profile?.proteinTargetG ?: 130.0
    val carbsTarget = profile?.carbTargetG ?: 250.0
    val fatTarget = profile?.fatTargetG ?: 65.0

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDark) BackgroundDark else BackgroundLight)
    ) {
        // Curved header
        CurvedHeader(
            title = "Welcome back!",
            subtitle = if (profile != null) "BMI Category: ${viewModel.preFilledDietSuggestions.value.keys.isNotEmpty()} (${profile!!.goal})" else "Personalizing targets...",
            isDark = isDark,
            onToggleTheme = { viewModel.toggleTheme() }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 20.dp)
                .verticalScroll(androidx.compose.foundation.rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // concentric rings card
            MacroRingTracker(
                maxCalories = calorieTarget,
                currentCalories = caloriesLeft,
                proteinTarget = proteinTarget,
                currentProtein = totalProtein,
                carbsTarget = carbsTarget,
                currentCarbs = totalCarbs,
                fatTarget = fatTarget,
                currentFat = totalFat,
                isDark = isDark
            )

            // Dynamic rule-based AI tips carousel
            AITipsSection(viewModel, isDark, cardBg, cardBorder, textPrimary, textSecondary)

            // Gym check-in card
            GymCheckInCard(viewModel, isDark, cardBg, cardBorder, textPrimary, textSecondary)

            // Hydration Card
            HydrationCard(viewModel, water?.amountMl ?: 0, water?.goalMl ?: 3000, isDark, cardBg, cardBorder, textPrimary, textSecondary)

            // Supplements quick checklist
            SupplementsDashboardCard(viewModel, supplementsList, isDark, cardBg, cardBorder, textPrimary, textSecondary)

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun AITipsSection(
    viewModel: IronViewModel,
    isDark: Boolean,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    val tips = viewModel.getAITips()

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "✨ Smart AI Tip Cards",
                color = textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .background(PrimaryGreen.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "LIVE",
                    color = PrimaryGreen,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tips) { tip ->
                Card(
                    modifier = Modifier
                        .width(260.dp)
                        .height(100.dp)
                        .testTag("ai_tip_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "info",
                            tint = PrimaryGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = tip,
                            color = textPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GymCheckInCard(
    viewModel: IronViewModel,
    isDark: Boolean,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    val gymLog by viewModel.currentDateString.flatMapLatest { date ->
        if (date.isEmpty()) kotlinx.coroutines.flow.flowOf(null) else viewModel.getGymLog(date)
    }.collectAsState(initial = null)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("gym_checkin_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Did you hit the gym today? 🏋️‍♂️",
                color = textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (gymLog == null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.checkInGymToday("YES") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("checkin_yes"),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen, contentColor = Color.Black),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("YES 💪", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { viewModel.checkInGymToday("NO") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("checkin_no"),
                        colors = ButtonDefaults.buttonColors(containerColor = DangerColor, contentColor = Color.White),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("NO 😴", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { viewModel.checkInGymToday("REST") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("checkin_rest"),
                        colors = ButtonDefaults.buttonColors(containerColor = textSecondary.copy(alpha = 0.2f), contentColor = textPrimary),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("REST 🔄", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "logged",
                        tint = if (gymLog!!.didGo == "YES") PrimaryGreen else textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (gymLog!!.didGo) {
                            "YES" -> "Workout Saved on this Date!"
                            "REST" -> "Marked Day as Rest 🔄"
                            else -> "Gym Missed on this Date. Tomorrow is another day! 🔥"
                        },
                        color = textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun HydrationCard(
    viewModel: IronViewModel,
    amount: Int,
    goal: Int,
    isDark: Boolean,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hydration_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = "water",
                        tint = InfoColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Hydration Tracker",
                        color = textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Drank $amount ml of $goal ml goal.",
                    color = textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.quickLogWater(250) },
                    modifier = Modifier
                        .testTag("add_water_250")
                        .height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = InfoColor.copy(alpha = 0.2f), contentColor = InfoColor),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) {
                    Text("+250ml", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { viewModel.quickLogWater(500) },
                    modifier = Modifier
                        .testTag("add_water_500")
                        .height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = InfoColor, contentColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) {
                    Text("+500ml", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SupplementsDashboardCard(
    viewModel: IronViewModel,
    supps: List<Supplement>,
    isDark: Boolean,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("supplements_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "💊 Supps Checklist Today",
                color = textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (supps.isEmpty()) {
                Text(
                    text = "No active supplements mapped. Add them in profile settings!",
                    color = textSecondary,
                    fontSize = 12.sp
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    supps.forEach { supp ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (supp.isTaken) PrimaryGreen.copy(alpha = 0.1f) else Color.Transparent)
                                .clickable { viewModel.toggleSupplementTaken(supp) }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = supp.name,
                                    color = textPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${supp.dosage} at ${supp.time}",
                                    color = textSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${supp.streak} 🔥",
                                    color = WarningColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Checkbox(
                                    checked = supp.isTaken,
                                    onCheckedChange = { viewModel.toggleSupplementTaken(supp) },
                                    colors = CheckboxDefaults.colors(checkedColor = PrimaryGreen)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
