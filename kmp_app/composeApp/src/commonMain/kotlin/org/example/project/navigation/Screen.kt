package org.example.project.navigation

sealed class Screen {
    data object Home : Screen()
    data class Details(val title: String) : Screen()
    data class JsonViewer(val moduleName: String, val path: String) : Screen()
    data class PlayerSeason(val playerId: Int) : Screen()
    data class MatchReport(val playerId: Int, val matchId: String) : Screen()
    data object Favorites : Screen()
    data class ComparePlayers(val initialPlayerId: Int?) : Screen()
}
