package br.com.karaokebrasil.ui.fila

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Button
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.OutlinedButton
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import br.com.karaokebrasil.data.Musica
import br.com.karaokebrasil.ui.KaraokeViewModel
import br.com.karaokebrasil.ui.componentes.Tela
import br.com.karaokebrasil.ui.theme.Destaque
import br.com.karaokebrasil.ui.theme.TextoSecundario

@Composable
fun FilaScreen(
    vm: KaraokeViewModel,
    onCantar: (Musica) -> Unit,
) {
    val fila by vm.filaDeMusicas.collectAsStateWithLifecycle()

    Tela(
        titulo = "Fila de músicas",
        subtitulo = if (fila.isEmpty()) {
            "A fila está vazia. Escolha uma música e selecione \"Adicionar à fila\"."
        } else {
            "${fila.size} na fila • selecione uma música para removê-la"
        },
    ) {
        if (fila.isNotEmpty()) {
            Row(Modifier.padding(vertical = 16.dp)) {
                Button(onClick = { vm.proximaDaFila()?.let(onCantar) }) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Começar a fila")
                }
                Spacer(Modifier.width(12.dp))
                OutlinedButton(onClick = vm::limparFila) {
                    Icon(Icons.Filled.Delete, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Limpar fila")
                }
            }
        }
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
        ) {
            itemsIndexed(fila) { indice, musica ->
                Surface(
                    onClick = { vm.removerDaFila(indice) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = Color(0xFF1B1B33),
                        focusedContainerColor = Color(musica.genero.cor).copy(alpha = 0.6f),
                    ),
                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("${indice + 1}.", fontSize = 22.sp, color = Destaque, fontWeight = FontWeight.Bold)
                        Column(Modifier.padding(start = 16.dp).weight(1f)) {
                            Text(musica.titulo, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                            Text(musica.artista, fontSize = 15.sp, color = TextoSecundario)
                        }
                        Text("Nº ${musica.codigo}", color = TextoSecundario)
                    }
                }
            }
        }
    }
}
