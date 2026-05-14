package org.example.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.project.data.model.Player
import org.example.project.ui.theme.AppDesign

@Composable
fun FavoritesScreen(
    favoritePlayers: List<Player>,
    onShowStats: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppDesign.ScreenPadding)
    ) {
        Text(
            text = "Ulubieni zawodnicy",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = AppDesign.SectionSpacing)
        )
        
        if (favoritePlayers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(AppDesign.ContentPadding))
                    Text(
                        "Brak ulubionych zawodników",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Dodaj zawodników do ulubionych na ekranie głównym",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = AppDesign.TinySpacing)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(AppDesign.ItemSpacing),
                modifier = Modifier.fillMaxSize()
            ) {
                items(favoritePlayers) { player ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = AppDesign.CardShape,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = AppDesign.CardElevation
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(AppDesign.CardInnerPadding),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(AppDesign.IconSizeMedium)
                                )
                                Spacer(modifier = Modifier.width(AppDesign.ItemSpacing))
                                Text(
                                    text = player.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Button(
                                onClick = { onShowStats(player.id) },
                                shape = AppDesign.ButtonShape,
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 1.dp,
                                    pressedElevation = 0.dp
                                )
                            ) {
                                Text("Statystyki")
                            }
                        }
                    }
                }
            }
        }
    }
}
