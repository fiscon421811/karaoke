package br.com.karaokebrasil.rede

import android.util.Log
import java.net.HttpURLConnection
import java.net.URL

object Http {
    private const val TAG = "Http"
    private const val TIMEOUT_MS = 8_000
    private const val USER_AGENT = "KaraokeBrasil/1.0 (https://github.com/fiscon421811/karaoke)"

    /** Baixa um texto UTF-8; devolve null em erro de rede ou resposta diferente de 200. */
    fun baixarTexto(url: String): String? = runCatching {
        val conexao = URL(url).openConnection() as HttpURLConnection
        try {
            conexao.connectTimeout = TIMEOUT_MS
            conexao.readTimeout = TIMEOUT_MS
            conexao.setRequestProperty("User-Agent", USER_AGENT)
            if (conexao.responseCode != HttpURLConnection.HTTP_OK) return@runCatching null
            conexao.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } finally {
            conexao.disconnect()
        }
    }.onFailure { Log.w(TAG, "Falha ao baixar $url", it) }.getOrNull()
}
