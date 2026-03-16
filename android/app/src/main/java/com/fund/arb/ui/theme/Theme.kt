package com.fund.arb.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Card,
    background = Background,
    surface = Card,
    onSurface = TextPrimary,
    onBackground = TextPrimary
)

@Composable
fun FundArbTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
