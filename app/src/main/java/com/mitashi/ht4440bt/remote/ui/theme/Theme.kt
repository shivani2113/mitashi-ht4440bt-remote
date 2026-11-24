package com.mitashi.ht4440bt.remote.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val RemoteBodyDark = Color(0xFF171A21)
val RemoteSurfaceDark = Color(0xFF232833)
val RemoteKeyDark = Color(0xFF343B4A)
val RemoteKeyBorderDark = Color(0xFF475063)
val PowerRed = Color(0xFFE53935)
val AccentTeal = Color(0xFF26A69A)

val RemoteBodyLight = Color(0xFFF3F5F8)
val RemoteSurfaceLight = Color(0xFFFFFFFF)
val RemoteKeyLight = Color(0xFFE5E9F0)
val RemoteKeyBorderLight = Color(0xFFCBD2DF)

private val DarkColors = darkColorScheme(
    primary = AccentTeal,
    secondary = Color(0xFF78909C),
    background = Color(0xFF0E1015),
    surface = RemoteBodyDark,
    surfaceVariant = RemoteSurfaceDark,
    onBackground = Color(0xFFF2F4F8),
    onSurface = Color(0xFFF2F4F8),
    onSurfaceVariant = Color(0xFFAAB3C5),
    error = PowerRed
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF00796B),
    secondary = Color(0xFF546E7A),
    background = Color(0xFFEAECEF),
    surface = RemoteBodyLight,
    surfaceVariant = RemoteSurfaceLight,
    onBackground = Color(0xFF161922),
    onSurface = Color(0xFF161922),
    onSurfaceVariant = Color(0xFF525C70),
    error = PowerRed
)

@Composable
fun MitashiRemoteTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
