package br.com.karaokebrasil.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import br.com.karaokebrasil.ui.theme.Fundo
import br.com.karaokebrasil.ui.theme.TextoSecundario

/** Estrutura padrão das telas: fundo escuro, margens seguras de TV e título. */
@Composable
fun Tela(
    titulo: String,
    subtitulo: String? = null,
    conteudo: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fundo)
            // Margens de "overscan" recomendadas para TV.
            .padding(horizontal = 48.dp, vertical = 27.dp),
    ) {
        Text(text = titulo, fontSize = 34.sp, fontWeight = FontWeight.Bold)
        if (subtitulo != null) {
            Text(text = subtitulo, fontSize = 16.sp, color = TextoSecundario)
        }
        conteudo()
    }
}
