package br.com.karaokebrasil.letra

data class LinhaLetra(val inicioMs: Long, val texto: String)

data class Letra(
    val linhas: List<LinhaLetra>,
    val titulo: String? = null,
    val artista: String? = null,
) {
    /** Índice da linha sendo cantada em [posicaoMs], ou -1 antes da primeira linha. */
    fun indiceEm(posicaoMs: Long): Int {
        var inicio = 0
        var fim = linhas.lastIndex
        var resultado = -1
        while (inicio <= fim) {
            val meio = (inicio + fim) ushr 1
            if (linhas[meio].inicioMs <= posicaoMs) {
                resultado = meio
                inicio = meio + 1
            } else {
                fim = meio - 1
            }
        }
        return resultado
    }

    /** Quanto da linha [indice] já foi cantado (0..1), usado para "pintar" o texto. */
    fun progressoDaLinha(indice: Int, posicaoMs: Long): Float {
        val linha = linhas.getOrNull(indice) ?: return 0f
        val fimMs = linhas.getOrNull(indice + 1)?.inicioMs ?: (linha.inicioMs + DURACAO_ULTIMA_LINHA_MS)
        val duracao = (fimMs - linha.inicioMs).coerceAtLeast(1)
        return ((posicaoMs - linha.inicioMs).toFloat() / duracao).coerceIn(0f, 1f)
    }

    val fimMs: Long
        get() = (linhas.lastOrNull()?.inicioMs ?: 0L) + DURACAO_ULTIMA_LINHA_MS

    companion object {
        const val DURACAO_ULTIMA_LINHA_MS = 4_000L
    }
}
