package com.shayshankrathore.irishvisadate.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val IrishColorScheme = lightColorScheme(
    primary            = IrishGreen,
    onPrimary          = Color.White,
    primaryContainer   = IrishGreenLight,
    onPrimaryContainer = IrishGreenDark,
    secondary          = IrishOrange,
    onSecondary        = Color.White,
    secondaryContainer = IrishOrangeLight,
    onSecondaryContainer = IrishOrangeDark,
    background         = IrishGreenBg,
    onBackground       = TextPrimary,
    surface            = SurfaceCard,
    onSurface          = TextPrimary,
    surfaceVariant     = IrishGreenBg,
    onSurfaceVariant   = TextSecondary,
    error              = StatusRed,
    errorContainer     = StatusRedLight,
    onError            = Color.White,
    onErrorContainer   = StatusRed,
    outline            = DividerGreen,
    outlineVariant     = IrishGreenLight,
)

@Composable
fun IrishVisaExpectedDateTheme(
    content: @Composable () -> Unit
) {
    // Always use the Irish theme — ignore system dark/dynamic color
    MaterialTheme(
        colorScheme = IrishColorScheme,
        typography  = Typography,
        content     = content
    )
}
