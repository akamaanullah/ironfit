package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun CurvedHeader(
    title: String,
    subtitle: String = "",
    isDark: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier,
    trailingContent: @Composable (() -> Unit)? = null
) {
    val headerBrush = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(Color(0xFF0F2027), Color(0xFF0D0D0D))
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(Color(0xFFE0F2F1), Color(0xFFF9F9F9))
        )
    }
    val contentColor = if (isDark) TextPrimaryDark else TextPrimaryLight
    val subColor = if (isDark) TextSecondaryDark else TextSecondaryLight

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .background(headerBrush)
            .drawBehind {
                val path = Path().apply {
                    moveTo(0f, size.height)
                    quadraticTo(
                        size.width / 2, size.height + 40f,
                        size.width, size.height
                    )
                    lineTo(size.width, 0f)
                    lineTo(0f, 0f)
                    close()
                }
                drawPath(path, color = if (isDark) BackgroundDark else BackgroundLight)
            }
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = contentColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                if (subtitle.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        color = subColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (trailingContent != null) {
                    trailingContent()
                    Spacer(modifier = Modifier.width(8.dp))
                }
                // Mode Toggle Switch
                IconButton(
                    onClick = onToggleTheme,
                    modifier = Modifier
                        .testTag("theme_toggle")
                        .size(44.dp)
                        .background(
                            color = if (isDark) Color(0x1AFFFFA5) else Color(0x19000000),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Text(
                        text = if (isDark) "☀️" else "🌙",
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun MacroRingTracker(
    maxCalories: Double,
    currentCalories: Double,
    proteinTarget: Double,
    currentProtein: Double,
    carbsTarget: Double,
    currentCarbs: Double,
    fatTarget: Double,
    currentFat: Double,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val cardBg = if (isDark) CardDark else CardLight
    val cardBorder = if (isDark) CardBorderDark else CardBorderLight
    val textPrimary = if (isDark) TextPrimaryDark else TextPrimaryLight
    val textSecondary = if (isDark) TextSecondaryDark else TextSecondaryLight

    val caloriePercent = if (maxCalories > 0) ((maxCalories - currentCalories) / maxCalories).toFloat().coerceIn(0f, 1f) else 0f
    val proteinPercent = if (proteinTarget > 0) (currentProtein / proteinTarget).toFloat().coerceIn(0f, 1f) else 0f
    val carbsPercent = if (carbsTarget > 0) (currentCarbs / carbsTarget).toFloat().coerceIn(0f, 1f) else 0f
    val fatPercent = if (fatTarget > 0) (currentFat / fatTarget).toFloat().coerceIn(0f, 1f) else 0f

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Calories Left Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("macro_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = BorderStroke(1.dp, cardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Daily Calorie Budget",
                            color = textSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${(maxCalories - currentCalories).roundToInt()} / ${maxCalories.roundToInt()} kcal",
                            color = textPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "${currentCalories.roundToInt()} left",
                        color = PrimaryGreen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Accent progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(if (isDark) Color(0xFF2A2A2A) else Color(0x19000000), RoundedCornerShape(4.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = caloriePercent)
                            .fillMaxHeight()
                            .background(PrimaryGreen, RoundedCornerShape(4.dp))
                    )
                }
            }
        }

        // Three-Column Macro Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Protein Ring Card
            MacroColCard(
                label = "PROTEIN",
                currentVal = "${currentProtein.roundToInt()}g",
                percent = proteinPercent,
                color = PrimaryGreen,
                isDark = isDark,
                cardBg = cardBg,
                cardBorder = cardBorder,
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                modifier = Modifier.weight(1f)
            )

            // Carbs Ring Card
            MacroColCard(
                label = "CARBS",
                currentVal = "${currentCarbs.roundToInt()}g",
                percent = carbsPercent,
                color = InfoColor,
                isDark = isDark,
                cardBg = cardBg,
                cardBorder = cardBorder,
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                modifier = Modifier.weight(1f)
            )

            // Fats Ring Card
            MacroColCard(
                label = "FATS",
                currentVal = "${currentFat.roundToInt()}g",
                percent = fatPercent,
                color = WarningColor,
                isDark = isDark,
                cardBg = cardBg,
                cardBorder = cardBorder,
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun MacroColCard(
    label: String,
    currentVal: String,
    percent: Float,
    color: Color,
    isDark: Boolean,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 5.dp.toPx()
                    // track
                    drawArc(
                        color = if (isDark) Color(0xFF2A2A2A) else Color(0x11000000),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth)
                    )
                    // arc
                    drawArc(
                        color = color,
                        startAngle = -90f,
                        sweepAngle = percent * 360f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                Text(
                    text = "${(percent * 100).roundToInt()}%",
                    color = textPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = label,
                    color = textSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = currentVal,
                    color = textPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SimpleCircularTimer(
    percentage: Float,
    timeText: String,
    modifier: Modifier = Modifier,
    color: Color = PrimaryGreen
) {
    Box(
        modifier = modifier.size(110.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 8.dp.toPx()
            drawArc(
                color = color.copy(alpha = 0.15f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth)
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = percentage * 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Text(
            text = timeText,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun CustomLineChart(
    points: List<Double>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    isDark: Boolean = true
) {
    val primaryColor = PrimaryGreen
    val gridColor = if (isDark) Color(0x19FFFFFF) else Color(0x19000000)

    Canvas(modifier = modifier) {
        if (points.size < 2) return@Canvas
        val width = size.width
        val height = size.height

        val maxVal = points.maxOrNull()?.coerceAtLeast(80.0) ?: 100.0
        val minVal = points.minOrNull()?.times(0.95) ?: 0.0
        val scaleY = (maxVal - minVal).coerceAtLeast(1.0)

        val stepX = width / (points.size - 1)

        // Draw horizontal grid lines
        for (i in 0..4) {
            val y = height * i / 4
            drawLine(
                color = gridColor,
                start = androidx.compose.ui.geometry.Offset(0f, y),
                end = androidx.compose.ui.geometry.Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Generate coordinates
        val coords = points.mapIndexed { i, p ->
            val x = i * stepX
            val ratioY = ((p - minVal) / scaleY).toFloat()
            val y = height - (ratioY * height)
            androidx.compose.ui.geometry.Offset(x, y)
        }

        // Draw fill gradient path below curve
        val fillPath = Path().apply {
            moveTo(coords.first().x, height)
            coords.forEach { lineTo(it.x, it.y) }
            lineTo(coords.last().x, height)
            close()
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(primaryColor.copy(alpha = 0.3f), Color.Transparent)
            )
        )

        // Draw line curve
        val linePath = Path().apply {
            moveTo(coords.first().x, coords.first().y)
            for (i in 1 until coords.size) {
                lineTo(coords[i].x, coords[i].y)
            }
        }
        drawPath(
            path = linePath,
            color = primaryColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw data circles
        coords.forEach { point ->
            drawCircle(
                color = if (isDark) BackgroundDark else BackgroundLight,
                radius = 6.dp.toPx(),
                center = point
            )
            drawCircle(
                color = primaryColor,
                radius = 4.dp.toPx(),
                center = point,
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}

@Composable
fun ShimmerCardLoader(
    modifier: Modifier = Modifier,
    isDark: Boolean = true
) {
    val cardBg = if (isDark) CardDark else CardLight
    val cardBorder = if (isDark) CardBorderDark else CardBorderLight

    val infiniteTransition = rememberInfiniteTransition(label = "shimmer_trans")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha_anim"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(18.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Gray.copy(alpha = alphaAnim))
            )
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Gray.copy(alpha = alphaAnim * 0.7f))
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Gray.copy(alpha = alphaAnim * 0.7f))
            )
        }
    }
}
