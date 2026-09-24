package br.com.karaokebrasil.letra

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LrcParserTest {

    @Test
    fun `le metadados e linhas em ordem`() {
        val letra = LrcParser.parse(
            """
            [ti:Ciranda, Cirandinha]
            [ar:Cantiga popular]
            [00:05.00]Ciranda, cirandinha
            [00:08.50]Vamos todos cirandar
            """.trimIndent()
        )

        assertEquals("Ciranda, Cirandinha", letra.titulo)
        assertEquals("Cantiga popular", letra.artista)
        assertEquals(listOf(5_000L, 8_500L), letra.linhas.map { it.inicioMs })
        assertEquals("Vamos todos cirandar", letra.linhas[1].texto)
    }

    @Test
    fun `linha com varias marcacoes de tempo vira varias linhas ordenadas`() {
        val letra = LrcParser.parse(
            """
            [00:10.00][00:30.00]Refrão
            [00:20.00]Verso
            """.trimIndent()
        )

        assertEquals(listOf("Refrão", "Verso", "Refrão"), letra.linhas.map { it.texto })
        assertEquals(listOf(10_000L, 20_000L, 30_000L), letra.linhas.map { it.inicioMs })
    }

    @Test
    fun `aceita fracoes de 1, 2 e 3 digitos e sem fracao`() {
        val letra = LrcParser.parse("[00:01.5]a\n[00:02.25]b\n[00:03.125]c\n[01:04]d")
        assertEquals(listOf(1_500L, 2_250L, 3_125L, 64_000L), letra.linhas.map { it.inicioMs })
    }

    @Test
    fun `offset positivo adianta a letra sem ficar negativo`() {
        val letra = LrcParser.parse("[offset:+500]\n[00:00.20]a\n[00:02.00]b")
        assertEquals(listOf(0L, 1_500L), letra.linhas.map { it.inicioMs })
    }

    @Test
    fun `ignora BOM, linhas vazias e tags desconhecidas`() {
        val letra = LrcParser.parse("﻿[by:alguém]\n\n[xx]\n[00:01.00]ok")
        assertEquals(1, letra.linhas.size)
        assertNull(letra.titulo)
    }

    @Test
    fun `indice e progresso acompanham o tempo`() {
        val letra = LrcParser.parse("[00:05.00]a\n[00:10.00]b")

        assertEquals(-1, letra.indiceEm(4_999))
        assertEquals(0, letra.indiceEm(5_000))
        assertEquals(0, letra.indiceEm(9_999))
        assertEquals(1, letra.indiceEm(60_000))

        assertEquals(0.5f, letra.progressoDaLinha(0, 7_500), 0.001f)
        assertEquals(1f, letra.progressoDaLinha(0, 20_000), 0.001f)
        // Última linha dura DURACAO_ULTIMA_LINHA_MS.
        assertEquals(0.5f, letra.progressoDaLinha(1, 12_000), 0.001f)
        assertEquals(14_000L, letra.fimMs)
    }
}
