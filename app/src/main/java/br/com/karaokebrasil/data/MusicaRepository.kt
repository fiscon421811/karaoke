package br.com.karaokebrasil.data

import android.content.Context
import android.util.Log
import br.com.karaokebrasil.BuildConfig
import br.com.karaokebrasil.rede.Http
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

class MusicaRepository(
    private val context: Context,
    private val dao: MusicaDao,
) {
    fun todas(): Flow<List<Musica>> = dao.todas()
    fun porGenero(generoId: String): Flow<List<Musica>> = dao.porGenero(generoId)
    fun buscar(termo: String): Flow<List<Musica>> = dao.buscar(normalizar(termo))
    fun favoritas(): Flow<List<Musica>> = dao.favoritas()
    fun maisCantadas(): Flow<List<Musica>> = dao.maisCantadas()

    suspend fun porCodigo(codigo: Int): Musica? = dao.porCodigo(codigo)
    suspend fun alternarFavorita(codigo: Int) = dao.alternarFavorita(codigo)
    suspend fun registrarExecucao(codigo: Int) = dao.registrarExecucao(codigo)

    /**
     * Carrega o catálogo (gêneros + músicas) e sincroniza com o banco. Fontes, em ordem:
     * servidor (`catalogo.json`), última cópia baixada, cópia embutida no APK.
     * Assim, gêneros e músicas novos publicados no servidor aparecem sem recompilar.
     */
    suspend fun importarCatalogo() = withContext(Dispatchers.IO) {
        val cache = File(context.filesDir, ARQUIVO_CATALOGO)
        val candidatos = sequence {
            val baixado = BuildConfig.SERVIDOR_MIDIA_URL.takeIf { it.isNotBlank() }
                ?.let { Http.baixarTexto(it + ARQUIVO_CATALOGO) }
            if (baixado != null) yield(baixado to true)
            if (cache.isFile) yield(cache.readText() to false)
            yield(context.assets.open(ARQUIVO_CATALOGO).bufferedReader().use { it.readText() } to false)
        }
        for ((json, doServidor) in candidatos) {
            val musicas = runCatching { interpretarCatalogo(json) }
                .onFailure { Log.w(TAG, "catalogo.json inválido", it) }
                .getOrNull()
                ?.takeIf { it.isNotEmpty() }
                ?: continue
            if (doServidor) runCatching { cache.writeText(json) }
            dao.sincronizar(musicas)
            return@withContext
        }
    }

    private fun interpretarCatalogo(json: String): List<Musica> {
        val raiz = JSONObject(json)
        val generosJson = raiz.getJSONArray("generos")
        val generos = (0 until generosJson.length()).associate { i ->
            val obj = generosJson.getJSONObject(i)
            val genero = Genero(
                id = obj.getString("id"),
                nome = obj.getString("nome"),
                cor = lerCor(obj.optString("cor")),
                ordem = i,
            )
            genero.id to genero
        }
        val musicasJson = raiz.getJSONArray("musicas")
        return (0 until musicasJson.length()).mapNotNull { i ->
            val obj = musicasJson.getJSONObject(i)
            val genero = generos[obj.getString("genero")] ?: return@mapNotNull null
            Musica(
                codigo = obj.getInt("codigo"),
                titulo = obj.getString("titulo"),
                artista = obj.getString("artista"),
                genero = genero,
            )
        }
    }

    /** "#D4A017" -> 0xFFD4A017. */
    private fun lerCor(texto: String): Long =
        texto.removePrefix("#").toLongOrNull(16)
            ?.let { if (texto.length <= 7) it or 0xFF000000 else it }
            ?: COR_PADRAO

    private companion object {
        const val TAG = "MusicaRepository"
        const val ARQUIVO_CATALOGO = "catalogo.json"
        const val COR_PADRAO = 0xFF2E86AB
    }
}
