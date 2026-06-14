package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.IronViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    viewModel: IronViewModel,
    modifier: Modifier = Modifier
) {
    val step by viewModel.onboardStep.collectAsState()
    val isDark by viewModel.isDarkTheme.collectAsState()

    val bg = if (isDark) BackgroundDark else BackgroundLight
    val textPrimary = if (isDark) TextPrimaryDark else TextPrimaryLight
    val textSecondary = if (isDark) TextSecondaryDark else TextSecondaryLight
    val cardBg = if (isDark) CardDark else CardLight
    val cardBorder = if (isDark) CardBorderDark else CardBorderLight

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Logo
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🇮🇵 ",
                    fontSize = 24.sp
                )
                Text(
                    text = "IRON",
                    color = PrimaryGreen,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "FIT",
                    color = textPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
            Text(
                text = "YOUR PREMIUM PERSONAL TRAINER",
                color = textSecondary,
                fontSize = 10.sp,
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Step Indicator dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                for (i in 0..3) {
                    val active = i == step
                    val color = if (active) PrimaryGreen else textSecondary.copy(alpha = 0.3f)
                    val width = if (active) 24.dp else 8.dp
                    Box(
                        modifier = Modifier
                            .size(height = 6.dp, width = width)
                            .background(color, RoundedCornerShape(3.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Animated Screen Step changes
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.TopCenter
            ) {
                when (step) {
                    0 -> StepZero(viewModel, textPrimary, textSecondary, cardBg, cardBorder)
                    1 -> StepOne(viewModel, textPrimary, textSecondary, cardBg, cardBorder)
                    2 -> StepTwo(viewModel, textPrimary, textSecondary, cardBg, cardBorder)
                    3 -> StepThree(viewModel, textPrimary, textSecondary, cardBg, cardBorder)
                }
            }

            // Next & Previous Control Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (step > 0) {
                    OutlinedButton(
                        onClick = { viewModel.prevOnboardStep() },
                        modifier = Modifier
                            .testTag("onboard_prev")
                            .height(52.dp)
                            .weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, PrimaryGreen),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryGreen)
                    ) {
                        Text(
                            text = "Back",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }

                Button(
                    onClick = { viewModel.nextOnboardStep() },
                    modifier = Modifier
                        .testTag("onboard_next")
                        .height(52.dp)
                        .weight(1.5f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreen,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = if (step == 3) "Complete Profile 💪" else "Continue →",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
fun StepZero(
    viewModel: IronViewModel,
    textPrimary: Color,
    textSecondary: Color,
    cardBg: Color,
    cardBorder: Color
) {
    val isMale by viewModel.onboardIsMale.collectAsState()
    val age by viewModel.onboardAge.collectAsState()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome to IronFit!",
            color = textPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Let's personalize your targets. First, tell us your gender and age.",
            color = textSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp, bottom = 28.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Male Picker card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (isMale) PrimaryGreen.copy(alpha = 0.15f) else cardBg)
                    .border(
                        BorderStroke(
                            width = if (isMale) 2.dp else 1.dp,
                            color = if (isMale) PrimaryGreen else cardBorder
                        ),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .clickable { viewModel.onboardIsMale.value = true }
                    .padding(24.dp)
                    .testTag("gender_male"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "💪", fontSize = 42.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Male",
                        color = textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Female Picker card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (!isMale) PrimaryGreen.copy(alpha = 0.15f) else cardBg)
                    .border(
                        BorderStroke(
                            width = if (!isMale) 2.dp else 1.dp,
                            color = if (!isMale) PrimaryGreen else cardBorder
                        ),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .clickable { viewModel.onboardIsMale.value = false }
                    .padding(24.dp)
                    .testTag("gender_female"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🧘‍♀️", fontSize = 42.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Female",
                        color = textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        OutlinedTextField(
            value = age,
            onValueChange = { viewModel.onboardAge.value = it },
            label = { Text("What is your age?") },
            placeholder = { Text("e.g. 24") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryGreen,
                focusedLabelColor = PrimaryGreen,
                unfocusedBorderColor = cardBorder,
                focusedTextColor = textPrimary,
                unfocusedTextColor = textPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("onboard_age_input"),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
fun StepOne(
    viewModel: IronViewModel,
    textPrimary: Color,
    textSecondary: Color,
    cardBg: Color,
    cardBorder: Color
) {
    val weight by viewModel.onboardWeight.collectAsState()
    val height by viewModel.onboardHeight.collectAsState()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Your Metrics",
            color = textPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "These numbers assist us in computing your Body Mass Index (BMI) and metabolic output.",
            color = textSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp, bottom = 28.dp)
        )

        OutlinedTextField(
            value = weight,
            onValueChange = { viewModel.onboardWeight.value = it },
            label = { Text("Current Weight (kg)") },
            placeholder = { Text("e.g. 70") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryGreen,
                focusedLabelColor = PrimaryGreen,
                unfocusedBorderColor = cardBorder,
                focusedTextColor = textPrimary,
                unfocusedTextColor = textPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("onboard_weight_input"),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = height,
            onValueChange = { viewModel.onboardHeight.value = it },
            label = { Text("Height (cm)") },
            placeholder = { Text("e.g. 175") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryGreen,
                focusedLabelColor = PrimaryGreen,
                unfocusedBorderColor = cardBorder,
                focusedTextColor = textPrimary,
                unfocusedTextColor = textPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("onboard_height_input"),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
fun StepTwo(
    viewModel: IronViewModel,
    textPrimary: Color,
    textSecondary: Color,
    cardBg: Color,
    cardBorder: Color
) {
    val activeLevel by viewModel.onboardActivity.collectAsState()
    val levels = listOf(
        Pair("Sedentary", "Desk work, little or no gym exercise"),
        Pair("Light", "Light training 1-3 times a week"),
        Pair("Moderate", "Moderate training 3-5 times a week"),
        Pair("Active", "Heavy training 6-7 times a week"),
        Pair("Athlete", "Daily intense training, competitive")
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Activity Budget",
            color = textPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Select an option that closely resembles your weekly lifestyle output.",
            color = textSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            levels.forEach { level ->
                val active = activeLevel == level.first
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (active) PrimaryGreen.copy(alpha = 0.15f) else cardBg)
                        .border(
                            BorderStroke(
                                width = if (active) 2.dp else 1.dp,
                                color = if (active) PrimaryGreen else cardBorder
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { viewModel.onboardActivity.value = level.first }
                        .padding(14.dp)
                        .testTag("activity_${level.first}"),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Column {
                        Text(
                            text = level.first,
                            color = textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = level.second,
                            color = textSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StepThree(
    viewModel: IronViewModel,
    textPrimary: Color,
    textSecondary: Color,
    cardBg: Color,
    cardBorder: Color
) {
    val activeGoal by viewModel.onboardGoal.collectAsState()
    val goals = listOf(
        Pair("Weight Loss", "Decline fat, build definition (TDEE - 500)"),
        Pair("Muscle Gain", "Bulking, build strength & size (TDEE + 300)"),
        Pair("Body Recomposition", "Lose fat and build muscle simultaneously"),
        Pair("Weight Gain", "Healthy calorie surplus bulking (TDEE + 500)")
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "What is your main goal?",
            color = textPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Choose the goal which structures your diet allocation splits.",
            color = textSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            goals.forEach { goal ->
                val active = activeGoal == goal.first
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (active) PrimaryGreen.copy(alpha = 0.15f) else cardBg)
                        .border(
                            BorderStroke(
                                width = if (active) 2.dp else 1.dp,
                                color = if (active) PrimaryGreen else cardBorder
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { viewModel.onboardGoal.value = goal.first }
                        .padding(16.dp)
                        .testTag("goal_${goal.first.replace(" ", "_")}"),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Column {
                        Text(
                            text = goal.first,
                            color = textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = goal.second,
                            color = textSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
