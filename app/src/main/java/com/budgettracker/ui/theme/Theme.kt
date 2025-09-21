package com.budgettracker.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Primary80,
    onPrimary = Neutral0,
    primaryContainer = Primary20,
    onPrimaryContainer = Primary80,
    
    secondary = Secondary80,
    onSecondary = Neutral0,
    secondaryContainer = Secondary20,
    onSecondaryContainer = Secondary80,
    
    tertiary = Success80,
    onTertiary = Neutral0,
    tertiaryContainer = Success20,
    onTertiaryContainer = Success80,
    
    error = Error80,
    onError = Neutral0,
    errorContainer = Error20,
    onErrorContainer = Error80,
    
    background = Neutral95,
    onBackground = Neutral10,
    surface = Neutral95,
    onSurface = Neutral10,
    
    surfaceVariant = Neutral90,
    onSurfaceVariant = Neutral20,
    outline = Neutral20,
    outlineVariant = Neutral90
)

private val LightColorScheme = lightColorScheme(
    primary = Primary40,
    onPrimary = Neutral0,
    primaryContainer = Primary80,
    onPrimaryContainer = Primary20,
    
    secondary = Secondary40,
    onSecondary = Neutral0,
    secondaryContainer = Secondary80,
    onSecondaryContainer = Secondary20,
    
    tertiary = Success40,
    onTertiary = Neutral0,
    tertiaryContainer = Success80,
    onTertiaryContainer = Success20,
    
    error = Error40,
    onError = Neutral0,
    errorContainer = Error80,
    onErrorContainer = Error20,
    
    background = Neutral0,
    onBackground = Neutral90,
    surface = Neutral0,
    onSurface = Neutral90,
    
    surfaceVariant = Neutral10,
    onSurfaceVariant = Neutral90,
    outline = Neutral90,
    outlineVariant = Neutral20
)

@Composable
fun BudgetTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}


