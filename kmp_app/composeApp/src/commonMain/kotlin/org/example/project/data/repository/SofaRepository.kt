package org.example.project.data.repository

import org.example.project.data.model.*
import org.example.project.data.remote.SofaApiClient

/**
 * Repozytorium opakowujące SofaApiClient.
 * Warstwa między ViewModelami a logiką HTTP.
 * Wywoływana z coroutines (suspend functions).
 */
class SofaRepository(
    private val apiClient: SofaApiClient = SofaApiClient("https://api.serkad.ovh")
) {

    // --- Autoryzacja ---

    /**
     * Próbuje zalogować się z podanym hasłem.
     * Zwraca true jeśli zalogowano pomyślnie.
     */
    suspend fun login(password: String): Result<LoginResponse> = runCatching {
        apiClient.login(password)
    }

    // --- Drużyny ---

    /**
     * Pobiera listę drużyn w bieżącym sezonie.
     */
    suspend fun getTeams(): Result<List<String>> = runCatching {
        apiClient.getTeams().teams
    }

    // --- Zawodnicy ---

    /**
     * Pobiera listę zawodników danej drużyny, posortowaną alfabetycznie.
     */
    suspend fun getPlayers(team: String): Result<List<Player>> = runCatching {
        apiClient.getPlayers(team).players
    }

    // --- Historia meczów ---

    /**
     * Pobiera listę meczów zawodnika (od najnowszego), z etykietami i ocenami.
     */
    suspend fun getMatchHistory(playerId: Int): Result<List<MatchHistoryItem>> = runCatching {
        apiClient.getMatchHistory(playerId)
    }

    // --- Raport meczowy ---

    /**
     * Pobiera pełne statystyki zawodnika z konkretnego meczu.
     * stats to mapa klucz->wartość z wszystkimi obliczonymi metrykami.
     */
    suspend fun getMatchReport(playerId: Int, matchId: String): Result<MatchReportResponse> = runCatching {
        apiClient.getMatchReport(playerId, matchId)
    }

    // --- Statystyki sezonowe ---

    /**
     * Pobiera dane z player_database (do wykresów radarowych i percentyli).
     * stats to mapa klucz->wartość z sumarycznymi statystykami sezonu.
     */
    suspend fun getSeasonStats(playerId: Int): Result<SeasonStatsResponse> = runCatching {
        apiClient.getSeasonStats(playerId)
    }
}
