package org.example.project.data.remote

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.example.project.data.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class SofaApiClientTest {

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun buildMockClient(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData
    ): HttpClient = HttpClient(MockEngine) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                coerceInputValues = true
            })
        }
        engine { addHandler(handler) }
    }

    private fun clientFor(vararg routes: Pair<String, String>): HttpClient {
        val map = routes.toMap()
        return buildMockClient { request ->
            val path = request.url.encodedPath
            val body = map[path]
            if (body != null) {
                respond(
                    content = body,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            } else {
                respondError(HttpStatusCode.NotFound)
            }
        }
    }

    // -------------------------------------------------------------------------
    // /login
    // -------------------------------------------------------------------------

    @Test
    fun `login should return success and token on valid password`() = runTest {
        val client = buildMockClient { request ->
            when (request.url.encodedPath) {
                "/login" -> respond(
                    content = """{"success": true, "token": "abc123", "message": "OK"}""",
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
                else -> respondError(HttpStatusCode.NotFound)
            }
        }

        val api = SofaApiClient(baseUrl = "https://mock.example.com", client = client)
        val response: LoginResponse = api.login("secret")

        assertTrue(response.success)
        assertEquals("abc123", response.token)
        assertEquals("OK", response.message)
    }

    @Test
    fun `login should return failure with null token on wrong password`() = runTest {
        val client = buildMockClient { _ ->
            respond(
                content = """{"success": false, "token": null, "message": "Nieprawidłowe hasło"}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val api = SofaApiClient(baseUrl = "https://mock.example.com", client = client)
        val response: LoginResponse = api.login("wrong")

        assertFalse(response.success)
        assertNull(response.token)
        assertEquals("Nieprawidłowe hasło", response.message)
    }

    @Test
    fun `login should throw on server error`() = runTest {
        val client = buildMockClient { _ ->
            respondError(HttpStatusCode.InternalServerError)
        }

        val api = SofaApiClient(baseUrl = "https://mock.example.com", client = client)
        assertFailsWith<Exception> { api.login("any") }
    }

    // -------------------------------------------------------------------------
    // /teams
    // -------------------------------------------------------------------------

    @Test
    fun `getTeams should return valid TeamsResponse on success`() = runTest {
        val client = clientFor(
            "/teams" to """{"season": "2023/24", "teams": ["Podbeskidzie", "Wisła Kraków"]}"""
        )

        val api = SofaApiClient(baseUrl = "https://mock.example.com", client = client)
        val response: TeamsResponse = api.getTeams()

        assertEquals("2023/24", response.season)
        assertEquals(2, response.teams.size)
        assertTrue(response.teams.contains("Podbeskidzie"))
        assertTrue(response.teams.contains("Wisła Kraków"))
    }

    @Test
    fun `getTeams should return empty list when no teams`() = runTest {
        val client = clientFor(
            "/teams" to """{"season": "2023/24", "teams": []}"""
        )

        val api = SofaApiClient(baseUrl = "https://mock.example.com", client = client)
        val response: TeamsResponse = api.getTeams()

        assertTrue(response.teams.isEmpty())
    }

    @Test
    fun `getTeams should throw on server error`() = runTest {
        val client = buildMockClient { _ -> respondError(HttpStatusCode.InternalServerError) }

        val api = SofaApiClient(baseUrl = "https://mock.example.com", client = client)
        assertFailsWith<Exception> { api.getTeams() }
    }

    // -------------------------------------------------------------------------
    // /players/{team}
    // -------------------------------------------------------------------------

    @Test
    fun `getPlayers should return list of players for given team`() = runTest {
        val client = clientFor(
            "/players/Podbeskidzie" to """
            {
              "team": "Podbeskidzie",
              "players": [
                {"id": 1, "name": "Jan Kowalski"},
                {"id": 2, "name": "Adam Nowak"},
                {"id": 3, "name": "Piotr Wiśniewski"}
              ]
            }"""
        )

        val api = SofaApiClient(baseUrl = "https://mock.example.com", client = client)
        val response: PlayersResponse = api.getPlayers("Podbeskidzie")

        assertEquals("Podbeskidzie", response.team)
        assertEquals(3, response.players.size)
        assertEquals(1, response.players[0].id)
        assertEquals("Jan Kowalski", response.players[0].name)
        assertEquals(2, response.players[1].id)
    }

    @Test
    fun `getPlayers should handle empty player list`() = runTest {
        val client = clientFor(
            "/players/EmptyTeam" to """{"team": "EmptyTeam", "players": []}"""
        )

        val api = SofaApiClient(baseUrl = "https://mock.example.com", client = client)
        val response: PlayersResponse = api.getPlayers("EmptyTeam")

        assertTrue(response.players.isEmpty())
    }

    @Test
    fun `getPlayers should throw on 404`() = runTest {
        val client = buildMockClient { _ -> respondError(HttpStatusCode.NotFound) }

        val api = SofaApiClient(baseUrl = "https://mock.example.com", client = client)
        assertFailsWith<Exception> { api.getPlayers("Unknown") }
    }

    // -------------------------------------------------------------------------
    // /matches/{player_id}
    // -------------------------------------------------------------------------

    @Test
    fun `getMatchHistory should return list of matches`() = runTest {
        val client = clientFor(
            "/matches/42" to """
            [
              {"match_id": "m001", "label": "Podbeskidzie vs Wisła", "minutes": 90, "rating": "7.5", "timestamp": 1711000000},
              {"match_id": "m002", "label": "Cracovia vs Podbeskidzie", "minutes": 67, "rating": null, "timestamp": 1710000000}
            ]"""
        )

        val api = SofaApiClient(baseUrl = "https://mock.example.com", client = client)
        val matches: List<MatchHistoryItem> = api.getMatchHistory(42)

        assertEquals(2, matches.size)
        assertEquals("m001", matches[0].matchId)
        assertEquals("Podbeskidzie vs Wisła", matches[0].label)
        assertEquals(90, matches[0].minutes)
        assertEquals("7.5", matches[0].rating)
        assertEquals(1711000000L, matches[0].timestamp)

        assertEquals("m002", matches[1].matchId)
        assertNull(matches[1].rating)
    }

    @Test
    fun `getMatchHistory should return empty list when no matches`() = runTest {
        val client = clientFor("/matches/99" to "[]")

        val api = SofaApiClient(baseUrl = "https://mock.example.com", client = client)
        val matches: List<MatchHistoryItem> = api.getMatchHistory(99)

        assertTrue(matches.isEmpty())
    }

    @Test
    fun `getMatchHistory should throw on server error`() = runTest {
        val client = buildMockClient { _ -> respondError(HttpStatusCode.InternalServerError) }

        val api = SofaApiClient(baseUrl = "https://mock.example.com", client = client)
        assertFailsWith<Exception> { api.getMatchHistory(1) }
    }

    // -------------------------------------------------------------------------
    // /match_report/{player_id}/{match_id}
    // -------------------------------------------------------------------------

    @Test
    fun `getMatchReport should return report with stats map`() = runTest {
        val client = clientFor(
            "/match_report/42/m001" to """
            {
              "match_id": "m001",
              "label": "Podbeskidzie vs Wisła",
              "minutes": 90,
              "rating": "7.5",
              "stats": {
                "goals": 1,
                "assists": 0,
                "passes_completed": 45,
                "distance_km": 10.2
              }
            }"""
        )

        val api = SofaApiClient(baseUrl = "https://mock.example.com", client = client)
        val report: MatchReportResponse = api.getMatchReport(42, "m001")

        assertEquals("m001", report.matchId)
        assertEquals("Podbeskidzie vs Wisła", report.label)
        assertEquals(90, report.minutes)
        assertEquals("7.5", report.rating)
        assertTrue(report.stats.containsKey("goals"))
        assertTrue(report.stats.containsKey("passes_completed"))
        assertEquals(4, report.stats.size)
    }

    @Test
    fun `getMatchReport should handle null rating`() = runTest {
        val client = clientFor(
            "/match_report/42/m002" to """
            {
              "match_id": "m002",
              "label": "Test Match",
              "minutes": 45,
              "rating": null,
              "stats": {}
            }"""
        )

        val api = SofaApiClient(baseUrl = "https://mock.example.com", client = client)
        val report: MatchReportResponse = api.getMatchReport(42, "m002")

        assertNull(report.rating)
        assertTrue(report.stats.isEmpty())
    }

    @Test
    fun `getMatchReport should throw on 404`() = runTest {
        val client = buildMockClient { _ -> respondError(HttpStatusCode.NotFound) }

        val api = SofaApiClient(baseUrl = "https://mock.example.com", client = client)
        assertFailsWith<Exception> { api.getMatchReport(1, "nonexistent") }
    }

    // -------------------------------------------------------------------------
    // /season_stats/{player_id}
    // -------------------------------------------------------------------------

    @Test
    fun `getSeasonStats should return stats for player`() = runTest {
        val client = clientFor(
            "/season_stats/42" to """
            {
              "player_id": 42,
              "season": "2023/24",
              "stats": {
                "goals": 12,
                "assists": 5,
                "matches_played": 30,
                "minutes_played": 2450.0
              }
            }"""
        )

        val api = SofaApiClient(baseUrl = "https://mock.example.com", client = client)
        val stats: SeasonStatsResponse = api.getSeasonStats(42)

        assertEquals(42, stats.playerId)
        assertEquals("2023/24", stats.season)
        assertEquals(4, stats.stats.size)
        assertTrue(stats.stats.containsKey("goals"))
        assertTrue(stats.stats.containsKey("minutes_played"))
    }

    @Test
    fun `getSeasonStats should handle empty stats map`() = runTest {
        val client = clientFor(
            "/season_stats/1" to """{"player_id": 1, "season": "2023/24", "stats": {}}"""
        )

        val api = SofaApiClient(baseUrl = "https://mock.example.com", client = client)
        val stats: SeasonStatsResponse = api.getSeasonStats(1)

        assertTrue(stats.stats.isEmpty())
    }

    @Test
    fun `getSeasonStats should throw on server error`() = runTest {
        val client = buildMockClient { _ -> respondError(HttpStatusCode.InternalServerError) }

        val api = SofaApiClient(baseUrl = "https://mock.example.com", client = client)
        assertFailsWith<Exception> { api.getSeasonStats(42) }
    }

    // -------------------------------------------------------------------------
    // /player_percentiles/{player_id}
    // -------------------------------------------------------------------------

    @Test
    fun `getPlayerPercentiles should return radar and full table`() = runTest {
        val client = buildMockClient { request ->
            when {
                request.url.encodedPath.startsWith("/player_percentiles/42") -> respond(
                    content = """
                    {
                      "playerName": "Jan Kowalski",
                      "position": "Napastnik",
                      "groupSize": 85,
                      "minutes": 2000.0,
                      "radarChart": [
                        {"statKey": "goals", "label": "Gole", "value": 12.0, "percentile": 92, "median": 5.0},
                        {"statKey": "assists", "label": "Asysty", "value": 5.0, "percentile": 75, "median": 3.0}
                      ],
                      "fullTable": [
                        {"statKey": "goals", "label": "Gole", "totalValue": 12.0, "p90Value": 0.54, "percentile": 92, "median": 5.0}
                      ]
                    }""",
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
                else -> respondError(HttpStatusCode.NotFound)
            }
        }

        val api = SofaApiClient(baseUrl = "https://mock.example.com", client = client)
        val response: PlayerPercentilesResponse = api.getPlayerPercentiles(42, "Napastnik", 300)

        assertEquals("Jan Kowalski", response.playerName)
        assertEquals("Napastnik", response.position)
        assertEquals(85, response.groupSize)
        assertEquals(2000.0, response.minutes)

        assertEquals(2, response.radarChart.size)
        assertEquals("goals", response.radarChart[0].statKey)
        assertEquals(92, response.radarChart[0].percentile)
        assertEquals(12.0, response.radarChart[0].value)
        assertEquals(5.0, response.radarChart[0].median)

        assertEquals(1, response.fullTable.size)
        assertEquals(0.54, response.fullTable[0].p90Value)
    }

    @Test
    fun `getPlayerPercentiles should send correct query parameters`() = runTest {
        var capturedTemplate: String? = null
        var capturedMinMinutes: String? = null

        val client = buildMockClient { request ->
            capturedTemplate = request.url.parameters["template"]
            capturedMinMinutes = request.url.parameters["min_minutes"]
            respond(
                content = """
                {
                  "playerName": "X",
                  "position": "CB",
                  "groupSize": 10,
                  "minutes": 500.0,
                  "radarChart": [],
                  "fullTable": []
                }""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val api = SofaApiClient(baseUrl = "https://mock.example.com", client = client)
        api.getPlayerPercentiles(7, template = "CB", minMinutes = 500)

        assertEquals("CB", capturedTemplate)
        assertEquals("500", capturedMinMinutes)
    }

    @Test
    fun `getPlayerPercentiles should throw on server error`() = runTest {
        val client = buildMockClient { _ -> respondError(HttpStatusCode.InternalServerError) }

        val api = SofaApiClient(baseUrl = "https://mock.example.com", client = client)
        assertFailsWith<Exception> { api.getPlayerPercentiles(1) }
    }
}
