package br.com.karaokebrasil.letra

import android.content.Context
import android.net.Uri
import android.util.Log
import br.com.karaokebrasil.BuildConfig
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Localiza letra (.lrc) e áudio de cada música pelo código, nesta ordem:
 *
 * 1. Pasta externa do app (arquivos copiados via `adb push`):
 *    `Android/data/br.com.karaokebrasil/files/karaoke/<codigo>.lrc` e `<codigo>.mp3`
 * 2. Assets do APK: `assets/letras/<codigo>.lrc` e `assets/audio/<codigo>.mp3`
 * 3. Servidor ([BuildConfig.SERVIDOR_MIDIA_URL]), listado em `midias.json`.
 *
 * Todas as funções fazem E/S (disco/rede): chame fora da thread principal.
 */
class FonteDeMidia(private val context: Context) {

    private data class MidiaRemota(val letra: String?, val audio: String?)

    private val servidor = BuildConfig.SERVIDOR_MIDIA_URL

    @Volatile
    private var indiceRemoto: Map<Int, MidiaRemota>? = null

    val pastaExterna: File?
        get() = context.getExternalFilesDir(PASTA_EXTERNA)

    fun carregarLetra(codigo: Int): Letra? {
        val nome = "$codigo.lrc"
        val texto = File(pastaExterna, nome).takeIf { it.isFile }?.readText()
            ?: runCatching {
                context.assets.open("$PASTA_LETRAS/$nome").bufferedReader().use { it.readText() }
            }.getOrNull()
            ?: baixarLetra(codigo)
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
        // O ExoPlayer toca direto do servidor (streaming), sem baixar o arquivo inteiro.
        return indice()[codigo]?.audio?.let { Uri.parse(resolver(it)) }
    }

    /** Códigos de todas as músicas que têm letra disponível (local ou no servidor). */
    fun codigosComLetra(): Set<Int> {
        val nosAssets = context.assets.list(PASTA_LETRAS).orEmpty().asSequence()
        val naPasta = pastaExterna?.list().orEmpty().asSequence()
        val locais = (nosAssets + naPasta)
            .filter { it.endsWith(".lrc") }
            .mapNotNull { it.removeSuffix(".lrc").toIntOrNull() }
        val remotos = indice().filterValues { it.letra != null }.keys
        return (locais + remotos).toSet()
    }

    private fun baixarLetra(codigo: Int): String? {
        val caminho = indice()[codigo]?.letra ?: return null
        val cache = File(context.cacheDir, "$PASTA_LETRAS/$codigo.lrc")
        return baixarTexto(resolver(caminho))
            ?.also { texto -> runCatching { cache.parentFile?.mkdirs(); cache.writeText(texto) } }
            ?: cache.takeIf { it.isFile }?.readText()
    }

    /**
     * Lê o `midias.json` do servidor (uma vez por execução). Sem internet, usa a última
     * cópia salva; se nunca baixou, tenta de novo na próxima chamada.
     */
    private fun indice(): Map<Int, MidiaRemota> {
        indiceRemoto?.let { return it }
        if (servidor.isBlank()) return emptyMap()

        val cache = File(context.filesDir, ARQUIVO_INDICE)
        val baixado = baixarTexto(resolver(ARQUIVO_INDICE))
        if (baixado != null) runCatching { cache.writeText(baixado) }
        val json = baixado ?: cache.takeIf { it.isFile }?.readText() ?: return emptyMap()

        val mapa = runCatching { interpretarIndice(json) }
            .onFailure { Log.w(TAG, "midias.json inválido", it) }
            .getOrDefault(emptyMap())
        if (baixado != null) indiceRemoto = mapa
        return mapa
    }

    private fun interpretarIndice(json: String): Map<Int, MidiaRemota> {
        val musicas = JSONObject(json).getJSONObject("musicas")
        return musicas.keys().asSequence().mapNotNull { chave ->
            val codigo = chave.toIntOrNull() ?: return@mapNotNull null
            val obj = musicas.getJSONObject(chave)
            codigo to MidiaRemota(
                letra = obj.optString("letra").ifBlank { null },
                audio = obj.optString("audio").ifBlank { null },
            )
        }.toMap()
    }

    /** Caminhos relativos são resolvidos a partir do servidor; URLs completas são mantidas. */
    private fun resolver(caminho: String): String =
        if (caminho.startsWith("http://") || caminho.startsWith("https://")) caminho else servidor + caminho

    private fun baixarTexto(url: String): String? = runCatching {
        val conexao = URL(url).openConnection() as HttpURLConnection
        try {
            conexao.connectTimeout = TIMEOUT_MS
            conexao.readTimeout = TIMEOUT_MS
            if (conexao.responseCode != HttpURLConnection.HTTP_OK) return@runCatching null
            conexao.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } finally {
            conexao.disconnect()
        }
    }.onFailure { Log.w(TAG, "Falha ao baixar $url", it) }.getOrNull()

    private companion object {
        const val TAG = "FonteDeMidia"
        const val PASTA_EXTERNA = "karaoke"
        const val PASTA_LETRAS = "letras"
        const val PASTA_AUDIO = "audio"
        const val ARQUIVO_INDICE = "midias.json"
        const val TIMEOUT_MS = 8_000
        val EXTENSOES_AUDIO = listOf("mp3", "m4a", "ogg", "wav")
    }
}
