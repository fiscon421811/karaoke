package br.com.karaokebrasil.data

/**
 * Gênero musical, definido no `catalogo.json` do servidor (não no código), para que
 * novos gêneros possam ser criados sem recompilar o app.
 *
 * @param ordem posição do gênero na tela inicial.
 * @param cor cor em ARGB, ex.: 0xFFD4A017.
 */
data class Genero(
    val id: String,
    val nome: String,
    val cor: Long,
    val ordem: Int,
)
