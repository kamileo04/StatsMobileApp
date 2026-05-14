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
    expectSuccess = true
}


class SofaApiClient(
    private val baseUrl: String,
    private val client: HttpClient = httpClient
) {

    /** POST /login */
    suspend fun login(password: String): LoginResponse =
        client.post("$baseUrl/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(password))
        }.body()

    /** GET /teams */
    suspend fun getTeams(): TeamsResponse =
        client.get("$baseUrl/teams").body()

    /** GET /players/{team} */
    suspend fun getPlayers(team: String): PlayersResponse =
        client.get("$baseUrl/players/${team.encodeURLPath()}").body()

    /** GET /matches/{player_id} */
    suspend fun getMatchHistory(playerId: Int): List<MatchHistoryItem> =
        client.get("$baseUrl/matches/$playerId").body()

    /** GET /match_report/{player_id}/{match_id} */
    suspend fun getMatchReport(playerId: Int, matchId: String): MatchReportResponse =
        client.get("$baseUrl/match_report/$playerId/$matchId").body()

    /** GET /season_stats/{player_id} */
    suspend fun getSeasonStats(playerId: Int): SeasonStatsResponse =
        client.get("$baseUrl/season_stats/$playerId").body()
        
    /** GET /player_percentiles/{player_id}?template={template} */
    suspend fun getPlayerPercentiles(playerId: Int, template: String = "Auto", minMinutes: Int = 300): PlayerPercentilesResponse {
        return client.get("$baseUrl/player_percentiles/$playerId") {
            parameter("template", template)
            parameter("min_minutes", minMinutes)
        }.body()
    }
}
