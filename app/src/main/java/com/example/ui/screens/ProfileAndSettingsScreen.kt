package com.example.ui.screens

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Supplement
import com.example.ui.components.CustomLineChart
import com.example.ui.components.CurvedHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.IronViewModel
import java.util.*

@Composable
fun ProgressTabScreen(
    viewModel: IronViewModel,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val textPrimary = if (isDark) TextPrimaryDark else TextPrimaryLight
    val textSecondary = if (isDark) TextSecondaryDark else TextSecondaryLight
    val cardBg = if (isDark) CardDark else CardLight
    val cardBorder = if (isDark) CardBorderDark else CardBorderLight

    val mealsToday by viewModel.mealsToday.collectAsState()
    val waterToday by viewModel.waterToday.collectAsState()
    val gymLogList by viewModel.gymLogs.collectAsState()

    var logWeightInput by remember { mutableStateOf("") }
    var logWeightNotes by remember { mutableStateOf("") }
    val weightHistory = remember { mutableStateListOf(72.0, 71.5, 71.2, 70.8, 70.5, 70.0) }
    val weightDates = listOf("06-09", "06-10", "06-11", "06-12", "06-13", "06-14")

    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDark) BackgroundDark else BackgroundLight)
    ) {
        CurvedHeader(
            title = "Body Analytics",
            subtitle = "Track raw bodyweights & unlock fitness medals",
            isDark = isDark,
            onToggleTheme = { viewModel.toggleTheme() }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Visual WEIGHT CHART
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("weight_chart_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, cardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "📉 Raw Weight Fluctuations",
                            color = textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        CustomLineChart(
                            points = weightHistory.toList(),
                            labels = weightDates,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            isDark = isDark
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            weightDates.forEach { date ->
                                Text(text = date, color = textSecondary, fontSize = 9.sp)
                            }
                        }
                    }
                }
            }

            // Weight Log inputs
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, cardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "⚖️ Record New Daily Weigh-in",
                            color = textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = logWeightInput,
                                onValueChange = { logWeightInput = it },
                                label = { Text("Weight (kg)") },
                                placeholder = { Text("e.g. 69.5") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryGreen,
                                    unfocusedBorderColor = cardBorder,
                                    focusedTextColor = textPrimary,
                                    unfocusedTextColor = textPrimary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).testTag("weight_log_input")
                            )

                            Button(
                                onClick = {
                                    val w = logWeightInput.toDoubleOrNull()
                                    if (w != null) {
                                        weightHistory.add(w)
                                        if (weightHistory.size > 6) {
                                            weightHistory.removeAt(0)
                                        }
                                        logWeightInput = ""
                                        logWeightNotes = ""
                                        Toast.makeText(context, "Weigh-in recorded successfully! 📈", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen, contentColor = Color.Black),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .height(56.dp)
                                    .testTag("submit_weight")
                            ) {
                                Text("Log", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Gamification medals
            item {
                Text(
                    text = "🏆 Unlocked Fitness Achievements",
                    color = textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                // Pre-calculating unlocked medals
                val proteinGoal = mealsToday.sumOf { it.protein } >= 80
                val waterGoalUn = (waterToday?.amountMl ?: 0) >= 2000
                val gymGo = gymLogList.any { it.didGo == "YES" }
                val streakGoal = true // defaults

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    MedalBadge(title = "Iron Beast", icon = "🏋️‍♂️", description = "Completed 1st Gym session", unlocked = gymGo, textPrimary = textPrimary, cardBg = cardBg)
                    MedalBadge(title = "Water King", icon = "💧", description = "Drank >2L in one date", unlocked = waterGoalUn, textPrimary = textPrimary, cardBg = cardBg)
                    MedalBadge(title = "Paratha Shredder", icon = "🫓", description = "Logged Pakistani food", unlocked = true, textPrimary = textPrimary, cardBg = cardBg)
                    MedalBadge(title = "Medals Master", icon = "🔥", description = "Checked 3+ item supps", unlocked = streakGoal, textPrimary = textPrimary, cardBg = cardBg)
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun MedalBadge(
    title: String,
    icon: String,
    description: String,
    unlocked: Boolean,
    textPrimary: Color,
    cardBg: Color
) {
    Box(
        modifier = Modifier
            .size(width = 120.dp, height = 150.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (unlocked) cardBg else cardBg.copy(alpha = 0.4f))
            .border(
                1.dp,
                if (unlocked) PrimaryGreen.copy(alpha = 0.6f) else Color.Transparent,
                RoundedCornerShape(16.dp)
            )
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(
                        if (unlocked) PrimaryGreen.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.15f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 28.sp)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                color = if (unlocked) textPrimary else Color.Gray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = description,
                color = Color.Gray,
                fontSize = 8.sp,
                textAlign = TextAlign.Center,
                lineHeight = 11.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun ProfileAndSettingsScreen(
    viewModel: IronViewModel,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfile.collectAsState()
    val supplementsList by viewModel.supplements.collectAsState()
    val notificationSettings by viewModel.notificationsSettings.collectAsState()

    val textPrimary = if (isDark) TextPrimaryDark else TextPrimaryLight
    val textSecondary = if (isDark) TextSecondaryDark else TextSecondaryLight
    val cardBg = if (isDark) CardDark else CardLight
    val cardBorder = if (isDark) CardBorderDark else CardBorderLight

    // Dialogue triggers
    var showAddSuppDialog by remember { mutableStateOf(false) }
    var suppName by remember { mutableStateOf("") }
    var suppDosage by remember { mutableStateOf("") }
    var suppTime by remember { mutableStateOf("08:00") }

    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDark) BackgroundDark else BackgroundLight)
    ) {
        CurvedHeader(
            title = "Coach Profile",
            subtitle = "Calibrate metadata configuration limits",
            isDark = isDark,
            onToggleTheme = { viewModel.toggleTheme() },
            trailingContent = {
                // Logout/Reset trigger
                IconButton(
                    onClick = {
                        // Reset Profile block
                        viewModel.updateProfile(24, 70.0, 175.0, true, "Moderate", "Muscle Gain")
                        Toast.makeText(context, "Coach profile reset successfully!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.background(DangerColor.copy(alpha = 0.15f), CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "reset", tint = DangerColor)
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Avatar profile banner
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .background(PrimaryGreen.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = if (profile?.isMale == true) "🙋‍♂️" else "🙋‍♀️", fontSize = 36.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = if (profile?.isMale == true) "Amaan Ullah (Coach)" else "IronFit Athlete",
                            color = textPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Goal: ${profile?.goal ?: "Muscle Gain"}",
                            color = PrimaryGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Metabolic BMR / TDEE Card
            if (profile != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = BorderStroke(1.dp, cardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("🧬 Metabolic Configurations", color = textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                            SummaryRowInfo(label = "Height / Weight", value = "${profile!!.heightCm.toInt()} cm / ${profile!!.weightKg.toInt()} kg", textPrimary = textPrimary)
                            SummaryRowInfo(label = "Mifflin BMR", value = "${profile!!.bmr.toInt()} kcal", textPrimary = textPrimary)
                            SummaryRowInfo(label = "Daily TDEE Budget", value = "${profile!!.tdee.toInt()} kcal", textPrimary = textPrimary)
                            Divider(color = cardBorder)
                            SummaryRowInfo(label = "Calorie Target Wallet", value = "${profile!!.calorieTarget.toInt()} kcal (${profile!!.goal})", textPrimary = PrimaryGreen)
                        }
                    }
                }
            }

            // Supplements Management section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💊 Active Supplement Plans",
                        color = textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = { showAddSuppDialog = true },
                        modifier = Modifier
                            .testTag("add_supp_btn")
                            .background(PrimaryGreen, CircleShape)
                            .size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "add", tint = Color.Black, modifier = Modifier.size(20.dp))
                    }
                }
            }

            items(supplementsList) { supp ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(cardBg)
                        .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = supp.name, color = textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(text = "${supp.dosage} at ${supp.time}", color = textSecondary, fontSize = 11.sp)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "${supp.streak} 🔥", color = WarningColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(10.dp))
                        IconButton(onClick = { viewModel.deleteSupplement(supp.id) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "delete", tint = DangerColor, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            // Settings Configurations Section (toggles)
            item {
                Text(
                    text = "⚙️ Notification Alarm Alerters",
                    color = textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("alarm_settings_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, cardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        RowSettingToggle(
                            label = "Gym Reminder Alarm",
                            desc = "30min before Gym: 'Gym in 30min. Log pre-workout meal.'",
                            checked = notificationSettings.gymAlarmEnabled,
                            onToggle = { viewModel.updateNotificationSetting { it.copy(gymAlarmEnabled = !it.gymAlarmEnabled) } },
                            textPrimary = textPrimary, textSecondary = textSecondary
                        )

                        RowSettingToggle(
                            label = "Meal Alarm Logs",
                            desc = "Notify remaining calories 10min prior to dinner slots.",
                            checked = notificationSettings.mealAlarmEnabled,
                            onToggle = { viewModel.updateNotificationSetting { it.copy(mealAlarmEnabled = !it.mealAlarmEnabled) } },
                            textPrimary = textPrimary, textSecondary = textSecondary
                        )

                        RowSettingToggle(
                            label = "Water Interval Reminders",
                            desc = "Push water hydration targets every 2-hours intervals.",
                            checked = notificationSettings.waterIntervalHours == 2,
                            onToggle = { viewModel.updateNotificationSetting { it.copy(waterIntervalHours = if (it.waterIntervalHours == 2) 0 else 2) } },
                            textPrimary = textPrimary, textSecondary = textSecondary
                        )

                        RowSettingToggle(
                            label = "Supplements Reminders",
                            desc = "Notify scheduled capsules and display active streak.",
                            checked = notificationSettings.supplementsEnabled,
                            onToggle = { viewModel.updateNotificationSetting { it.copy(supplementsEnabled = !it.supplementsEnabled) } },
                            textPrimary = textPrimary, textSecondary = textSecondary
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Add Supplement popup layout
    if (showAddSuppDialog) {
        AlertDialog(
            onDismissRequest = { showAddSuppDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        if (suppName.trim().isNotEmpty() && suppDosage.trim().isNotEmpty()) {
                            viewModel.addSupplement(suppName, suppDosage, suppTime)
                            showAddSuppDialog = false
                            suppName = ""
                            suppDosage = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen, contentColor = Color.Black)
                ) {
                    Text("Add Plan 💊", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSuppDialog = false }, colors = ButtonDefaults.textButtonColors(contentColor = textPrimary)) {
                    Text("Cancel")
                }
            },
            title = { Text("Log New Supplement", color = textPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = suppName,
                        onValueChange = { suppName = it },
                        label = { Text("Supplement Name") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryGreen),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = suppDosage,
                        onValueChange = { suppDosage = it },
                        label = { Text("Dosage (Serving)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryGreen),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // Simple time selector dialog
                                val calendar = Calendar.getInstance()
                                TimePickerDialog(
                                    context,
                                    { _, hr, min -> suppTime = String.format("%02d:%02d", hr, min) },
                                    calendar.get(Calendar.HOUR_OF_DAY),
                                    calendar.get(Calendar.MINUTE),
                                    true
                                ).show()
                            }
                            .border(1.dp, cardBorder, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Trigger Schedule Time:", color = textSecondary, fontSize = 12.sp)
                        Text(suppTime, color = textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            containerColor = cardBg,
            textContentColor = textPrimary
        )
    }
}

@Composable
fun SummaryRowInfo(
    label: String,
    value: String,
    textPrimary: Color
) {
    val textSecondary = textPrimary.copy(alpha = 0.6f)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Text(text = value, color = textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun RowSettingToggle(
    label: String,
    desc: String,
    checked: Boolean,
    onToggle: () -> Unit,
    textPrimary: Color,
    textSecondary: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
            Text(text = label, color = textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(text = desc, color = textSecondary, fontSize = 10.sp, lineHeight = 13.sp, modifier = Modifier.padding(top = 2.dp))
        }

        Switch(
            checked = checked,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = PrimaryGreen)
        )
    }
}
