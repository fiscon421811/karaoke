package br.com.karaokebrasil

import android.app.Application
import br.com.karaokebrasil.data.FilaDeMusicas
import br.com.karaokebrasil.data.KaraokeDatabase
import br.com.karaokebrasil.data.MusicaRepository
import br.com.karaokebrasil.letra.FonteDeMidia
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Ponto único de criação das dependências (injeção manual, sem framework). */
class KaraokeApp : Application() {

    val escopo = CoroutineScope(SupervisorJob())

    val repositorio: MusicaRepository by lazy {
        MusicaRepository(this, KaraokeDatabase.criar(this).musicaDao())
    }
    val fonteDeMidia: FonteDeMidia by lazy { FonteDeMidia(this) }
    val fila = FilaDeMusicas()

    override fun onCreate() {
        super.onCreate()
        escopo.launch { repositorio.importarCatalogo() }
    }
}
