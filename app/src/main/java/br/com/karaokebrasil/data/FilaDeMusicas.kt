package br.com.karaokebrasil.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Fila de músicas da festa, compartilhada entre as telas. */
class FilaDeMusicas {
    private val _musicas = MutableStateFlow<List<Musica>>(emptyList())
    val musicas: StateFlow<List<Musica>> = _musicas.asStateFlow()

    fun adicionar(musica: Musica) = _musicas.update { it + musica }

    fun removerEm(indice: Int) = _musicas.update { lista ->
        lista.filterIndexed { i, _ -> i != indice }
    }

    fun limpar() = _musicas.update { emptyList() }

    /** Remove e devolve a primeira música da fila, ou null se estiver vazia. */
    fun proxima(): Musica? {
        var primeira: Musica? = null
        _musicas.update { lista ->
            primeira = lista.firstOrNull()
            lista.drop(1)
        }
        return primeira
    }
}
