package br.com.karaokebrasil.ui.player

import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.util.UnstableApi
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Efeito "karaokê" clássico: a voz costuma ser mixada no centro (igual nos dois canais),
 * então subtrair um canal do outro (L − R) a remove, mantendo boa parte dos instrumentos.
 * Funciona em áudio estéreo 16 bits; em outros formatos o áudio passa sem alteração.
 */
@UnstableApi
class RemovedorDeVoz : BaseAudioProcessor() {

    /** Pode ser alterado durante a reprodução. */
    @Volatile
    var ativo: Boolean = false

    override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat =
        if (inputAudioFormat.encoding == C.ENCODING_PCM_16BIT && inputAudioFormat.channelCount == 2) {
            inputAudioFormat
        } else {
            AudioProcessor.AudioFormat.NOT_SET
        }

    override fun queueInput(inputBuffer: ByteBuffer) {
        inputBuffer.order(ByteOrder.nativeOrder())
        val saida = replaceOutputBuffer(inputBuffer.remaining())
        val removerVoz = ativo
        while (inputBuffer.remaining() >= BYTES_POR_QUADRO) {
            val esquerdo = inputBuffer.short.toInt()
            val direito = inputBuffer.short.toInt()
            if (removerVoz) {
                val semVoz = ((esquerdo - direito) / 2).toShort()
                saida.putShort(semVoz).putShort(semVoz)
            } else {
                saida.putShort(esquerdo.toShort()).putShort(direito.toShort())
            }
        }
        inputBuffer.position(inputBuffer.limit())
        saida.flip()
    }

    private companion object {
        const val BYTES_POR_QUADRO = 4 // 2 canais × 16 bits
    }
}
