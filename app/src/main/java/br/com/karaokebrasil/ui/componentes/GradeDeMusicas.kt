package br.com.karaokebrasil.ui.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.karaokebrasil.data.Musica

@Composable
fun GradeDeMusicas(
    musicas: List<Musica>,
    comLetra: Set<Int>,
    onSelecionar: (Musica) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(LarguraCard),
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 16.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        items(musicas, key = { it.codigo }) { musica ->
            MusicaCard(
                musica = musica,
                temLetra = musica.codigo in comLetra,
                onClick = { onSelecionar(musica) },
            )
        }
    }
}
