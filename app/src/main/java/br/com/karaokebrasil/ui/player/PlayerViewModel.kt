package br.com.karaokebrasil.ui.player

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import br.com.karaokebrasil.KaraokeApp
import br.com.karaokebrasil.data.Musica
import br.com.karaokebrasil.letra.Letra
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class EstadoPlayer(
    val carregando: Boolean = true,
    val musica: Musica? = null,
    val letra: Letra? = null,
    val temAudio: Boolean = false,
    val tocando: Boolean = false,
    val posicaoMs: Long = 0,
    val duracaoMs: Long = 0,
    val terminou: Boolean = false,
    val proximaNaFila: Musica? = null,
)

/**
 * Controla a reprodução de uma música. Com áudio, o tempo vem do ExoPlayer;
 * sem áudio (modo demonstração), um relógio interno avança a letra.
 */
class PlayerViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle,
) : AndroidViewModel(application) {

    private val app = application as KaraokeApp
    private val codigo: Int = checkNotNull(savedStateHandle.get<Int>("codigo"))

    private val _estado = MutableStateFlow(EstadoPlayer())
    val estado: StateFlow<EstadoPlayer> = _estado.asStateFlow()

    private var player: ExoPlayer? = null

    // Relógio do modo demonstração.
    private var baseMs = 0L
    private var inicioRealMs = 0L
    private var relogioRodando = false

    private var atualizador: Job? = null

    init {
        viewModelScope.launch {
            val musica = app.repositorio.porCodigo(codigo)
            val (letra, uriAudio) = withContext(Dispatchers.IO) {
                app.fonteDeMidia.carregarLetra(codigo) to app.fonteDeMidia.uriDoAudio(codigo)
            }
            if (uriAudio != null) {
                player = ExoPlayer.Builder(app).build().apply {
                    setMediaItem(MediaItem.fromUri(uriAudio))
                    prepare()
                }
            }
            _estado.value = EstadoPlayer(
                carregando = false,
                musica = musica,
                letra = letra,
                temAudio = uriAudio != null,
                duracaoMs = letra?.fimMs ?: 0L,
            )
            app.repositorio.registrarExecucao(codigo)
            if (letra != null || uriAudio != null) tocar()
            iniciarAtualizacao()
        }
        viewModelScope.launch {
            app.fila.musicas.collect { fila ->
                _estado.update { it.copy(proximaNaFila = fila.firstOrNull()) }
            }
        }
    }

    fun alternarPausa() {
        if (_estado.value.tocando) pausar() else tocar()
    }

    fun avancar(deltaMs: Long) {
        val destino = (posicaoAtual() + deltaMs).coerceIn(0L, _estado.value.duracaoMs.coerceAtLeast(0L))
        player?.seekTo(destino) ?: run {
            baseMs = destino
            inicioRealMs = SystemClock.elapsedRealtime()
        }
        _estado.update { it.copy(terminou = false) }
    }

    private fun tocar() {
        if (_estado.value.terminou) avancar(-posicaoAtual())
        player?.play() ?: run {
            inicioRealMs = SystemClock.elapsedRealtime()
            relogioRodando = true
        }
        _estado.update { it.copy(tocando = true) }
    }

    private fun pausar() {
        player?.pause() ?: run {
            baseMs = posicaoAtual()
            relogioRodando = false
        }
        _estado.update { it.copy(tocando = false) }
    }

    private fun posicaoAtual(): Long = player?.currentPosition
        ?: if (relogioRodando) baseMs + (SystemClock.elapsedRealtime() - inicioRealMs) else baseMs

    private fun iniciarAtualizacao() {
        atualizador?.cancel()
        atualizador = viewModelScope.launch {
            while (isActive) {
                val atual = _estado.value
                val duracaoAudio = player?.duration?.takeIf { it > 0 }
                val duracao = duracaoAudio ?: atual.duracaoMs
                val posicao = posicaoAtual().coerceAtMost(duracao.coerceAtLeast(0L))
                val terminou = when (val p = player) {
                    null -> duracao > 0 && posicao >= duracao
                    else -> p.playbackState == Player.STATE_ENDED
                }
                if (terminou && atual.tocando) pausar()
                _estado.update {
                    it.copy(posicaoMs = posicao, duracaoMs = duracao, terminou = terminou)
                }
                delay(INTERVALO_ATUALIZACAO_MS)
            }
        }
    }

    override fun onCleared() {
        player?.release()
        player = null
    }

    private companion object {
        const val INTERVALO_ATUALIZACAO_MS = 40L
    }
}
