package br.com.karaokebrasil.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray

class MusicaRepository(
    private val context: Context,
    private val dao: MusicaDao,
) {
    fun todas(): Flow<List<Musica>> = dao.todas()
    fun porGenero(genero: Genero): Flow<List<Musica>> = dao.porGenero(genero)
    fun buscar(termo: String): Flow<List<Musica>> = dao.buscar(normalizar(termo))
    fun favoritas(): Flow<List<Musica>> = dao.favoritas()
    fun maisCantadas(): Flow<List<Musica>> = dao.maisCantadas()

    suspend fun porCodigo(codigo: Int): Musica? = dao.porCodigo(codigo)
    suspend fun alternarFavorita(codigo: Int) = dao.alternarFavorita(codigo)
    suspend fun registrarExecucao(codigo: Int) = dao.registrarExecucao(codigo)

    /**
     * Importa `assets/catalogo.json` para o banco. Roda a cada abertura do app:
     * músicas novas adicionadas ao JSON entram, as existentes ficam intactas.
     */
    suspend fun importarCatalogo() = withContext(Dispatchers.IO) {
        val json = context.assets.open(ARQUIVO_CATALOGO).bufferedReader().use { it.readText() }
        val array = JSONArray(json)
        val musicas = (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            Musica(
                codigo = obj.getInt("codigo"),
                titulo = obj.getString("titulo"),
                artista = obj.getString("artista"),
                genero = Genero.valueOf(obj.getString("genero")),
            )
        }
        dao.inserirTodas(musicas)
    }

    private companion object {
        const val ARQUIVO_CATALOGO = "catalogo.json"
    }
}
