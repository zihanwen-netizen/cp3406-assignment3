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
    secondary = Color(0xFFBE6246),
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

@Composable
fun StudyLabTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else StudyPalette,
        typography = MaterialTheme.typography,
        content = content
    )
}
