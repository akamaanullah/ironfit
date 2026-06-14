package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ActiveScreen
import com.example.ui.viewmodel.IronViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val viewModel = ViewModelProvider(this)[IronViewModel::class.java]

        setContent {
            val isDark by viewModel.isDarkTheme.collectAsState()

            MyApplicationTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = if (isDark) BackgroundDark else BackgroundLight
                ) {
                    MainScreenRouter(viewModel = viewModel, isDark = isDark)
                }
            }
        }
    }
}

@Composable
fun MainScreenRouter(
    viewModel: IronViewModel,
    isDark: Boolean
) {
    val activeScreen by viewModel.activeScreen.collectAsState()

    AnimatedContent(
        targetState = activeScreen,
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
        },
        label = "active_screen_router"
    ) { screen ->
        when (screen) {
            ActiveScreen.SPLASH -> SplashScreenContent(viewModel, isDark)
            ActiveScreen.ONBOARDING -> OnboardingScreen(viewModel = viewModel)
            ActiveScreen.MAIN_TAB -> MainTabsController(viewModel = viewModel, isDark = isDark)
            ActiveScreen.ACTIVE_WORKOUT -> ActiveWorkoutSessionScreen(viewModel = viewModel, isDark = isDark)
            ActiveScreen.POST_WORKOUT_SUMMARY -> PostWorkoutSummaryScreen(viewModel = viewModel, isDark = isDark)
            ActiveScreen.ADD_FOOD_FLOW -> AddFoodFlowScreen(viewModel = viewModel, isDark = isDark)
            else -> MainTabsController(viewModel = viewModel, isDark = isDark)
        }
    }
}

@Composable
fun SplashScreenContent(
    viewModel: IronViewModel,
    isDark: Boolean
) {
    val scale = remember { androidx.compose.animation.core.Animatable(0.7f) }
    val alpha = remember { androidx.compose.animation.core.Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(1100, easing = FastOutSlowInEasing)
        )
    }
    LaunchedEffect(Unit) {
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(1100, easing = LinearEasing)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) BackgroundDark else BackgroundLight),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scale.value)
                .testTag("splash_container")
        ) {
            Text(text = "🇮🇵", fontSize = 56.sp)
            Spacer(modifier = Modifier.height(16.dp))

            Row {
                Text(
                    text = "IRON",
                    color = PrimaryGreen,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "FIT",
                    color = if (isDark) Color.White else Color.Black,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }

            Text(
                text = "HYPER-PERSONALIZED FIT LIFE",
                color = if (isDark) TextSecondaryDark else TextSecondaryLight,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun MainTabsController(
    viewModel: IronViewModel,
    isDark: Boolean
) {
    val currentTab by viewModel.currentTab.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = if (isDark) CardDark else CardLight,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .testTag("bottom_nav")
                    .height(84.dp)
                    .drawBehind {
                        drawLine(
                            color = if (isDark) CardBorderDark else CardBorderLight,
                            start = androidx.compose.ui.geometry.Offset(0f, 0f),
                            end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
            ) {
                val items = listOf(
                    NavigationItem("Home", Icons.Filled.Home, Icons.Outlined.Home, 0),
                    NavigationItem("Gym", Icons.Filled.FitnessCenter, Icons.Outlined.FitnessCenter, 1),
                    NavigationItem("Meals", Icons.Filled.Restaurant, Icons.Outlined.Restaurant, 2),
                    NavigationItem("Progress", Icons.Filled.TrendingUp, Icons.Outlined.TrendingUp, 3),
                    NavigationItem("Profile", Icons.Filled.Person, Icons.Outlined.Person, 4)
                )

                items.forEach { tab ->
                    val active = currentTab == tab.index
                    NavigationBarItem(
                        selected = active,
                        onClick = { viewModel.currentTab.value = tab.index },
                        icon = {
                            Icon(
                                imageVector = if (active) tab.filledIcon else tab.outlinedIcon,
                                contentDescription = tab.label,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                fontSize = 11.sp,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = PrimaryGreen,
                            indicatorColor = PrimaryGreen,
                            unselectedIconColor = if (isDark) TextSecondaryDark else TextSecondaryLight,
                            unselectedTextColor = if (isDark) TextSecondaryDark else TextSecondaryLight
                        ),
                        modifier = Modifier.testTag("tab_${tab.label.lowercase()}")
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets.navigationBars
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            when (currentTab) {
                0 -> DashboardScreen(viewModel = viewModel, isDark = isDark)
                1 -> GymScreen(viewModel = viewModel, isDark = isDark)
                2 -> MealsScreen(viewModel = viewModel, isDark = isDark)
                3 -> ProgressTabScreen(viewModel = viewModel, isDark = isDark)
                4 -> ProfileAndSettingsScreen(viewModel = viewModel, isDark = isDark)
            }
        }
    }
}

data class NavigationItem(
    val label: String,
    val filledIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val outlinedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val index: Int
)

