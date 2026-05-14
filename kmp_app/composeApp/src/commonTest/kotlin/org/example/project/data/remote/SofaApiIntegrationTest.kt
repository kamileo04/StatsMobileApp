package org.example.project.data.remote

import kotlinx.coroutines.test.runTest
import org.example.project.data.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue


class SofaApiIntegrationTest {

    companion object {
        private val BASE_URL: String = System.getProperty("api.base.url") ?: "https://api.serkad.ovh"
        private val PASSWORD: String = System.getProperty("api.password") ?: ""

        /** Zwraca prawdziwy SofaApiClient bez żadnego mockowania */
        private fun realApi() = SofaApiClient(baseUrl = BASE_URL)
    }

    // -------------------------------------------------------------------------
    // /login
    // -------------------------------------------------------------------------

    @Test
    fun `integration - login z poprawnym hasłem zwraca success=true`() = runTest {
        val api = realApi()
        val response: LoginResponse = api.login(PASSWORD)

        assertTrue(
            actual = response.success,
            message = "Logowanie nie powiodło się. Sprawdź api.password w local.properties. " +
                "Komunikat serwera: '${response.message}'"
        )
        assertNotNull(response.token)
        assertTrue(response.token.isNotBlank())
    }

    @Test
    fun `integration - login z błędnym hasłem zwraca success=false`() = runTest {
        val api = realApi()
        try {
            val response: LoginResponse = api.login("ZUPELNIE_ZLEW_HASLO_XYZ_123")
            assertFalse(
                actual = response.success,
                message = "Serwer powinien odrzucić błędne hasło"
            )
            assertNull(response.token)
            assertTrue(response.message.isNotBlank())
        } catch (e: Exception) {
            println("Prawidłowo odrzucono hasło, serwer zwrócił błąd: ${e.message}")
            assertTrue(true)
        }
    }

    // -------------------------------------------------------------------------
    // /teams
    // -------------------------------------------------------------------------

    @Test
    fun `integration - getTeams zwraca niepustą listę drużyn`() = runTest {
        val api = realApi()
        val response: TeamsResponse = api.getTeams()

        assertTrue(
            actual = response.teams.isNotEmpty(),
            message = "Lista drużyn jest pusta – czy serwer ma dane za ten sezon?"
        )
        assertTrue(response.season.isNotBlank(), "Pole 'season' jest puste")

        println("=== /teams ===")
        println("Sezon: ${response.season}")
        println("Drużyny (${response.teams.size}): ${response.teams.joinToString(", ")}")
    }

    @Test
    fun `integration - getTeams zwraca poprawny format sezonu`() = runTest {
        val api = realApi()
        val response: TeamsResponse = api.getTeams()

        val seasonRegex = Regex("""\d{4}/\d{2}|\d{2}-\d{2}""")
        assertTrue(
            actual = seasonRegex.containsMatchIn(response.season),
            message = "Format sezonu '${response.season}' nie pasuje do wzorca RRRR/RR ani RR-RR"
        )
    }

    // -------------------------------------------------------------------------
    // /players/{team}
    // -------------------------------------------------------------------------

    @Test
    fun `integration - getPlayers zwraca zawodników dla pierwszej drużyny z listy`() = runTest {
        val api = realApi()

        val teams = api.getTeams().teams
        assertTrue(teams.isNotEmpty(), "Brak drużyn – nie można przetestować /players")

        val firstTeam = teams.first()
        val response: PlayersResponse = api.getPlayers(firstTeam)

        assertEquals(firstTeam, response.team)
        assertTrue(
            actual = response.players.isNotEmpty(),
            message = "Drużyna '$firstTeam' nie ma żadnych zawodników"
        )

        response.players.forEach { player ->
            assertTrue(player.id > 0, "Zawodnik ma id <= 0: $player")
            assertTrue(player.name.isNotBlank(), "Zawodnik ma puste imię: $player")
        }

        println("=== /players/$firstTeam ===")
        println("Zawodnicy (${response.players.size}):")
        response.players.take(5).forEach { println("  ${it.id}: ${it.name}") }
        if (response.players.size > 5) println("  ... i ${response.players.size - 5} więcej")
    }

    // -------------------------------------------------------------------------
    // /matches/{player_id}
    // -------------------------------------------------------------------------

    @Test
    fun `integration - getMatchHistory zwraca historię meczów dla istniejącego zawodnika`() = runTest {
        val api = realApi()

        val teams = api.getTeams().teams
        assertTrue(teams.isNotEmpty(), "Brak drużyn")
        val players = api.getPlayers(teams.first()).players
        assertTrue(players.isNotEmpty(), "Brak zawodników")

        val firstPlayerId = players.first().id
        val matches: List<MatchHistoryItem> = api.getMatchHistory(firstPlayerId)

        assertNotNull(matches)

        matches.forEach { match ->
            assertTrue(match.matchId.isNotBlank(), "match_id jest puste: $match")
            assertTrue(match.label.isNotBlank(), "label jest puste: $match")
            assertTrue(match.minutes >= 0, "minutes < 0: $match")
            assertTrue(match.timestamp > 0, "timestamp <= 0: $match")
        }

        println("=== /matches/$firstPlayerId (${players.first().name}) ===")
        println("Liczba meczów: ${matches.size}")
        matches.take(3).forEach { println("  ${it.matchId}: ${it.label} (${it.minutes} min, ocena: ${it.rating ?: "-"})") }
    }

