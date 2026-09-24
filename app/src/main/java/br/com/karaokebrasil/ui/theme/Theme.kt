package br.com.karaokebrasil.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

val Fundo = Color(0xFF0B0B1A)
val Destaque = Color(0xFFFFC300)
val TextoSecundario = Color(0xFFB8B8D0)

private val cores = darkColorScheme(
    primary = Destaque,
    onPrimary = Color.Black,
    secondary = Color(0xFF00B4D8),
    background = Fundo,
    onBackground = Color.White,
    surface = Color(0xFF1B1B33),
    onSurface = Color.White,
)

@Composable
fun KaraokeTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = cores, content = content)
}
