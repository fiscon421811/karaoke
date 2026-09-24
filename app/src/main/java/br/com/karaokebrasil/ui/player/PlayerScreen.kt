package br.com.karaokebrasil.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Text
import br.com.karaokebrasil.data.Musica
import br.com.karaokebrasil.letra.Letra
import br.com.karaokebrasil.ui.theme.Destaque
import br.com.karaokebrasil.ui.theme.Fundo
import br.com.karaokebrasil.ui.theme.TextoSecundario
import kotlinx.coroutines.delay

private const val SALTO_MS = 5_000L
private const val CONTAGEM_MS = 3_000L

@Composable
fun PlayerScreen(
    onProxima: () -> Unit,
    vm: PlayerViewModel = viewModel(),
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val foco = remember { FocusRequester() }

    LaunchedEffect(estado.terminou) {
        if (estado.terminou) {
            delay(2_500)
            onProxima()
        }
    }

    val corGenero = estado.musica?.genero?.cor?.let { Color(it) } ?: Destaque

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(corGenero.copy(alpha = 0.45f), Fundo, Fundo)))
            // onKeyEvent precisa vir antes de focusable() para receber as teclas.
            .onKeyEvent { evento ->
                if (evento.type != KeyEventType.KeyDown) return@onKeyEvent false
                when (evento.key) {
                    Key.DirectionCenter, Key.Enter, Key.NumPadEnter, Key.Spacebar,
                    Key.MediaPlayPause, Key.MediaPlay, Key.MediaPause -> vm.alternarPausa()
                    Key.DirectionLeft, Key.MediaRewind -> vm.avancar(-SALTO_MS)
                    Key.DirectionRight, Key.MediaFastForward -> vm.avancar(SALTO_MS)
                    Key.DirectionDown, Key.MediaNext -> if (estado.proximaNaFila != null) onProxima()
                    Key.DirectionUp -> vm.alternarVoz()
                    else -> return@onKeyEvent false
                }
                true
            }
            .focusRequester(foco)
            .focusable()
            .padding(horizontal = 48.dp, vertical = 27.dp),
    ) {
        Cabecalho(estado)

        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val letra = estado.letra
            when {
                estado.carregando -> Text("Buscando a letra…", fontSize = 28.sp)
                letra != null -> AreaDaLetra(letra, estado.posicaoMs)
                estado.temAudio -> Text("♪ Tocando sem letra ♪", fontSize = 40.sp, color = Destaque)
                else -> SemLetra(estado.musica)
            }
        }

        Rodape(estado, Modifier.align(Alignment.BottomCenter))
    }

    LaunchedEffect(Unit) { runCatching { foco.requestFocus() } }
}

@Composable
private fun Cabecalho(estado: EstadoPlayer) {
    val musica = estado.musica ?: return
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f)) {
            Text(musica.titulo, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Text("${musica.artista} • ${musica.genero.nome}", fontSize = 18.sp, color = TextoSecundario)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("Nº ${musica.codigo}", fontSize = 22.sp, color = Destaque, fontWeight = FontWeight.Bold)
            if (!estado.temAudio && estado.letra != null) {
                Text("Modo demonstração (sem áudio)", fontSize = 14.sp, color = TextoSecundario)
            }
            if (estado.temAudio) {
                Text(
                    if (estado.semVoz) "🎤 Voz original: REMOVIDA" else "🗣 Voz original: LIGADA",
                    fontSize = 14.sp,
                    color = TextoSecundario,
                )
            }
        }
    }
}

@Composable
private fun AreaDaLetra(letra: Letra, posicaoMs: Long) {
    val indice = letra.indiceEm(posicaoMs)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        if (indice < 0) {
            val faltaMs = letra.linhas.first().inicioMs - posicaoMs
            val texto = if (faltaMs <= CONTAGEM_MS) "${faltaMs / 1000 + 1}" else "♪ Prepare-se… ♪"
            Text(texto, fontSize = 64.sp, color = Destaque, fontWeight = FontWeight.Bold)
            LinhaSecundaria(letra.linhas.first().texto, 34.sp)
        } else {
            letra.linhas.getOrNull(indice - 1)?.let { LinhaSecundaria(it.texto, 26.sp, alpha = 0.45f) }
            LinhaAtual(letra.linhas[indice].texto, letra.progressoDaLinha(indice, posicaoMs))
            letra.linhas.getOrNull(indice + 1)?.let { LinhaSecundaria(it.texto, 34.sp) }
            letra.linhas.getOrNull(indice + 2)?.let { LinhaSecundaria(it.texto, 26.sp, alpha = 0.45f) }
        }
    }
}

/** Linha em destaque: as letras vão sendo "pintadas" conforme o tempo passa. */
@Composable
private fun LinhaAtual(texto: String, progresso: Float) {
    val corte = (texto.length * progresso).toInt().coerceIn(0, texto.length)
    val anotado = buildAnnotatedString {
        withStyle(SpanStyle(color = Destaque)) { append(texto.substring(0, corte)) }
        withStyle(SpanStyle(color = Color.White)) { append(texto.substring(corte)) }
    }
    Text(
        text = anotado,
        fontSize = 52.sp,
        fontWeight = FontWeight.ExtraBold,
        textAlign = TextAlign.Center,
        lineHeight = 60.sp,
    )
}

@Composable
private fun LinhaSecundaria(texto: String, tamanho: TextUnit, alpha: Float = 0.75f) {
    Text(
        text = texto,
        fontSize = tamanho,
        color = Color.White.copy(alpha = alpha),
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun SemLetra(musica: Musica?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Letra sincronizada não encontrada", fontSize = 40.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Não achamos a letra no servidor nem no LRCLIB (verifique a internet).\n" +
                "Publique servidor/letras/${musica?.codigo ?: "<numero>"}.lrc no GitHub ou envie a letra\n" +
                "sincronizada para lrclib.net — depois é só tentar de novo.",
            fontSize = 20.sp,
            color = TextoSecundario,
            textAlign = TextAlign.Center,
            lineHeight = 30.sp,
        )
    }
}

@Composable
private fun Rodape(estado: EstadoPlayer, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth()) {
        val progresso = if (estado.duracaoMs > 0) {
            (estado.posicaoMs.toFloat() / estado.duracaoMs).coerceIn(0f, 1f)
        } else {
            0f
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(3.dp)),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(progresso)
                    .fillMaxHeight()
                    .background(Destaque, RoundedCornerShape(3.dp)),
            )
        }
        Row(Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Text(
                "${formatarTempo(estado.posicaoMs)} / ${formatarTempo(estado.duracaoMs)}" +
                    if (!estado.tocando && !estado.carregando) "   ⏸ PAUSADO" else "",
                fontSize = 16.sp,
                color = TextoSecundario,
            )
            Spacer(Modifier.weight(1f))
            Text(
                estado.proximaNaFila?.let { "Próxima: ${it.titulo} — ${it.artista}" } ?: "Fila vazia",
                fontSize = 16.sp,
                color = TextoSecundario,
            )
        }
        Text(
            "OK: pausar  •  ◀ ▶: ±5s  •  ▲: voz original liga/desliga  •  ▼: próxima da fila  •  Voltar: sair",
            fontSize = 14.sp,
            color = TextoSecundario.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

private fun formatarTempo(ms: Long): String {
    val totalSeg = (ms / 1000).coerceAtLeast(0)
    return "%d:%02d".format(totalSeg / 60, totalSeg % 60)
}
