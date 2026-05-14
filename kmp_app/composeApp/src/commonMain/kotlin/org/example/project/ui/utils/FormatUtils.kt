package org.example.project.ui.utils

import kotlin.math.roundToInt

fun Double.toFormattedString(): String {
    val isWhole = this % 1.0 == 0.0
    if (isWhole) return this.toLong().toString()
    
    val str = this.toString()
    val dotIndex = str.indexOf('.')
    if (dotIndex == -1) return str
    
    val endIndex = minOf(str.length, dotIndex + 3) // +3 bo kropka to 1 znak i 2 po przecinku
    var result = str.substring(0, endIndex)
    
    // Ucinamy końcowe zera i kropkę jeśli trzeba
    result = result.trimEnd('0')
    if (result.endsWith(".")) {
        result = result.dropLast(1)
    }
    return result
}

// Statystyki z odwrotną logiką
val LOWER_IS_BETTER_STATS = setOf(
    "bigChanceMissed", "errorLeadToAShot", "fouls", "unsuccessfulTouch", 
    "possessionLostCtrl", "goalsConceded", "crossNotClaimed", 
    "dispossessed", "duelLost", "aerialLost", "challengeLost"
)

val STATS_CATEGORIES = mapOf(
    "Ogólne" to listOf("rating", "minutesPlayed"),
    "Bramki i xG" to listOf("goals", "expectedGoals", "G-xG", "xGOT", "xGOT-xG", "G-xGOT"),
    "Bramkarskie" to listOf("saves", "goalsConceded", "goalsPrevented", "cleanSheet", "xGA", "xGOTA", "savedShotsFromInsideTheBox", "crossNotClaimed", "goodHighClaim", "totalKeeperSweeper", "accurateKeeperSweeper"),
    "Strzały" to listOf("totalShots", "onTargetScoringAttempt", "shotOffTarget", "blockedScoringAttempt", "hitWoodwork", "bigChanceMissed"),
    "Podania i Kreacja" to listOf("goalAssist", "assists", "expectedAssists", "keyPass", "bigChanceCreated", "touches", "totalPass", "accuratePass", "accuratePassesPercentage", "totalLongBalls", "accurateLongBalls", "accurateLongBallsPercentage", "totalCross", "accurateCross", "accurateCrossesPercentage", "accurateOppositionHalfPasses", "totalOppositionHalfPasses", "accurateOppositionHalfPassesPercentage", "accurateOwnHalfPasses","totalOwnHalfPasses", "accurateOwnHalfPassesPercentage"),
    "Drybling i Pojedynki" to listOf("totalContest", "wonContest", "wonContestPercentage", "duelWon", "duelLost", "groundDuelsWonPercentage", "aerialWon", "aerialLost", "aerialDuelsWonPercentage", "wasFouled", "fouls", "dispossessed", "unsuccessfulTouch", "possessionLostCtrl"),
    "Defensywa" to listOf("totalTackle", "wonTackle", "wonTacklePercentage", "interceptionWon", "ballRecovery", "totalClearance", "challengeLost", "errorLeadToAShot", "penaltyConceded")
)

// Pełna mapa ze Streamlita
val STATS_PL_MAP = mapOf(
    "rating" to "Ocena",
    "goals" to "Gole",
    "expectedGoals" to "xG",
    "G-xG" to "Gole - xG (G-xG)",
    "xGOT-xG" to "Jakość Strzałów (xGOT-xG)", 
    "G-xGOT" to "Wykończenie vs Bramkarz (G-xGOT)",
    "goalAssist" to "Asysty",
    "assists" to "Asysty",
    "expectedAssists" to "xA",
    "minutesPlayed" to "Minuty",
    "totalSeasonMinutes_stats" to "Minuty",
    "appearances" to "Mecze",
    "xGOT" to "xGOT",
    "totalPass" to "Podania (Razem)",
    "accuratePass" to "Podania Celne",
    "accuratePassesPercentage" to "Celność Podań %",
    "keyPass" to "Kluczowe Podania",
    "totalLongBalls" to "Długie Piłki",
    "accurateLongBalls" to "Celne Długie Piłki",
    "accurateLongBallsPercentage" to "Celność Długich %",
    "totalCross" to "Dośrodkowania",
    "accurateCross" to "Celne Dośrodkowania",
    "accurateCrossesPercentage" to "Celność Dośrodkowań %",
    "touches" to "Kontakty z piłką",
    "accurateOppositionHalfPasses" to "Celne na poł. rywala",
    "totalOppositionHalfPasses" to "Wszystkie na poł. rywala",
    "accurateOppositionHalfPassesPercentage" to "Celność na poł. rywala %",
    "accurateOwnHalfPasses" to "Celne na wł. połowie",
    "totalOwnHalfPasses" to "Wszystkie na wł. połowie",
    "accurateOwnHalfPassesPercentage" to "Celność na wł. połowie %",
    "totalShots" to "Strzały",
    "onTargetScoringAttempt" to "Celne Strzały",
    "shotOffTarget" to "Niecelne Strzały",
    "blockedScoringAttempt" to "Zablokowane Strzały",
    "bigChanceMissed" to "Zmarnowane Setki",
    "bigChanceCreated" to "Stworzone Setki",
    "hitWoodwork" to "Słupki/Poprzeczki",
    "totalContest" to "Próby Dryblingu",
    "wonContest" to "Udane Dryblingi",
    "wonContestPercentage" to "Skuteczność Dryblingu %",
    "duelWon" to "Wygrane Pojedynki",
    "duelLost" to "Przegrane Pojedynki",
    "groundDuelsWonPercentage" to "Wygrane Pojedynki %",
    "aerialWon" to "Wygrane Główki",
    "aerialLost" to "Przegrane Główki",
    "aerialDuelsWonPercentage" to "Wygrane Główki %",
    "wasFouled" to "Faulowany",
    "fouls" to "Faule",
    "dispossessed" to "Strata (Odbiór rywala)",
    "unsuccessfulTouch" to "Złe przyjęcie",
    "totalTackle" to "Próby Odbioru",
    "wonTackle" to "Udane Odbiory",
    "wonTacklePercentage" to "Skuteczność Odbioru %",
    "interceptionWon" to "Przechwyty",
    "ballRecovery" to "Odzyskanie Piłki",
    "totalClearance" to "Wybicia",
    "challengeLost" to "Ograny (Drybling)",
    "errorLeadToAShot" to "Błąd do strzału",
    "possessionLostCtrl" to "Strata Piłki",
    "outfielderBlock" to "Zablokowane (Pole)",
    "saves" to "Obrony",
    "savedShotsFromInsideTheBox" to "Obrony z pola karnego",
    "goalsConceded" to "Wpuszczone Gole",
    "xGA" to "xG Przeciwnika (xGA)",
    "xGOTA" to "xGOT Przeciwnika (xGOTA)",
    "goalsPrevented" to "Uratowane Gole (xGOTA - Gole)",
    "cleanSheet" to "Czyste Konto",
    "crossNotClaimed" to "Nieudane wyjście do dośrodkowania",
    "goodHighClaim" to "Udane wyjście do dośrodkowania",
    "totalKeeperSweeper" to "Wyjścia poza p. karne (Keeper-Sweeper)",
    "accurateKeeperSweeper" to "Udane wyjścia poza p. karne"
)

fun mapStatKeyToPolish(key: String): String {
    return STATS_PL_MAP[key] ?: key.replace("Percentage", " [%]").replace(Regex("([a-z])([A-Z]+)"), "$1 $2").replaceFirstChar { it.uppercase() }
}
