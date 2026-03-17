package org.example.project.data.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.example.project.data.model.*

// Wspólny singleton HttpClient
// Engine (OkHttp/Darwin/Js) jest wstrzykiwany przez actual/expect lub konfigurowany przez platformę.
// Tutaj używamy domyślnego engine'a wykrywanego automatycznie przez Ktor na każdej platformie.
val httpClient = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
        })
    }
    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.INFO
    }
}

/**
 * Klient do komunikacji z SofaMobile API.
 * Wywołaj SofaApiClient(baseUrl = "https://api.serkad.ovh")
 */
class SofaApiClient(private val baseUrl: String) {

    /** POST /login */
    suspend fun login(password: String): LoginResponse =
        httpClient.post("$baseUrl/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(password))
        }.body()

    /** GET /teams */
    suspend fun getTeams(): TeamsResponse =
        httpClient.get("$baseUrl/teams").body()

    /** GET /players/{team} */
    suspend fun getPlayers(team: String): PlayersResponse =
        httpClient.get("$baseUrl/players/${team.encodeURLPath()}").body()

    /** GET /matches/{player_id} */
    suspend fun getMatchHistory(playerId: Int): List<MatchHistoryItem> =
        httpClient.get("$baseUrl/matches/$playerId").body()

    /** GET /match_report/{player_id}/{match_id} */
    suspend fun getMatchReport(playerId: Int, matchId: String): MatchReportResponse =
        httpClient.get("$baseUrl/match_report/$playerId/$matchId").body()

    /** GET /season_stats/{player_id} */
    suspend fun getSeasonStats(playerId: Int): SeasonStatsResponse =
        httpClient.get("$baseUrl/season_stats/$playerId").body()
}
