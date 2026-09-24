package br.com.karaokebrasil.ui.busca

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Text
import br.com.karaokebrasil.data.Musica
import br.com.karaokebrasil.ui.KaraokeViewModel
import br.com.karaokebrasil.ui.componentes.GradeDeMusicas
import br.com.karaokebrasil.ui.componentes.Tela
import br.com.karaokebrasil.ui.theme.Destaque
import br.com.karaokebrasil.ui.theme.TextoSecundario

@Composable
fun BuscaScreen(
    vm: KaraokeViewModel,
    onSelecionar: (Musica) -> Unit,
) {
    var termo by rememberSaveable { mutableStateOf(vm.termoBusca.value) }
    val resultados by vm.resultadosBusca.collectAsStateWithLifecycle()
    val comLetra by vm.comLetra.collectAsStateWithLifecycle()
    val foco = remember { FocusRequester() }
    var focado by remember { mutableStateOf(false) }

    Tela(titulo = "Buscar", subtitulo = "Digite título, artista ou o número da música") {
        Box(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
                .background(Color(0xFF1B1B33), RoundedCornerShape(12.dp))
                .border(2.dp, if (focado) Destaque else Color.Transparent, RoundedCornerShape(12.dp))
                .padding(horizontal = 20.dp, vertical = 14.dp),
        ) {
            if (termo.isEmpty()) {
                Text("Ex.: evidências, Legião Urbana, 1001…", color = TextoSecundario, fontSize = 22.sp)
            }
            BasicTextField(
                value = termo,
                onValueChange = {
                    termo = it
                    vm.termoBusca.value = it
                },
                singleLine = true,
                textStyle = TextStyle(color = Color.White, fontSize = 22.sp),
                cursorBrush = SolidColor(Destaque),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(foco)
                    .onFocusChanged { focado = it.isFocused },
            )
        }
        if (termo.isNotBlank() && resultados.isEmpty()) {
            Text(
                "Nenhuma música encontrada para \"$termo\".",
                color = TextoSecundario,
                fontSize = 18.sp,
                modifier = Modifier.padding(top = 24.dp),
            )
        }
        GradeDeMusicas(musicas = resultados, comLetra = comLetra, onSelecionar = onSelecionar)
    }

    LaunchedEffect(Unit) { runCatching { foco.requestFocus() } }
}
