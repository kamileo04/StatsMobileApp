package org.example.project.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ---------- /login ----------

@Serializable
data class LoginRequest(val password: String)

@Serializable
data class LoginResponse(
    val success: Boolean,
    val token: String? = null,
    val message: String
)

// ---------- /teams ----------

@Serializable
data class TeamsResponse(
    val season: String,
    val teams: List<String>
)

// ---------- /players/{team} ----------

@Serializable
data class Player(
    val id: Int,
    val name: String
)

@Serializable
data class PlayersResponse(
    val team: String,
    val players: List<Player>
)

// ---------- /matches/{player_id} ----------

@Serializable
data class MatchHistoryItem(
    @SerialName("match_id") val matchId: String,
    val label: String,
    val minutes: Int,
    val rating: String? = null,   // może być float jako String, lub '-'
    val timestamp: Long
)

// ---------- /match_report/{player_id}/{match_id} ----------

@Serializable
data class MatchReportResponse(
    @SerialName("match_id") val matchId: String,
    val label: String,
    val minutes: Int,
    val rating: String? = null,
    val stats: Map<String, kotlinx.serialization.json.JsonElement>  // dowolny JSON
)

// ---------- /season_stats/{player_id} ----------

@Serializable
data class SeasonStatsResponse(
    @SerialName("player_id") val playerId: Int,
    val season: String,
    val stats: Map<String, kotlinx.serialization.json.JsonElement>
)
