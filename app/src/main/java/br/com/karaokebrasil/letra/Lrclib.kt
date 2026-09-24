package br.com.karaokebrasil.letra

import br.com.karaokebrasil.data.normalizar
import br.com.karaokebrasil.rede.Http
import org.json.JSONArray
import java.net.URLEncoder

/**
 * Cliente do LRCLIB (https://lrclib.net), base pública e gratuita de letras
 * sincronizadas no formato LRC, mantida pela comunidade.
 */
object Lrclib {

    private const val API = "https://lrclib.net/api/search"

    /** Devolve o conteúdo LRC sincronizado da música, ou null se não encontrar. */
    fun buscarLetraSincronizada(titulo: String, artista: String): String? {
        val porCampos = "$API?track_name=${codificar(titulo)}&artist_name=${codificar(artista)}"
        val porTexto = "$API?q=${codificar("$titulo $artista")}"
        return sequenceOf(porCampos, porTexto)
            .mapNotNull { url -> Http.baixarTexto(url)?.let { escolher(it, titulo) } }
            .firstOrNull()
    }

    /** Entre os resultados com letra sincronizada, prefere o de título idêntico. */
    private fun escolher(json: String, titulo: String): String? {
        val resultados = runCatching { JSONArray(json) }.getOrNull() ?: return null
        val sincronizadas = (0 until resultados.length())
            .map { resultados.getJSONObject(it) }
            .filter { !it.isNull("syncedLyrics") && it.optString("syncedLyrics").isNotBlank() }
        val alvo = normalizar(titulo)
        val melhor = sincronizadas.firstOrNull { normalizar(it.optString("trackName")) == alvo }
            ?: sincronizadas.firstOrNull()
        return melhor?.getString("syncedLyrics")
    }

    private fun codificar(texto: String) = URLEncoder.encode(texto, "UTF-8")
}
