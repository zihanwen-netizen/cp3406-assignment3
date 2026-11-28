package au.edu.jcu.assessment.studylab.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.Density
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import au.edu.jcu.assessment.studylab.ui.screens.ActivityScreen
import au.edu.jcu.assessment.studylab.ui.screens.HomeScreen
import au.edu.jcu.assessment.studylab.ui.screens.SettingsScreen
import au.edu.jcu.assessment.studylab.ui.screens.StatisticsScreen
import au.edu.jcu.assessment.studylab.ui.viewmodel.StudyLabViewModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
sealed interface StudyRoute {
    @Serializable @SerialName("home") data object Home : StudyRoute
    @Serializable @SerialName("practice") data object Practice : StudyRoute
    @Serializable @SerialName("progress") data object Progress : StudyRoute
    @Serializable @SerialName("settings") data object Settings : StudyRoute
}

private data class NavItem(
    val route: StudyRoute,
    val routeName: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun StudyLabApp(viewModel: StudyLabViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val navItems = listOf(
        NavItem(StudyRoute.Home, "home", "Home", Icons.Outlined.Home),
        NavItem(StudyRoute.Practice, "practice", "Practice", Icons.AutoMirrored.Outlined.MenuBook),
        NavItem(StudyRoute.Progress, "progress", "Progress", Icons.Outlined.BarChart),
        NavItem(StudyRoute.Settings, "settings", "Settings", Icons.Outlined.Settings)
    )
    val currentDestination = backStack?.destination
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
                            selected = currentDestination?.hierarchy?.any { destination ->
                                destination.route == item.routeName
                            } == true,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = { androidx.compose.material3.Text(item.label) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = StudyRoute.Home,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable<StudyRoute.Home> {
                    HomeScreen(
                        state = state,
                        onStartPractice = {
                            viewModel.startOrResumeSession()
                            navController.navigate(StudyRoute.Practice) { launchSingleTop = true }
                        },
                        onSaveStudyPlace = viewModel::setStudyPlace
                    )
                }
                composable<StudyRoute.Practice> {
                    ActivityScreen(
                        state = state,
                        onStart = viewModel::startSession,
                        onSelect = viewModel::selectAnswer,
                        onCheck = viewModel::checkAnswer,
                        onNext = viewModel::nextQuestion,
                        onTryAgain = viewModel::startSession,
                        onDone = {
                            viewModel.clearSession()
                            navController.navigate(StudyRoute.Home) {
                                popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable<StudyRoute.Progress> { StatisticsScreen(state) }
                composable<StudyRoute.Settings> {
                    SettingsScreen(
                        state = state,
                        onTopic = viewModel::setTopic,
                        onDifficulty = viewModel::setDifficulty,
                        onLargeText = viewModel::setLargeText,
                        onClearData = viewModel::clearLocalData
                    )
                }
            }
        }
    }
}
