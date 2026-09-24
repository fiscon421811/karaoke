package br.com.karaokebrasil.ui.componentes

import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import br.com.karaokebrasil.data.Musica

/**
 * Abre a busca "<título> <artista> karaokê" no YouTube. Tenta o app de TV, depois o de
 * celular e, por fim, o navegador. Devolve false se nenhum estiver instalado.
 */
fun abrirKaraokeNoYoutube(context: Context, musica: Musica): Boolean {
    val busca = "${musica.titulo} ${musica.artista} karaokê"
    val tentativas = listOf(
        Intent(Intent.ACTION_SEARCH)
            .setPackage("com.google.android.youtube.tv")
            .putExtra(SearchManager.QUERY, busca),
        Intent(Intent.ACTION_SEARCH)
            .setPackage("com.google.android.youtube")
            .putExtra(SearchManager.QUERY, busca),
        Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=${Uri.encode(busca)}")),
    )
    return tentativas.any { intent ->
        try {
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }
}