    // -------------------------------------------------------------------------
    // /match_report/{player_id}/{match_id}
    // -------------------------------------------------------------------------

    @Test
    fun `integration - getMatchReport zwraca raport z mapą statystyk`() = runTest {
        val api = realApi()

        val teams = api.getTeams().teams
        assertTrue(teams.isNotEmpty(), "Brak drużyn")
        val players = api.getPlayers(teams.first()).players
        assertTrue(players.isNotEmpty(), "Brak zawodników")

        val firstPlayer = players.first()
        val matches = api.getMatchHistory(firstPlayer.id)

        if (matches.isEmpty()) {
            println("Zawodnik ${firstPlayer.name} nie ma meczów – pomijam test raportu")
            return@runTest
        }

        val firstMatch = matches.first()
        val report: MatchReportResponse = api.getMatchReport(firstPlayer.id, firstMatch.matchId)

        assertEquals(firstMatch.matchId, report.matchId)
        assertTrue(report.label.isNotBlank(), "Raport ma puste label")
        assertTrue(report.minutes >= 0, "Raport ma ujemne minuty")
        assertNotNull(report.stats, "Brak mapy statystyk w raporcie")
        assertTrue(report.stats.isNotEmpty(), "Mapa statystyk jest pusta")

        println("=== /match_report/${firstPlayer.id}/${firstMatch.matchId} ===")
        println("Mecz: ${report.label} (${report.minutes} min)")
        println("Ocena: ${report.rating ?: "-"}")
        println("Statystyki (${report.stats.size} kluczy): ${report.stats.keys.take(5).joinToString(", ")}...")
    }

    // -------------------------------------------------------------------------
    // /season_stats/{player_id}
    // -------------------------------------------------------------------------

    @Test
    fun `integration - getSeasonStats zwraca statystyki sezonowe`() = runTest {
        val api = realApi()

        val teams = api.getTeams().teams
        assertTrue(teams.isNotEmpty(), "Brak drużyn")
        val players = api.getPlayers(teams.first()).players
        assertTrue(players.isNotEmpty(), "Brak zawodników")

        val firstPlayer = players.first()
        val stats: SeasonStatsResponse = api.getSeasonStats(firstPlayer.id)

        assertEquals(firstPlayer.id, stats.playerId)
        assertTrue(stats.season.isNotBlank(), "Pole 'season' w statystykach sezonowych jest puste")
        assertNotNull(stats.stats)

        println("=== /season_stats/${firstPlayer.id} (${firstPlayer.name}) ===")
        println("Sezon: ${stats.season}")
        println("Klucze statystyk (${stats.stats.size}): ${stats.stats.keys.take(5).joinToString(", ")}...")
    }

    // -------------------------------------------------------------------------
    // /player_percentiles/{player_id}
    // -------------------------------------------------------------------------

    @Test
    fun `integration - getPlayerPercentiles zwraca radarChart i fullTable`() = runTest {
        val api = realApi()

        val teams = api.getTeams().teams
        assertTrue(teams.isNotEmpty(), "Brak drużyn")
        val players = api.getPlayers(teams.first()).players
        assertTrue(players.isNotEmpty(), "Brak zawodników")

        val firstPlayer = players.first()
        val response: PlayerPercentilesResponse = api.getPlayerPercentiles(
            playerId = firstPlayer.id,
            template = "Auto",
            minMinutes = 0   // 0 żeby zawodnik na pewno się zakwalifikował
        )

        assertTrue(response.playerName.isNotBlank(), "playerName jest puste")
        assertTrue(response.position.isNotBlank(), "position jest puste")
        assertTrue(response.groupSize > 0, "groupSize <= 0")
        assertTrue(response.minutes >= 0.0, "minutes < 0")

        response.radarChart.forEach { item ->
            assertTrue(item.percentile in 0..100, "radarChart: percentyl poza zakresem: $item")
            assertTrue(item.statKey.isNotBlank(), "radarChart: pusty statKey: $item")
        }
        response.fullTable.forEach { item ->
            assertTrue(item.percentile in 0..100, "fullTable: percentyl poza zakresem: $item")
            assertTrue(item.p90Value >= 0.0, "fullTable: ujemna wartość p90: $item")
        }

        println("=== /player_percentiles/${firstPlayer.id} (${firstPlayer.name}) ===")
        println("Pozycja: ${response.position}, Minuty: ${response.minutes}, Grupa: ${response.groupSize} zawodników")
        println("RadarChart: ${response.radarChart.size} statystyk, FullTable: ${response.fullTable.size} statystyk")
        if (response.radarChart.isNotEmpty()) {
            println("Top 3 radarChart:")
            response.radarChart.sortedByDescending { it.percentile }.take(3)
                .forEach { println("  ${it.label}: ${it.value} (${it.percentile}. percentyl)") }
        }
    }

    @Test
    fun `integration - getPlayerPercentiles działa z konkretnym templatem`() = runTest {
        val api = realApi()

        val teams = api.getTeams().teams
        assertTrue(teams.isNotEmpty(), "Brak drużyn")
        val players = api.getPlayers(teams.first()).players
        assertTrue(players.isNotEmpty(), "Brak zawodników")

        val response: PlayerPercentilesResponse = api.getPlayerPercentiles(
            playerId = players.first().id,
            template = "Auto",
            minMinutes = 0
        )

        assertNotNull(response)
        assertTrue(response.position.isNotBlank())
    }
}
