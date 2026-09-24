package br.com.karaokebrasil.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import br.com.karaokebrasil.KaraokeApp
import br.com.karaokebrasil.data.Genero
import br.com.karaokebrasil.data.Musica
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Estado do catálogo compartilhado pelas telas de navegação (início, gênero, busca, fila). */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class KaraokeViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as KaraokeApp
    private val repositorio = app.repositorio
    private val fila = app.fila

    val musicasPorGenero: StateFlow<Map<Genero, List<Musica>>> = repositorio.todas()
        .map { musicas -> musicas.groupBy { it.genero }.toSortedMap() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val maisCantadas: StateFlow<List<Musica>> = repositorio.maisCantadas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val favoritas: StateFlow<List<Musica>> = repositorio.favoritas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val filaDeMusicas: StateFlow<List<Musica>> = fila.musicas

    private val _comLetra = MutableStateFlow<Set<Int>>(emptySet())
    val comLetra: StateFlow<Set<Int>> = _comLetra

    val termoBusca = MutableStateFlow("")
    val resultadosBusca: StateFlow<List<Musica>> = termoBusca
        .debounce(250)
        .flatMapLatest { termo ->
            if (termo.isBlank()) flowOf(emptyList()) else repositorio.buscar(termo)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        atualizarLetrasDisponiveis()
    }

    fun atualizarLetrasDisponiveis() {
        viewModelScope.launch(Dispatchers.IO) {
            _comLetra.value = app.fonteDeMidia.codigosComLetra()
        }
    }

    fun musicasDoGenero(genero: Genero): Flow<List<Musica>> = repositorio.porGenero(genero)

    fun alternarFavorita(musica: Musica) {
        viewModelScope.launch { repositorio.alternarFavorita(musica.codigo) }
    }

    fun registrarExecucao(musica: Musica) {
        viewModelScope.launch { repositorio.registrarExecucao(musica.codigo) }
    }

    fun adicionarNaFila(musica: Musica) = fila.adicionar(musica)
    fun removerDaFila(indice: Int) = fila.removerEm(indice)
    fun limparFila() = fila.limpar()
    fun proximaDaFila(): Musica? = fila.proxima()
}
