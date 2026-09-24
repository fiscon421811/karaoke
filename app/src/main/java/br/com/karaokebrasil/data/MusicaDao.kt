package br.com.karaokebrasil.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicaDao {

    /** IGNORE preserva favoritas e contagens ao reimportar o catálogo. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun inserirTodas(musicas: List<Musica>)

    @Query("SELECT * FROM musicas ORDER BY titulo COLLATE NOCASE")
    fun todas(): Flow<List<Musica>>

    @Query("SELECT * FROM musicas WHERE genero = :genero ORDER BY titulo COLLATE NOCASE")
    fun porGenero(genero: Genero): Flow<List<Musica>>

    @Query(
        """
        SELECT * FROM musicas
        WHERE termoBusca LIKE '%' || :termo || '%' OR CAST(codigo AS TEXT) = :termo
        ORDER BY titulo COLLATE NOCASE
        LIMIT 200
        """
    )
    fun buscar(termo: String): Flow<List<Musica>>

    @Query("SELECT * FROM musicas WHERE favorita = 1 ORDER BY titulo COLLATE NOCASE")
    fun favoritas(): Flow<List<Musica>>

    @Query("SELECT * FROM musicas WHERE vezesCantada > 0 ORDER BY vezesCantada DESC, titulo LIMIT 20")
    fun maisCantadas(): Flow<List<Musica>>

    @Query("SELECT * FROM musicas WHERE codigo = :codigo")
    suspend fun porCodigo(codigo: Int): Musica?

    @Query("UPDATE musicas SET favorita = NOT favorita WHERE codigo = :codigo")
    suspend fun alternarFavorita(codigo: Int)

    @Query("UPDATE musicas SET vezesCantada = vezesCantada + 1 WHERE codigo = :codigo")
    suspend fun registrarExecucao(codigo: Int)
}
