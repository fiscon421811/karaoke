package br.com.karaokebrasil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import br.com.karaokebrasil.ui.KaraokeNavHost
import br.com.karaokebrasil.ui.theme.KaraokeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KaraokeTheme {
                KaraokeNavHost()
            }
        }
    }
}
