package au.edu.jcu.assessment.studylab.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val StudyPalette = lightColorScheme(
    primary = Color(0xFF20594B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD5E9DF),
    onPrimaryContainer = Color(0xFF153B32),
    secondary = Color(0xFF8B3D26),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDBCE),
    onSecondaryContainer = Color(0xFF4B1C0B),
    background = Color(0xFFF6F5F0),
    onBackground = Color(0xFF202B27),
    surface = Color(0xFFFFFEFA),
    onSurface = Color(0xFF202B27),
    surfaceVariant = Color(0xFFE9EEE8),
    onSurfaceVariant = Color(0xFF4B5751),
    error = Color(0xFFB3261E)
)

private val StudyDarkPalette = darkColorScheme(
    primary = Color(0xFFA4D2C0),
    onPrimary = Color(0xFF123B31),
    primaryContainer = Color(0xFF285548),
    onPrimaryContainer = Color(0xFFD5E9DF),
    secondary = Color(0xFFFFB59D),
    onSecondary = Color(0xFF5A1D0B),
    secondaryContainer = Color(0xFF71301B),
    onSecondaryContainer = Color(0xFFFFDBCE),
    background = Color(0xFF101512),
    onBackground = Color(0xFFE0E7E1),
    surface = Color(0xFF171E1A),
    onSurface = Color(0xFFE0E7E1),
    surfaceVariant = Color(0xFF29332D),
    onSurfaceVariant = Color(0xFFC0CAC2),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

@Composable
fun StudyLabTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) StudyDarkPalette else StudyPalette,
        content = content
    )
}
