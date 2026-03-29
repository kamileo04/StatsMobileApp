package org.example.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.example.project.data.model.PlayerPercentilesResponse
import org.example.project.data.repository.SofaRepository
import org.example.project.ui.components.PlayerFullStatsTable
import org.example.project.ui.components.PlayerPercentileChart

@Composable
fun PlayerSeasonScreen(
    playerId: Int,
    repository: SofaRepository,
    onBackClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var percentilesData by remember { mutableStateOf<PlayerPercentilesResponse?>(null) }
    
    // Hardcodowane dla testu auto-template, w przyszłości ze stanu UI
    val selectedTemplate = "Auto"

    LaunchedEffect(playerId) {
        isLoading = true
        repository.getPlayerPercentiles(playerId, selectedTemplate)
            .onSuccess { data ->
                percentilesData = data
                errorMessage = null
            }
            .onFailure { error ->
                errorMessage = error.message ?: "Wystąpił nieznany błąd"
            }
        isLoading = false
    }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Button(onClick = onBackClick, modifier = Modifier.padding(bottom = 8.dp)) {
            Text("Powrót")
        }
        
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Błąd: $errorMessage", color = MaterialTheme.colorScheme.error)
            }
        } else if (percentilesData != null) {
            val data = percentilesData!!
            
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                Text(
                    text = "Player: ${data.playerName}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
                Text(
                    text = "Pozycja: ${data.position} | Grupa: ${data.groupSize} graczy",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 16.dp)
                )
                
                // Wykres Słupkowy Percentyli dla wybranej pozycji (Zastępstwo dla Pizza Chart)
                PlayerPercentileChart(
                    title = "Profil (${data.position})", 
                    stats = data.radarChart
                )
                
                // Pełna tabela wszystkich statystyk
                PlayerFullStatsTable(stats = data.fullTable)
            }
        }
    }
}
