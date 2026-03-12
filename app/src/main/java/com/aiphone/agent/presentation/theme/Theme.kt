package com.aiphone.agent.presentation.theme
import androidx.compose.material3.MaterialTheme; import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable; import androidx.compose.ui.graphics.Color
private val DarkColorScheme = darkColorScheme(
    primary = BrandPrimary, onPrimary = OnPrimary, primaryContainer = BrandPrimaryDark,
    secondary = BrandSecondary, tertiary = BrandTertiary, background = Background,
    surface = Surface, surfaceVariant = SurfaceVariant, onSurface = OnSurface,
    onSurfaceVariant = OnSurfaceVariant, error = AppError,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = Color(0xFF252538), surfaceContainerHighest = Color(0xFF2C2C40)
)
@Composable fun AIPhoneAgentTheme(content: @Composable () -> Unit) =
    MaterialTheme(colorScheme = DarkColorScheme, typography = AppTypography, content = content)