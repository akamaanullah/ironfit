package com.example.ui.screens

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.models.Exercise
import com.example.data.models.WorkoutPlan
import com.example.ui.components.CurvedHeader
import com.example.ui.components.SimpleCircularTimer
import com.example.ui.theme.*
import com.example.ui.viewmodel.ActiveScreen
import com.example.ui.viewmodel.IronViewModel

@Composable
fun GymScreen(
    viewModel: IronViewModel,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val textPrimary = if (isDark) TextPrimaryDark else TextPrimaryLight
    val textSecondary = if (isDark) TextSecondaryDark else TextSecondaryLight
    val cardBg = if (isDark) CardDark else CardLight
    val cardBorder = if (isDark) CardBorderDark else CardBorderLight

    var showExerciseDetail by remember { mutableStateOf<Exercise?>(null) }
    var selectedMuscleFilter by remember { mutableStateOf("All") }
    var selectedDiffFilter by remember { mutableStateOf("All") }
    var searchExerciseQuery by remember { mutableStateOf("") }

    // Let's use the static exercises list we have in Repository
    val repository = remember { com.example.data.repository.IronRepository(com.example.data.database.IronDatabase.getDatabase(viewModel.getApplication()).ironDao()) }
    val exerciseLibrary = repository.exerciseLibrary
    val gymPlans = repository.gymPlans

    val filteredExercises = remember(selectedMuscleFilter, selectedDiffFilter, searchExerciseQuery) {
        exerciseLibrary.filter { ex ->
            val matchMuscle = selectedMuscleFilter == "All" || ex.muscleGroup.contains(selectedMuscleFilter, ignoreCase = true)
            val matchDiff = selectedDiffFilter == "All" || ex.difficulty.equals(selectedDiffFilter, ignoreCase = true)
            val matchQuery = searchExerciseQuery.isEmpty() || ex.name.contains(searchExerciseQuery, ignoreCase = true)
            matchMuscle && matchDiff && matchQuery
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDark) BackgroundDark else BackgroundLight)
    ) {
        CurvedHeader(
            title = "IronGym Split",
            subtitle = "Choose a workout template or browse library",
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
            // Tab Header 1: Workout Plans
            item {
                Text(
                    text = "⚙️ Quick-Start Templates",
                    color = textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            items(gymPlans) { plan ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("plan_card_${plan.id}"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, cardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = plan.name,
                                color = textPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .background(PrimaryGreen.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = plan.difficulty,
                                    color = PrimaryGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = plan.description,
                            color = textSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Text(
                            text = "Exercises: ${plan.exerciseIds.size} movements",
                            color = textPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.selectWorkoutPlanAndStart(plan) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("start_plan_${plan.id}"),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen, contentColor = Color.Black),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "start")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Start Workout Session", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Tab Header 2: Search Exercises
            item {
                Text(
                    text = "📚 Exercise Library",
                    color = textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            item {
                // Search field
                OutlinedTextField(
                    value = searchExerciseQuery,
                    onValueChange = { searchExerciseQuery = it },
                    placeholder = { Text("Search 10+ movements...") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "search") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = cardBorder,
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("exercise_search")
                )
            }

            item {
                // Horizontal Filters
                Text(text = "Filter by Muscle:", color = textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                val muscles = listOf("All", "Chest", "Legs", "Back", "Shoulders", "Arms")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    muscles.forEach { m ->
                        val selected = selectedMuscleFilter == m
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) PrimaryGreen else cardBg)
                                .border(1.dp, if (selected) PrimaryGreen else cardBorder, RoundedCornerShape(8.dp))
                                .clickable { selectedMuscleFilter = m }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("filter_muscle_$m")
                        ) {
                            Text(
                                text = m,
                                color = if (selected) Color.Black else textPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Exercise items
            if (filteredExercises.isEmpty()) {
                item {
                    Text(
                        text = "No movements found under that segment. 🙅‍♂️",
                        color = textSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp)
                    )
                }
            } else {
                items(filteredExercises) { ex ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(cardBg)
                            .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                            .clickable { showExerciseDetail = ex }
                            .padding(12.dp)
                            .testTag("exercise_row_${ex.id}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Thumbnail
                        AsyncImage(
                            model = ex.thumbnail,
                            contentDescription = "thumb",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(cardBorder)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = ex.name,
                                color = textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier.padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(PrimaryGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(ex.muscleGroup, color = PrimaryGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                                Box(
                                    modifier = Modifier
                                        .background(textSecondary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(ex.equipment, color = textPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "detail",
                            tint = textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Modal detail popups
    val detail = showExerciseDetail
    if (detail != null) {
        ExerciseDetailDialog(
            exercise = detail,
            isDark = isDark,
            cardBg = cardBg,
            textPrimary = textPrimary,
            textSecondary = textSecondary,
            onDismiss = { showExerciseDetail = null }
        )
    }
}

@Composable
fun ExerciseDetailDialog(
    exercise: Exercise,
    isDark: Boolean,
    cardBg: Color,
    textPrimary: Color,
    textSecondary: Color,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = PrimaryGreen)
            ) {
                Text("Got It! 💪", fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Text(
                text = exercise.name,
                color = textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AsyncImage(
                    model = exercise.thumbnail,
                    contentDescription = "banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(12.dp))
                )

                Text(
                    text = "🎯 Targets: ${exercise.muscleMap}",
                    color = PrimaryGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Instructions:",
                    color = textPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                exercise.instructions.forEachIndexed { idx, step ->
                    Text(
                        text = "${idx + 1}. $step",
                        color = textSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }

                Text(
                    text = "Common Mistakes to Avoid:",
                    color = DangerColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                exercise.commonMistakes.forEach { mistake ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("❌", fontSize = 10.sp)
                        Text(
                            text = mistake,
                            color = textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        },
        containerColor = cardBg,
        textContentColor = textPrimary
    )
}

@Composable
fun ActiveWorkoutSessionScreen(
    viewModel: IronViewModel,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val plan by viewModel.activeWorkoutPlan.collectAsState()
    val timerSeconds by viewModel.workoutTimerSeconds.collectAsState()
    val activeExercises by viewModel.activeSessionExercises.collectAsState()
    val activeIndex by viewModel.activeExerciseIndex.collectAsState()
    val rTimerSeconds by viewModel.restTimerSeconds.collectAsState()

    val textPrimary = if (isDark) TextPrimaryDark else TextPrimaryLight
    val textSecondary = if (isDark) TextSecondaryDark else TextSecondaryLight
    val cardBg = if (isDark) CardDark else CardLight
    val cardBorder = if (isDark) CardBorderDark else CardBorderLight

    val count = activeExercises.size
    val currentPair = if (activeIndex in 0 until count) activeExercises[activeIndex] else null

    val focusManager = LocalFocusManager.current

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
            // Header Active Workout stopwatch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = plan?.name ?: "Active Session",
                        color = textPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "⏱️ Duration: ${viewModel.formatWorkoutTimer(timerSeconds)}",
                        color = PrimaryGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { viewModel.cancelWorkout() },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerColor.copy(alpha = 0.15f), contentColor = DangerColor),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("Quit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Step Progress bar
            val displayIndex = (activeIndex + 1).coerceAtMost(count)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Exercise $displayIndex of $count",
                    color = textPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${((displayIndex.toFloat() / count.toFloat()) * 100).toInt()}% Done",
                    color = textSecondary,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { displayIndex.toFloat() / count.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = PrimaryGreen,
                trackColor = textSecondary.copy(alpha = 0.15f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Active exercise workspace Card
            if (currentPair != null) {
                val exercise = currentPair.first
                val setsList = currentPair.second

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = BorderStroke(1.dp, cardBorder)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = exercise.thumbnail,
                                contentDescription = "thumb",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = exercise.name,
                                    color = textPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Target: ${exercise.setsByGoal} | ${exercise.muscleGroup}",
                                    color = textSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // Sets Log Table
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = BorderStroke(1.dp, cardBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Table Headers
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("SET", color = textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.6f))
                                Text("WEIGHT (KG)", color = textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f), textAlign = TextAlign.Center)
                                Text("REPS", color = textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f), textAlign = TextAlign.Center)
                                Text("DONE", color = textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            }

                            setsList.forEachIndexed { idx, setLog ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Set tag
                                    Box(
                                        modifier = Modifier
                                            .weight(0.6f)
                                            .size(24.dp)
                                            .background(
                                                if (setLog.isDone) PrimaryGreen else textSecondary.copy(alpha = 0.2f),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "${setLog.setNum}",
                                            color = if (setLog.isDone) Color.Black else textPrimary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Weight text input
                                    OutlinedTextField(
                                        value = if (setLog.weight == 0.0) "" else setLog.weight.toString(),
                                        onValueChange = { weightStr ->
                                            val w = weightStr.toDoubleOrNull() ?: 0.0
                                            viewModel.updateSetLogValue(exercise.id, idx, w, null)
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = PrimaryGreen,
                                            unfocusedBorderColor = cardBorder,
                                            focusedTextColor = textPrimary,
                                            unfocusedTextColor = textPrimary
                                        ),
                                        modifier = Modifier
                                            .weight(1.5f)
                                            .height(44.dp)
                                            .padding(horizontal = 4.dp)
                                            .testTag("weight_input_${exercise.id}_$idx"),
                                        shape = RoundedCornerShape(6.dp),
                                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, textAlign = TextAlign.Center)
                                    )

                                    // Reps text input
                                    OutlinedTextField(
                                        value = if (setLog.reps == 0) "" else setLog.reps.toString(),
                                        onValueChange = { repsStr ->
                                            val r = repsStr.toIntOrNull() ?: 0
                                            viewModel.updateSetLogValue(exercise.id, idx, null, r)
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = PrimaryGreen,
                                            unfocusedBorderColor = cardBorder,
                                            focusedTextColor = textPrimary,
                                            unfocusedTextColor = textPrimary
                                        ),
                                        modifier = Modifier
                                            .weight(1.2f)
                                            .height(44.dp)
                                            .padding(horizontal = 4.dp)
                                            .testTag("reps_input_${exercise.id}_$idx"),
                                        shape = RoundedCornerShape(6.dp),
                                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, textAlign = TextAlign.Center)
                                    )

                                    // Checkmark
                                    Checkbox(
                                        checked = setLog.isDone,
                                        onCheckedChange = {
                                            focusManager.clearFocus()
                                            viewModel.toggleSetCollected(exercise.id, idx)
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = PrimaryGreen),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("set_done_${exercise.id}_$idx")
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Next / Prev Exercise Navigation or Finish Session
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (activeIndex > 0) {
                    OutlinedButton(
                        onClick = { viewModel.activeExerciseIndex.value -= 1 },
                        modifier = Modifier
                            .testTag("active_prev")
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryGreen),
                        border = BorderStroke(1.dp, PrimaryGreen)
                    ) {
                        Text("← Prev", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Spacer(modifier = Modifier.width(20.dp))
                }

                if (activeIndex < count - 1) {
                    Button(
                        onClick = { viewModel.activeExerciseIndex.value += 1 },
                        modifier = Modifier
                            .testTag("active_next")
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen, contentColor = Color.Black)
                    ) {
                        Text("Next Exercise →", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { viewModel.completeWorkout() },
                        modifier = Modifier
                            .testTag("active_finish")
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen, contentColor = Color.Black)
                    ) {
                        Text("Finish Workout 🎉", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Rest Floating Countdown overlay
        if (rTimerSeconds > 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .clickable { /* Block clicks */ },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "REST INTERVAL ACTIVE 🧘",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(18.dp))

                    SimpleCircularTimer(
                        percentage = rTimerSeconds.toFloat() / 45f,
                        timeText = "${rTimerSeconds}s",
                        color = PrimaryGreen
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.skipRestTimer() },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Skip Rest (Resume) 👍", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PostWorkoutSummaryScreen(
    viewModel: IronViewModel,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val textPrimary = if (isDark) TextPrimaryDark else TextPrimaryLight
    val textSecondary = if (isDark) TextSecondaryDark else TextSecondaryLight
    val cardBg = if (isDark) CardDark else CardLight
    val cardBorder = if (isDark) CardBorderDark else CardBorderLight

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDark) BackgroundDark else BackgroundLight)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            // Celebration Animated Badge
            Text(text = "🏆", fontSize = 64.sp)
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "WORKOUT COMPLETE!",
                color = PrimaryGreen,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = viewModel.summaryWorkoutName,
                color = textPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Stats Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, cardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SummaryRow(label = "⏱️ Duration", value = viewModel.summaryDuration, textPrimary = textPrimary)
                    SummaryRow(label = "🔥 Calories Burned", value = "${viewModel.summaryCaloriesBurned} kcal (MET)", textPrimary = textPrimary)
                    SummaryRow(label = "🏋️ Total Weight Volume", value = "${viewModel.summaryVolumeKg.toInt()} kg", textPrimary = textPrimary)
                    SummaryRow(label = "✅ Movements Logged", value = "${viewModel.summaryExercisesCount} exercises", textPrimary = textPrimary)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Post workout meal suggestion
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "🍗 Post-Gym Recovery Meal",
                        color = textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Log protein resources now to rebuild muscle fibers. Suggested: Chicken breast, 2-boiled eggs or Whey milk shake.",
                        color = textSecondary,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )

                    Button(
                        onClick = {
                            viewModel.currentTab.value = 2 // Meal Tab
                            viewModel.activeScreen.value = ActiveScreen.MAIN_TAB
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .testTag("goto_meals_summary")
                            .height(36.dp)
                    ) {
                        Text("Open Meal Tracker 🥣", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Button(
            onClick = {
                viewModel.activeScreen.value = ActiveScreen.MAIN_TAB
                viewModel.currentTab.value = 1 // Retain Gym
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("summary_back_home"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen, contentColor = Color.Black)
        ) {
            Text("Back to Dashboard", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun SummaryRow(
    label: String,
    value: String,
    textPrimary: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Text(text = value, color = textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
