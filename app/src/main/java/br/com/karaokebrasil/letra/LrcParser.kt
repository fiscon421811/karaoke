package br.com.karaokebrasil.letra

/**
 * Leitor do formato LRC (letras sincronizadas), por exemplo:
 *
 * ```
 * [ti:Ciranda, Cirandinha]
 * [00:04.00]Ciranda, cirandinha
 * [00:08.50][01:10.00]Vamos todos cirandar
 * ```
 */
object LrcParser {

    private val TAG_TEMPO = Regex("""\[(\d{1,3}):(\d{1,2})(?:[.:](\d{1,3}))?]""")
    private val TAG_META = Regex("""^\[([a-zA-Z]+):(.*)]$""")

    fun parse(conteudo: String): Letra {
        val linhas = mutableListOf<LinhaLetra>()
        var titulo: String? = null
        var artista: String? = null
        var offsetMs = 0L

        conteudo.lineSequence()
            .map { it.trim().removePrefix("﻿") }
            .filter { it.isNotEmpty() }
            .forEach { linha ->
                val tempos = TAG_TEMPO.findAll(linha).toList()
                if (tempos.isEmpty()) {
                    TAG_META.matchEntire(linha)?.let { meta ->
                        val valor = meta.groupValues[2].trim()
                        when (meta.groupValues[1].lowercase()) {
                            "ti" -> titulo = valor
                            "ar" -> artista = valor
                            "offset" -> offsetMs = valor.toLongOrNull() ?: 0L
                        }
                    }
                    return@forEach
                }
                // O texto é o que vem depois da última tag de tempo.
                val texto = linha.substring(tempos.last().range.last + 1).trim()
                tempos.forEach { tempo ->
                    linhas += LinhaLetra(paraMs(tempo), texto)
                }
            }

        // Offset positivo no LRC adianta a letra.
        val ajustadas = linhas
            .map { it.copy(inicioMs = (it.inicioMs - offsetMs).coerceAtLeast(0)) }
            .sortedBy { it.inicioMs }
        return Letra(ajustadas, titulo, artista)
    }

    private fun paraMs(tempo: MatchResult): Long {
        val (min, seg, fracao) = tempo.destructured
        val fracaoMs = when (fracao.length) {
            0 -> 0L
            1 -> fracao.toLong() * 100
            2 -> fracao.toLong() * 10
            else -> fracao.toLong()
        }
        return min.toLong() * 60_000 + seg.toLong() * 1_000 + fracaoMs
    }
}
