package br.com.karaokebrasil.ui.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import br.com.karaokebrasil.data.Musica
import br.com.karaokebrasil.ui.theme.Destaque
import br.com.karaokebrasil.ui.theme.TextoSecundario

val LarguraCard = 240.dp

@Composable
fun MusicaCard(
    musica: Musica,
    temLetra: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cor = Color(musica.genero.cor)
    Card(
        onClick = onClick,
        modifier = modifier.width(LarguraCard).height(136.dp),
        shape = CardDefaults.shape(RoundedCornerShape(12.dp)),
        colors = CardDefaults.colors(
            containerColor = cor.copy(alpha = 0.22f),
            contentColor = Color.White,
            focusedContainerColor = cor.copy(alpha = 0.6f),
            focusedContentColor = Color.White,
        ),
        scale = CardDefaults.scale(focusedScale = 1.08f),
        border = CardDefaults.border(
            focusedBorder = Border(BorderStroke(3.dp, Color.White), shape = RoundedCornerShape(12.dp)),
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = musica.titulo,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = musica.artista,
                    fontSize = 14.sp,
                    color = TextoSecundario,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Nº ${musica.codigo}", fontSize = 13.sp, color = Destaque)
                Spacer(Modifier.weight(1f))
                if (temLetra) {
                    Text(text = "♪ LETRA", fontSize = 11.sp, color = Destaque, fontWeight = FontWeight.Bold)
                }
                if (musica.favorita) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Favorita",
                        tint = Color(0xFFFF4D6D),
                        modifier = Modifier.padding(start = 6.dp).size(16.dp),
                    )
                }
            }
        }
    }
}
