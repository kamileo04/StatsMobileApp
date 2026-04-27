package org.example.project.data.repository

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.example.project.data.model.*
import org.example.project.data.remote.SofaApiClient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Testy jednostkowe dla SofaRepository.
 * Weryfikują mapowanie Result<> i propagację błędów.
 */
class SofaRepositoryTest {

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun buildRepository(
        handler: suspend MockRequestHandleScope.(io.ktor.client.request.HttpRequestData) -> io.ktor.client.request.HttpResponseData
    ): SofaRepository {
        val mockClient = HttpClient(MockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true })
            }
            engine { addHandler(handler) }
        }
        return SofaRepository(SofaApiClient(baseUrl = "https://mock.example.com", client = mockClient))
    }

    private fun okJson(path: String, body: String): suspend MockRequestHandleScope.(io.ktor.client.request.HttpRequestData) -> io.ktor.client.request.HttpResponseData =
        { request ->
            if (request.url.encodedPath == path || request.url.encodedPath.startsWith(path)) {
                respond(
                    content = body,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            } else {
                respondError(HttpStatusCode.NotFound)
            }
        }

    private fun serverError(): suspend MockRequestHandleScope.(io.ktor.client.request.HttpRequestData) -> io.ktor.client.request.HttpResponseData =
        { _ -> respondError(HttpStatusCode.InternalServerError) }

    // -------------------------------------------------------------------------
    // login
    // -------------------------------------------------------------------------

    @Test
    fun `login returns Result success on valid response`() = runTest {
        val repo = buildRepository(okJson(
            "/login",
            """{"success": true, "token": "tok123", "message": "OK"}"""
        ))

        val result = repo.login("secret")

        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertNotNull(response)
        assertTrue(response.success)
        assertEquals("tok123", response.token)
    }

    @Test
    fun `login returns Result success but success=false on wrong password`() = runTest {
        val repo = buildRepository(okJson(
            "/login",
            """{"success": false, "token": null, "message": "Złe hasło"}"""
        ))

        val result = repo.login("wrong")

        // Serwer zwrócił 200, więc Result to Success (nie Failure)
        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertNotNull(response)
        assertFalse(response.success)
        assertNull(response.token)
        assertEquals("Złe hasło", response.message)
    }

    @Test
    fun `login returns Result failure on server error`() = runTest {
        val repo = buildRepository(serverError())

        val result = repo.login("any")

        assertTrue(result.isFailure)
        assertNull(result.getOrNull())
    }

    // -------------------------------------------------------------------------
    // getTeams
    // -------------------------------------------------------------------------

    @Test
    fun `getTeams returns list of team names on success`() = runTest {
        val repo = buildRepository(okJson(
            "/teams",
            """{"season": "2023/24", "teams": ["Podbeskidzie", "Wisła Kraków", "Cracovia"]}"""
        ))

        val result = repo.getTeams()

        assertTrue(result.isSuccess)
        val teams = result.getOrNull()
        assertNotNull(teams)
        assertEquals(3, teams.size)
        assertTrue(teams.contains("Wisła Kraków"))
    }

    @Test
    fun `getTeams returns empty list when no teams`() = runTest {
        val repo = buildRepository(okJson("/teams", """{"season": "2023/24", "teams": []}"""))

        val result = repo.getTeams()

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
    }

    @Test
    fun `getTeams returns Result failure on server error`() = runTest {
        val repo = buildRepository(serverError())

        val result = repo.getTeams()

        assertTrue(result.isFailure)
    }

    // -------------------------------------------------------------------------
    // getPlayers
    // -------------------------------------------------------------------------

    @Test
    fun `getPlayers returns list of Player objects`() = runTest {
        val repo = buildRepository(okJson(
            "/players/Podbeskidzie",
            """{"team": "Podbeskidzie", "players": [{"id": 1, "name": "Jan Kowalski"}, {"id": 2, "name": "Adam Nowak"}]}"""
        ))

        val result = repo.getPlayers("Podbeskidzie")

        assertTrue(result.isSuccess)
        val players = result.getOrNull()
        assertNotNull(players)
        assertEquals(2, players.size)
        assertEquals(1, players[0].id)
        assertEquals("Jan Kowalski", players[0].name)
    }

    @Test
    fun `getPlayers returns Result failure on 404`() = runTest {
        val repo = buildRepository { _ -> respondError(HttpStatusCode.NotFound) }

        val result = repo.getPlayers("Nieznany")

        assertTrue(result.isFailure)
    }

    // -------------------------------------------------------------------------
    // getMatchHistory
    // -------------------------------------------------------------------------

    @Test
    fun `getMatchHistory returns list of MatchHistoryItem`() = runTest {
        val repo = buildRepository(okJson(
            "/matches/42",
            """[
              {"match_id": "m001", "label": "Podbeskidzie vs Wisła", "minutes": 90, "rating": "7.5", "timestamp": 1711000000},
              {"match_id": "m002", "label": "Test mecz", "minutes": 45, "rating": null, "timestamp": 1710000000}
            ]"""
        ))

        val result = repo.getMatchHistory(42)

        assertTrue(result.isSuccess)
        val matches = result.getOrNull()
        assertNotNull(matches)
        assertEquals(2, matches.size)
        assertEquals("m001", matches[0].matchId)
        assertNull(matches[1].rating)
    }

    @Test
    fun `getMatchHistory returns empty list`() = runTest {
        val repo = buildRepository(okJson("/matches/99", "[]"))

        val result = repo.getMatchHistory(99)

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
    }

    @Test
    fun `getMatchHistory returns failure on server error`() = runTest {
        val repo = buildRepository(serverError())

        val result = repo.getMatchHistory(1)

        assertTrue(result.isFailure)
    }

    // -------------------------------------------------------------------------
    // getMatchReport
    // -------------------------------------------------------------------------

    @Test
    fun `getMatchReport returns MatchReportResponse with stats`() = runTest {
        val repo = buildRepository(okJson(
            "/match_report/42/m001",
            """{"match_id": "m001", "label": "Mecz testowy", "minutes": 90, "rating": "7.0", "stats": {"goals": 1, "assists": 2}}"""
        ))

        val result = repo.getMatchReport(42, "m001")

        assertTrue(result.isSuccess)
        val report = result.getOrNull()
        assertNotNull(report)
        assertEquals("m001", report.matchId)
        assertEquals(2, report.stats.size)
        assertTrue(report.stats.containsKey("goals"))
    }

    @Test
    fun `getMatchReport returns failure on 404`() = runTest {
        val repo = buildRepository { _ -> respondError(HttpStatusCode.NotFound) }

        val result = repo.getMatchReport(1, "nonexistent")

        assertTrue(result.isFailure)
    }

    // -------------------------------------------------------------------------
    // getSeasonStats
    // -------------------------------------------------------------------------

    @Test
    fun `getSeasonStats returns SeasonStatsResponse`() = runTest {
        val repo = buildRepository(okJson(
            "/season_stats/42",
            """{"player_id": 42, "season": "2023/24", "stats": {"goals": 12, "matches": 30}}"""
        ))

        val result = repo.getSeasonStats(42)

        assertTrue(result.isSuccess)
        val stats = result.getOrNull()
        assertNotNull(stats)
        assertEquals(42, stats.playerId)
        assertEquals("2023/24", stats.season)
        assertEquals(2, stats.stats.size)
    }

    @Test
    fun `getSeasonStats returns failure on server error`() = runTest {
        val repo = buildRepository(serverError())

        val result = repo.getSeasonStats(42)

        assertTrue(result.isFailure)
    }

    // -------------------------------------------------------------------------
    // getPlayerPercentiles
    // -------------------------------------------------------------------------

    @Test
    fun `getPlayerPercentiles returns PlayerPercentilesResponse`() = runTest {
        val repo = buildRepository { request ->
            if (request.url.encodedPath.startsWith("/player_percentiles/42")) {
                respond(
                    content = """
                    {
                      "playerName": "Jan Kowalski",
                      "position": "Napastnik",
                      "groupSize": 85,
                      "minutes": 2000.0,
                      "radarChart": [
                        {"statKey": "goals", "label": "Gole", "value": 12.0, "percentile": 92, "median": 5.0}
                      ],
                      "fullTable": [
                        {"statKey": "goals", "label": "Gole", "totalValue": 12.0, "p90Value": 0.54, "percentile": 92, "median": 5.0}
                      ]
                    }""",
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            } else {
                respondError(HttpStatusCode.NotFound)
            }
        }

        val result = repo.getPlayerPercentiles(42, "Napastnik", 300)

        assertTrue(result.isSuccess)
        val percentiles = result.getOrNull()
        assertNotNull(percentiles)
        assertEquals("Jan Kowalski", percentiles.playerName)
        assertEquals(1, percentiles.radarChart.size)
        assertEquals(92, percentiles.radarChart[0].percentile)
        assertEquals(1, percentiles.fullTable.size)
        assertEquals(0.54, percentiles.fullTable[0].p90Value)
    }

    @Test
    fun `getPlayerPercentiles uses Auto template and 300 min by default`() = runTest {
        var templateParam: String? = null
        var minMinutesParam: String? = null

        val repo = buildRepository { request ->
            templateParam = request.url.parameters["template"]
            minMinutesParam = request.url.parameters["min_minutes"]
            respond(
                content = """{"playerName":"X","position":"X","groupSize":1,"minutes":300.0,"radarChart":[],"fullTable":[]}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        repo.getPlayerPercentiles(5)

        assertEquals("Auto", templateParam)
        assertEquals("300", minMinutesParam)
    }

    @Test
    fun `getPlayerPercentiles returns failure on server error`() = runTest {
        val repo = buildRepository(serverError())

        val result = repo.getPlayerPercentiles(1)

        assertTrue(result.isFailure)
    }
}
