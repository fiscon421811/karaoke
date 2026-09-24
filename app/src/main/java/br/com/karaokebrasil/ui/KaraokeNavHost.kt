package br.com.karaokebrasil.ui

import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.com.karaokebrasil.data.Genero
import br.com.karaokebrasil.data.Musica
import br.com.karaokebrasil.ui.busca.BuscaScreen
import br.com.karaokebrasil.ui.componentes.OpcoesMusicaDialog
import br.com.karaokebrasil.ui.componentes.abrirKaraokeNoYoutube
import br.com.karaokebrasil.ui.fila.FilaScreen
import br.com.karaokebrasil.ui.genero.FavoritasScreen
import br.com.karaokebrasil.ui.genero.GeneroScreen
import br.com.karaokebrasil.ui.home.HomeScreen
import br.com.karaokebrasil.ui.player.PlayerScreen

private object Rotas {
    const val INICIO = "inicio"
    const val GENERO = "genero/{genero}"
    const val BUSCA = "busca"
    const val FILA = "fila"
    const val FAVORITAS = "favoritas"
    const val PLAYER = "player/{codigo}"

    fun genero(genero: Genero) = "genero/${Uri.encode(genero.id)}"
    fun player(codigo: Int) = "player/$codigo"
}

@Composable
fun KaraokeNavHost() {
    val nav = rememberNavController()
    val context = LocalContext.current
    // Um único ViewModel de catálogo, compartilhado por todas as telas.
    val vm: KaraokeViewModel = viewModel()
    val comLetra by vm.comLetra.collectAsState()
    var selecionada by remember { mutableStateOf<Musica?>(null) }
    val selecionar: (Musica) -> Unit = { selecionada = it }

    NavHost(navController = nav, startDestination = Rotas.INICIO) {
        composable(Rotas.INICIO) {
            // Letras baixadas no player passam a exibir o selo "♪ LETRA".
            LaunchedEffect(Unit) { vm.atualizarLetrasDisponiveis() }
            HomeScreen(
                vm = vm,
                onSelecionar = selecionar,
                onAbrirGenero = { nav.navigate(Rotas.genero(it)) },
                onBuscar = { nav.navigate(Rotas.BUSCA) },
                onFila = { nav.navigate(Rotas.FILA) },
                onFavoritas = { nav.navigate(Rotas.FAVORITAS) },
            )
        }
        composable(
            Rotas.GENERO,
            arguments = listOf(navArgument("genero") { type = NavType.StringType }),
        ) { entrada ->
            val generoId = checkNotNull(entrada.arguments?.getString("genero"))
            GeneroScreen(generoId = generoId, vm = vm, onSelecionar = selecionar)
        }
        composable(Rotas.BUSCA) {
            BuscaScreen(vm = vm, onSelecionar = selecionar)
        }
        composable(Rotas.FAVORITAS) {
            FavoritasScreen(vm = vm, onSelecionar = selecionar)
        }
        composable(Rotas.FILA) {
            FilaScreen(vm = vm, onCantar = { nav.cantar(it) })
        }
        composable(
            Rotas.PLAYER,
            arguments = listOf(navArgument("codigo") { type = NavType.IntType }),
        ) {
            PlayerScreen(
                onProxima = {
                    val proxima = vm.proximaDaFila()
                    if (proxima != null) nav.cantar(proxima) else nav.popBackStack()
                },
            )
        }
    }

    selecionada?.let { musica ->
        OpcoesMusicaDialog(
            musica = musica,
            temLetra = musica.codigo in comLetra,
            onCantar = {
                selecionada = null
                nav.cantar(musica)
            },
            onCantarNoYoutube = {
                selecionada = null
                vm.registrarExecucao(musica)
                if (!abrirKaraokeNoYoutube(context, musica)) {
                    Toast.makeText(context, "Instale o app do YouTube para cantar esta música.", Toast.LENGTH_LONG).show()
                }
            },
            onAdicionarNaFila = {
                vm.adicionarNaFila(musica)
                selecionada = null
            },
            onAlternarFavorita = {
                vm.alternarFavorita(musica)
                selecionada = null
            },
            onFechar = { selecionada = null },
        )
    }
}

/** Abre o player substituindo um player anterior, para "Voltar" sempre retornar ao catálogo. */
private fun NavHostController.cantar(musica: Musica) {
    navigate(Rotas.player(musica.codigo)) {
        popUpTo(Rotas.PLAYER) { inclusive = true }
    }
}
