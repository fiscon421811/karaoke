package br.com.karaokebrasil.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.tv.material3.Button
import androidx.tv.material3.OutlinedButton
import androidx.tv.material3.Text
import br.com.karaokebrasil.data.Musica
import br.com.karaokebrasil.ui.theme.Destaque
import br.com.karaokebrasil.ui.theme.TextoSecundario

@Composable
fun OpcoesMusicaDialog(
    musica: Musica,
    temLetra: Boolean,
    onCantar: () -> Unit,
    onAdicionarNaFila: () -> Unit,
    onAlternarFavorita: () -> Unit,
    onFechar: () -> Unit,
) {
    val foco = remember { FocusRequester() }
    Dialog(onDismissRequest = onFechar) {
        Column(
            modifier = Modifier
                .width(460.dp)
                .background(Color(0xFF1B1B33), RoundedCornerShape(20.dp))
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = "Nº ${musica.codigo} • ${musica.genero.nome}", color = Destaque, fontSize = 14.sp)
            Text(
                text = musica.titulo,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Text(text = musica.artista, color = TextoSecundario, fontSize = 18.sp)
            if (!temLetra) {
                Text(
                    text = "Letra ainda não instalada para esta música.",
                    color = TextoSecundario,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = onCantar, modifier = Modifier.fillMaxWidth().focusRequester(foco)) {
                Text("🎤  Cantar agora")
            }
            OutlinedButton(onClick = onAdicionarNaFila, modifier = Modifier.fillMaxWidth()) {
                Text("➕  Adicionar à fila")
            }
            OutlinedButton(onClick = onAlternarFavorita, modifier = Modifier.fillMaxWidth()) {
                Text(if (musica.favorita) "💔  Remover das favoritas" else "❤  Adicionar às favoritas")
            }
        }
        LaunchedEffect(Unit) { runCatching { foco.requestFocus() } }
    }
}
