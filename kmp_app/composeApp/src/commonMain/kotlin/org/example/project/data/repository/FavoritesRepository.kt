package org.example.project.data.repository

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.example.project.data.model.Player

class FavoritesRepository {
    private val settings = Settings()
    private val PREF_FAVORITES = "favorite_players_json"

    fun getFavoritePlayers(): List<Player> {
        val json = settings.getString(PREF_FAVORITES, "")
        if (json.isBlank()) return emptyList()
        return try {
            Json.decodeFromString<List<Player>>(json)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun toggleFavorite(player: Player) {
        val current = getFavoritePlayers().toMutableList()
        val index = current.indexOfFirst { it.id == player.id }
        if (index >= 0) {
            current.removeAt(index)
        } else {
            current.add(player)
        }
        settings[PREF_FAVORITES] = Json.encodeToString(current)
    }

    fun isFavorite(playerId: Int): Boolean {
        return getFavoritePlayers().any { it.id == playerId }
    }
}
