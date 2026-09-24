package br.com.karaokebrasil.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Button
import androidx.tv.material3.Icon
import androidx.tv.material3.OutlinedButton
import androidx.tv.material3.Text
import br.com.karaokebrasil.data.Genero
import br.com.karaokebrasil.data.Musica
import br.com.karaokebrasil.ui.KaraokeViewModel
import br.com.karaokebrasil.ui.componentes.MusicaCard
import br.com.karaokebrasil.ui.theme.Destaque
import br.com.karaokebrasil.ui.theme.Fundo
import br.com.karaokebrasil.ui.theme.TextoSecundario

private const val MUSICAS_POR_LINHA = 12

@Composable
fun HomeScreen(
    vm: KaraokeViewModel,
    onSelecionar: (Musica) -> Unit,
    onAbrirGenero: (Genero) -> Unit,
    onBuscar: () -> Unit,
    onFila: () -> Unit,
    onFavoritas: () -> Unit,
) {
    val porGenero by vm.musicasPorGenero.collectAsStateWithLifecycle()
    val maisCantadas by vm.maisCantadas.collectAsStateWithLifecycle()
    val fila by vm.filaDeMusicas.collectAsStateWithLifecycle()
    val comLetra by vm.comLetra.collectAsStateWithLifecycle()
    val focoInicial = remember { FocusRequester() }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Fundo),
        contentPadding = PaddingValues(horizontal = 48.dp, vertical = 27.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(56.dp).background(Destaque, CircleShape),
                    contentAlignment = Alignment.Center,
                ) { Text("🎤", fontSize = 28.sp) }
                Column(Modifier.padding(start = 16.dp)) {
                    Text("Karaokê Brasil", fontSize = 36.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "${porGenero.values.sumOf { it.size }} músicas brasileiras • ${porGenero.size} gêneros",
                        color = TextoSecundario,
                        fontSize = 15.sp,
                    )
                }
                Spacer(Modifier.weight(1f))
                BotaoTopo("Buscar", Icons.Filled.Search, onBuscar, Modifier.focusRequester(focoInicial))
                Spacer(Modifier.width(12.dp))
                BotaoTopo("Favoritas", Icons.Filled.Favorite, onFavoritas)
                Spacer(Modifier.width(12.dp))
                Button(onClick = onFila) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Fila (${fila.size})")
                }
            }
        }

        if (maisCantadas.isNotEmpty()) {
            item {
                LinhaDeMusicas(
                    titulo = "⭐ Mais cantadas",
                    cor = Destaque,
                    musicas = maisCantadas,
                    comLetra = comLetra,
                    onSelecionar = onSelecionar,
                )
            }
        }

        porGenero.forEach { (genero, musicas) ->
            item(key = genero.id) {
                LinhaDeMusicas(
                    titulo = genero.nome,
                    cor = Color(genero.cor),
                    musicas = musicas.take(MUSICAS_POR_LINHA),
                    comLetra = comLetra,
                    onSelecionar = onSelecionar,
                    totalNoGenero = musicas.size,
                    onVerTodas = { onAbrirGenero(genero) },
                )
            }
        }
    }

    LaunchedEffect(Unit) { runCatching { focoInicial.requestFocus() } }
}

@Composable
private fun BotaoTopo(texto: String, icone: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(onClick = onClick, modifier = modifier) {
        Icon(icone, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(texto)
    }
}

@Composable
private fun LinhaDeMusicas(
    titulo: String,
    cor: Color,
    musicas: List<Musica>,
    comLetra: Set<Int>,
    onSelecionar: (Musica) -> Unit,
    totalNoGenero: Int? = null,
    onVerTodas: (() -> Unit)? = null,
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(width = 6.dp, height = 26.dp).background(cor))
            Text(
                text = titulo,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 12.dp),
            )
            if (totalNoGenero != null) {
                Text(
                    text = "  $totalNoGenero músicas",
                    fontSize = 15.sp,
                    color = TextoSecundario,
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(vertical = 10.dp, horizontal = 4.dp),
        ) {
            items(musicas, key = { it.codigo }) { musica ->
                MusicaCard(
                    musica = musica,
                    temLetra = musica.codigo in comLetra,
                    onClick = { onSelecionar(musica) },
                )
            }
            if (onVerTodas != null) {
                item {
                    OutlinedButton(onClick = onVerTodas, modifier = Modifier.height(136.dp)) {
                        Text("Ver todas ›", fontSize = 18.sp)
                    }
                }
            }
        }
    }
}
