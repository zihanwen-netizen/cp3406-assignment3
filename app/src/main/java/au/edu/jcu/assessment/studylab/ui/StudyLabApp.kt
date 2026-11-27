package au.edu.jcu.assessment.studylab.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.Density
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import au.edu.jcu.assessment.studylab.ui.screens.ActivityScreen
import au.edu.jcu.assessment.studylab.ui.screens.HomeScreen
import au.edu.jcu.assessment.studylab.ui.screens.SettingsScreen
import au.edu.jcu.assessment.studylab.ui.screens.StatisticsScreen
import au.edu.jcu.assessment.studylab.ui.viewmodel.StudyLabViewModel

private data class NavItem(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Composable
fun StudyLabApp(viewModel: StudyLabViewModel) {
    val state by viewModel.uiState.collectAsState()
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route ?: "home"
    val navItems = listOf(
        NavItem("home", "Home", Icons.Outlined.Home),
        NavItem("activity", "Practice", Icons.AutoMirrored.Outlined.MenuBook),
        NavItem("stats", "Progress", Icons.Outlined.BarChart),
        NavItem("settings", "Settings", Icons.Outlined.Settings)
    )
    val systemDensity = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(
            systemDensity.density,
            systemDensity.fontScale * if (state.preferences.largeText) 1.18f else 1f
        )
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar {
                    navItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { androidx.compose.material3.Text(item.label) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("home") {
                    HomeScreen(
                        state = state,
                        onStartPractice = { navController.navigate("activity") },
                        onSaveStudyPlace = viewModel::setStudyPlace
                    )
                }
                composable("activity") {
                    ActivityScreen(
                        state = state,
                        onStart = viewModel::startSession,
                        onSelect = viewModel::selectAnswer,
                        onCheck = viewModel::checkAnswer,
                        onNext = viewModel::nextQuestion,
                        onTryAgain = viewModel::startSession,
                        onDone = {
                            viewModel.clearSession()
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable("stats") { StatisticsScreen(state) }
                composable("settings") {
                    SettingsScreen(
                        state = state,
                        onTopic = viewModel::setTopic,
                        onDifficulty = viewModel::setDifficulty,
                        onLargeText = viewModel::setLargeText
                    )
                }
            }
        }
    }
}
