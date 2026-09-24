package br.com.karaokebrasil.letra

import android.content.Context
import android.net.Uri
import java.io.File

/**
 * Localiza letra (.lrc) e áudio de cada música pelo código. Procura primeiro na pasta
 * externa do app (onde o usuário pode copiar arquivos via `adb push`) e depois em assets:
 *
 * - `Android/data/br.com.karaokebrasil/files/karaoke/<codigo>.lrc` e `<codigo>.mp3`
 * - `assets/letras/<codigo>.lrc` e `assets/audio/<codigo>.mp3`
 */
class FonteDeMidia(private val context: Context) {

    val pastaExterna: File?
        get() = context.getExternalFilesDir(PASTA_EXTERNA)

    fun carregarLetra(codigo: Int): Letra? {
        val nome = "$codigo.lrc"
        val texto = File(pastaExterna, nome).takeIf { it.isFile }?.readText()
            ?: runCatching {
                context.assets.open("$PASTA_LETRAS/$nome").bufferedReader().use { it.readText() }
            }.getOrNull()
        return texto?.let(LrcParser::parse)?.takeIf { it.linhas.isNotEmpty() }
    }

    fun uriDoAudio(codigo: Int): Uri? {
        val externa = pastaExterna
        val assetsAudio = context.assets.list(PASTA_AUDIO).orEmpty().toSet()
        for (ext in EXTENSOES_AUDIO) {
            val nome = "$codigo.$ext"
            if (externa != null && File(externa, nome).isFile) return Uri.fromFile(File(externa, nome))
            if (nome in assetsAudio) return Uri.parse("asset:///$PASTA_AUDIO/$nome")
        }
        return null
    }

    /** Códigos de todas as músicas que têm letra disponível. */
    fun codigosComLetra(): Set<Int> {
        val nosAssets = context.assets.list(PASTA_LETRAS).orEmpty().asSequence()
        val naPasta = pastaExterna?.list().orEmpty().asSequence()
        return (nosAssets + naPasta)
            .filter { it.endsWith(".lrc") }
            .mapNotNull { it.removeSuffix(".lrc").toIntOrNull() }
            .toSet()
    }

    private companion object {
        const val PASTA_EXTERNA = "karaoke"
        const val PASTA_LETRAS = "letras"
        const val PASTA_AUDIO = "audio"
        val EXTENSOES_AUDIO = listOf("mp3", "m4a", "ogg", "wav")
    }
}
