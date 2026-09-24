package br.com.karaokebrasil.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class MusicaDao {

    @Query("SELECT * FROM musicas ORDER BY titulo COLLATE NOCASE")
    abstract fun todas(): Flow<List<Musica>>

    @Query("SELECT * FROM musicas WHERE genero_id = :generoId ORDER BY titulo COLLATE NOCASE")
    abstract fun porGenero(generoId: String): Flow<List<Musica>>

    @Query(
        """
        SELECT * FROM musicas
        WHERE termoBusca LIKE '%' || :termo || '%' OR CAST(codigo AS TEXT) = :termo
        ORDER BY titulo COLLATE NOCASE
        LIMIT 200
        """
    )
    abstract fun buscar(termo: String): Flow<List<Musica>>

    @Query("SELECT * FROM musicas WHERE favorita = 1 ORDER BY titulo COLLATE NOCASE")
    abstract fun favoritas(): Flow<List<Musica>>

    @Query("SELECT * FROM musicas WHERE vezesCantada > 0 ORDER BY vezesCantada DESC, titulo LIMIT 20")
    abstract fun maisCantadas(): Flow<List<Musica>>

    @Query("SELECT * FROM musicas WHERE codigo = :codigo")
    abstract suspend fun porCodigo(codigo: Int): Musica?

    @Query("UPDATE musicas SET favorita = NOT favorita WHERE codigo = :codigo")
    abstract suspend fun alternarFavorita(codigo: Int)

    @Query("UPDATE musicas SET vezesCantada = vezesCantada + 1 WHERE codigo = :codigo")
    abstract suspend fun registrarExecucao(codigo: Int)

    @Query("SELECT * FROM musicas")
    protected abstract suspend fun listarTodas(): List<Musica>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun salvarTodas(musicas: List<Musica>)

    @Query("DELETE FROM musicas")
    protected abstract suspend fun apagarTodas()

    /**
     * Deixa o banco igual ao catálogo: entra o que é novo, sai o que foi removido e
     * título/artista/gênero são atualizados, mantendo favoritas e contagens.
     */
    @Transaction
    open suspend fun sincronizar(catalogo: List<Musica>) {
        val atuais = listarTodas().associateBy { it.codigo }
        val mescladas = catalogo.map { nova ->
            val antiga = atuais[nova.codigo] ?: return@map nova
            nova.copy(favorita = antiga.favorita, vezesCantada = antiga.vezesCantada)
        }
        apagarTodas()
        salvarTodas(mescladas)
    }
}
