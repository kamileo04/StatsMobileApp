package org.example.project

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import dowklejenia.composeapp.generated.resources.Res
import dowklejenia.composeapp.generated.resources.ball
import dowklejenia.composeapp.generated.resources.compose_multiplatform
import org.jetbrains.compose.resources.painterResource

data class Tile(
    val title: String
)
@Composable
fun TileCard(tile: Tile) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(5.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clickable(onClick = ::buttonOnClick)

        ) {

            Image(
                painter = painterResource(Res.drawable.ball),
                contentDescription = "sada",
                modifier = Modifier.fillMaxWidth(),
                alignment = Alignment.Center,

                contentScale = ContentScale.Crop // dopasowanie obrazka do boxa
            )
            Text(tile.title,
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black
            )

        }
    }
}

fun buttonOnClick(){
    println("Button Clicked")


    return
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {

    val tiles = List(20) {
        Tile("Item $it")
    }

    MaterialTheme {
        Scaffold(
            topBar = {

                TopAppBar(
                    title = {
                        Text(
                            text = "Staty Apka",
                            style = MaterialTheme.typography.titleSmall


                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    )
                )
            }
        ) { innerPadding ->
            LazyVerticalGrid(
                columns = GridCells.Adaptive( 140.dp),
                contentPadding = PaddingValues(top = innerPadding.calculateTopPadding()),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(tiles) { tile ->
                    TileCard(tile)
                }
            }
        }
    }
}
