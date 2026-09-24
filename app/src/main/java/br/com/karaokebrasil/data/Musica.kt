package br.com.karaokebrasil.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.text.Normalizer

@Entity(
    tableName = "musicas",
    indices = [Index("genero_id"), Index("termoBusca")],
)
data class Musica(
    @PrimaryKey val codigo: Int,
    val titulo: String,
    val artista: String,
    @Embedded(prefix = "genero_") val genero: Genero,
    /** Título + artista sem acentos e em minúsculas, usado na busca. */
    val termoBusca: String = normalizar("$titulo $artista"),
    val favorita: Boolean = false,
    val vezesCantada: Int = 0,
)

private val MARCAS_DIACRITICAS = Regex("\\p{Mn}+")

/** "Forró" -> "forro": permite buscar sem acentos usando o controle remoto. */
fun normalizar(texto: String): String =
    Normalizer.normalize(texto, Normalizer.Form.NFD)
        .replace(MARCAS_DIACRITICAS, "")
        .lowercase()
        .trim()
