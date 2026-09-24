package br.com.karaokebrasil.ui.genero

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.karaokebrasil.data.Genero
import br.com.karaokebrasil.data.Musica
import br.com.karaokebrasil.ui.KaraokeViewModel
import br.com.karaokebrasil.ui.componentes.GradeDeMusicas
import br.com.karaokebrasil.ui.componentes.Tela

@Composable
fun GeneroScreen(
    genero: Genero,
    vm: KaraokeViewModel,
    onSelecionar: (Musica) -> Unit,
) {
    val fluxo = remember(genero) { vm.musicasDoGenero(genero) }
    val musicas by fluxo.collectAsStateWithLifecycle(initialValue = emptyList())
    val comLetra by vm.comLetra.collectAsStateWithLifecycle()

    Tela(titulo = genero.nome, subtitulo = "${musicas.size} músicas") {
        GradeDeMusicas(musicas = musicas, comLetra = comLetra, onSelecionar = onSelecionar)
    }
}

@Composable
fun FavoritasScreen(
    vm: KaraokeViewModel,
    onSelecionar: (Musica) -> Unit,
) {
    val musicas by vm.favoritas.collectAsStateWithLifecycle()
    val comLetra by vm.comLetra.collectAsStateWithLifecycle()

    Tela(
        titulo = "Favoritas",
        subtitulo = if (musicas.isEmpty()) {
            "Nenhuma favorita ainda. Selecione uma música e escolha \"Adicionar às favoritas\"."
        } else {
            "${musicas.size} músicas"
        },
    ) {
        GradeDeMusicas(musicas = musicas, comLetra = comLetra, onSelecionar = onSelecionar)
    }
}
