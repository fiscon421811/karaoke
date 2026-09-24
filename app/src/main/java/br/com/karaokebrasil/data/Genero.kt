package br.com.karaokebrasil.data

/**
 * Gêneros musicais do catálogo. O código de cada música começa pelo [prefixo]
 * do gênero (ex.: 1001 = primeira música de Sertanejo).
 */
enum class Genero(val nome: String, val prefixo: Int, val cor: Long) {
    SERTANEJO("Sertanejo", 1, 0xFFD4A017),
    MPB("MPB", 2, 0xFF2E86AB),
    SAMBA("Samba", 3, 0xFF2A9D8F),
    PAGODE("Pagode", 4, 0xFFE76F51),
    FORRO("Forró", 5, 0xFFF4A261),
    AXE("Axé", 6, 0xFFE63946),
    ROCK_NACIONAL("Rock Nacional", 7, 0xFF6C757D),
    BOSSA_NOVA("Bossa Nova", 8, 0xFF457B9D),
    FUNK("Funk", 9, 0xFF9B5DE5),
    GOSPEL("Gospel", 10, 0xFF4CAF50),
    BREGA_ROMANTICA("Brega & Românticas", 11, 0xFFD63384),
    FOLCLORE_INFANTIL("Folclore & Infantil", 12, 0xFF00B4D8),
}
