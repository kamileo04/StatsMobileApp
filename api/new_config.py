# new_config.py
import numpy as np

# --- USTAWIENIA OGÓLNE ---
CONFIG_FILE = "config.json"
LEAGUE_NAME = "II Liga"
DATA_DIR = "sofascore_data"

# --- USTAWIENIA BOISKA I KOLORÓW ---
PITCH_LENGTH = 105
PITCH_WIDTH = 68
PITCH_COLOR = '#fafafa'
LINE_COLOR = '#606060'
HOME_COLOR = '#d31a1a'
AWAY_COLOR = '#1a69d3'
GOAL_COLOR = '#1ad369'

# --- USTAWIENIA HEATMAPY ---
MIN_HEATMAP_POINTS = 5
MINUTES_THRESHOLD_FOR_ALPHA = 45
LOW_ALPHA = 0.4
HIGH_ALPHA = 0.9

# --- SZABLONY WYKRESÓW (PIZZA CHARTS) ---
PIZZA_CHART_TEMPLATES = {
    "ST": [
        "goals", "expectedGoals", "G-xG", "xGOT-xG", 
        "G-xGOT", "bigChanceMissed", "totalShots", "keyPass", 
        "wonContestPercentage", "wonContest", "aerialWon", "aerialDuelsWonPercentage", "ballRecovery"
    ],
    "W": [
        "goals", "goalAssist", "expectedGoals", "G-xG", "xGOT-xG",
        "keyPass", "bigChanceCreated", "accurateCrossesPercentage", "accurateCross",
        "wonContest", "wasFouled", "ballRecovery"
    ],
    "CAM": [
        "goals", "goalAssist", "expectedGoals", "bigChanceCreated", "keyPass", "touches",
        "accurateOppositionHalfPassesPercentage", "wonContestPercentage", "wasFouled", "ballRecovery"
    ],
    "CM/CDM": [
        "goalAssist", "goals", "expectedGoals", "bigChanceCreated", "touches", "keyPass",
        "accuratePassesPercentage", "accuratePass", "accurateOppositionHalfPasses","accurateOppositionHalfPassesPercentage", "ballRecovery", 
        "wonTackle", "interceptionWon", "errorLeadToAShot"
    ],
    "LB": [
        "keyPass", "accuratePassesPercentage", "accurateCross", "accurateCrossesPercentage", 
        "wonContest", "wonContestPercentage", "wonTackle", "interceptionWon", "ballRecovery", 
        "errorLeadToAShot", "fouls"
    ],
    "RB": [
        "keyPass", "accuratePassesPercentage", "accurateCross", "accurateCrossesPercentage", 
        "wonContest", "wonContestPercentage", "wonTackle", "interceptionWon", "ballRecovery", 
        "errorLeadToAShot", "fouls"
    ],
    "CB": [
        "accuratePassesPercentage", "totalLongBalls", "accurateLongBallsPercentage",
        "aerialWon", "aerialDuelsWonPercentage", "wonTackle", "interceptionWon",
        "totalClearance", "ballRecovery", "fouls", "errorLeadToAShot"
    ],
    "GK": [
        "saves", "goalsConceded", "cleanSheet", "goalsPrevented",
        "xGA", "xGOTA", "savedShotsFromInsideTheBox",
        "accurateLongBalls", "accuratePass", "totalKeeperSweeper",
        "goodHighClaim", "crossNotClaimed"
    ]
}

# Statystyki, gdzie mniej znaczy lepiej (do odwracania na wykresach)
LOWER_IS_BETTER_STATS = ["bigChanceMissed", "errorLeadToAShot", "fouls", "unsuccessfulTouch", "possessionLostCtrl","goalsConceded","crossNotClaimed", "dispossessed", "duelLost", "aerialLost", "challengeLost"]

STATS_PL_MAP = {
    # --- Główne ---
    "rating": "Ocena",
    "goals": "Gole",
    "expectedGoals": "xG",
    "G-xG":"Gole - xG (G-xG)",
    "xGOT-xG":"Jakość Strzałów (xGOT-xG)", 
    "G-xGOT":"Wykończenie vs Bramkarz (G-xGOT)",
    "goalAssist": "Asysty",
    "assists": "Asysty",
    "expectedAssists": "xA",
    "minutesPlayed": "Minuty",
    "totalSeasonMinutes_stats": "Minuty",
    "appearances": "Mecze",
    "xGOT": "xGOT",
    
    # --- Podania (Nazwy zgodne z run_build_database) ---
    "totalPass": "Podania (Razem)",
    "accuratePass": "Podania Celne",
    "accuratePassesPercentage": "Celność Podań %", # Plural
    
    "keyPass": "Kluczowe Podania",
    
    "totalLongBalls": "Długie Piłki",
    "accurateLongBalls": "Celne Długie Piłki",
    "accurateLongBallsPercentage": "Celność Długich %",
    
    "totalCross": "Dośrodkowania",
    "accurateCross": "Celne Dośrodkowania",
    "accurateCrossesPercentage": "Celność Dośrodkowań %", # Plural
    
    "touches": "Kontakty z piłką",
    
    # --- Strefowe ---
    "accurateOppositionHalfPasses": "Celne na poł. rywala",
    "totalOppositionHalfPasses": "Wszystkie na poł. rywala",
    "accurateOppositionHalfPassesPercentage": "Celność na poł. rywala %",
    
    "accurateOwnHalfPasses": "Celne na wł. połowie",
    "totalOwnHalfPasses": "Wszystkie na wł. połowie",
    "accurateOwnHalfPassesPercentage": "Celność na wł. połowie %",
    
    # --- Strzały ---
    "totalShots": "Strzały",
    "onTargetScoringAttempt": "Celne Strzały",
    "shotOffTarget": "Niecelne Strzały",
    "blockedScoringAttempt": "Zablokowane Strzały",
    "bigChanceMissed": "Zmarnowane Setki",
    "bigChanceCreated": "Stworzone Setki",
    "hitWoodwork": "Słupki/Poprzeczki",
    
    # --- Dryblingi i Pojedynki ---
    "totalContest": "Próby Dryblingu",
    "wonContest": "Udane Dryblingi",
    "wonContestPercentage": "Skuteczność Dryblingu %",
    "duelWon": "Wygrane Pojedynki",
    "duelLost": "Przegrane Pojedynki",
    "groundDuelsWonPercentage": "Wygrane Pojedynki %",
    "aerialWon": "Wygrane Główki",
    "aerialLost": "Przegrane Główki",
    "aerialDuelsWonPercentage": "Wygrane Główki %",
    "wasFouled": "Faulowany",
    "fouls": "Faule",
    "dispossessed": "Strata (Odbiór rywala)",
    "unsuccessfulTouch": "Złe przyjęcie",
    
    # --- Defensywa ---
    "totalTackle": "Próby Odbioru",
    "wonTackle": "Udane Odbiory",
    "wonTacklePercentage": "Skuteczność Odbioru %",
    "interceptionWon": "Przechwyty",
    "ballRecovery": "Odzyskanie Piłki",
    "totalClearance": "Wybicia",
    "challengeLost": "Ograny (Drybling)",
    "errorLeadToAShot": "Błąd do strzału",
    "possessionLostCtrl": "Strata Piłki",
    "outfielderBlock": "Zablokowane (Pole)",
    #GK
    "saves": "Obrony",
    "savedShotsFromInsideTheBox": "Obrony z pola karnego",
    "goalsConceded": "Wpuszczone Gole",
    "xGA": "xG Przeciwnika (xGA)",
    "xGOTA": "xGOT Przeciwnika (xGOTA)",
    "goalsPrevented": "Uratowane Gole (xGOTA - Gole)",
    "cleanSheet": "Czyste Konto",
    "crossNotClaimed": "Nieudane wyjście do dośrodkowania",
    "goodHighClaim": "Udane wyjście do dośrodkowania",
    "totalKeeperSweeper": "Wyjścia poza pole karne (Keeper-Sweeper)",
    "accurateKeeperSweeper": "Udane wyjścia poza pole karne",
}
STATS_CATEGORIES = {
    "📌 Ogólne": [
        "rating", "appearances", "minutesPlayed"
    ],
    "⚽ Bramki i xG": [
        "goals", "expectedGoals", "G-xG", "xGOT", "xGOT-xG", "G-xGOT"
    ],
    "🥅 Bramkarskie": [
        "saves", "goalsConceded", "goalsPrevented", "cleanSheet", 
        "xGA", "xGOTA", "savedShotsFromInsideTheBox", 
        "crossNotClaimed", "goodHighClaim", "totalKeeperSweeper", "accurateKeeperSweeper"
    ],
    "🎯 Strzały": [
        "totalShots", "onTargetScoringAttempt", "shotOffTarget", "blockedScoringAttempt", 
        "hitWoodwork", "bigChanceMissed"
    ],
    "🅰️ Podania i Kreacja": [
        "goalAssist", "expectedAssists", "keyPass", "bigChanceCreated", "touches",
        "totalPass", "accuratePass", "accuratePassesPercentage",
        "totalLongBalls", "accurateLongBalls", "accurateLongBallsPercentage",
        "totalCross", "accurateCross", "accurateCrossesPercentage",
        "accurateOppositionHalfPasses",  "totalOppositionHalfPasses",  
        "accurateOppositionHalfPassesPercentage", "accurateOwnHalfPasses","totalOwnHalfPasses",
        "accurateOwnHalfPassesPercentage",
    ],
    "⚡ Drybling i Pojedynki": [
        "totalContest", "wonContest", "wonContestPercentage",
        "duelWon", "duelLost", "groundDuelsWonPercentage",
        "aerialWon", "aerialLost", "aerialDuelsWonPercentage",
        "wasFouled", "fouls", "dispossessed", "unsuccessfulTouch",
        "possessionLostCtrl"
    ],
    "🛡️ Defensywa": [
        "totalTackle", "wonTackle", "wonTacklePercentage",
        "interceptionWon", "ballRecovery", "totalClearance", "challengeLost", 
        "errorLeadToAShot", "penaltyConceded"
    ]
}
RANKING_ROLES = {
    # --- NAPASTNICY (ST) ---
    "Napastnik - Lis Pola Karnego (Finisher)": {
        "goals": 2.0,
        "expectedGoals": 1.0,
        "G-xG": 2.0,
        "xGOT-xG": 1.5,
        "aerialWon": 1.5,
        "aerialDuelsWonPercentage": 1.0,
        "bigChanceMissed": 1.0,   
        "ballRecovery":1.0 
    },
    "Napastnik - Odgrywający (Target Man)": {
        "aerialWon": 3.0,
        "aerialDuelsWonPercentage": 2.0,
        "expectedAssists": 1.0,
        "keyPass": 1.5,
        "goalAssist": 1.5,
        "goals": 1.5,
        "expectedGoals": 1.0,
        "G-xG": 2.0,
        "xGOT-xG": 1.5,
        "wasFouled": 1.5,   
        "ballRecovery":1.0           
    },
    "Napastnik - Cofnięty": {
        "goals": 1.0,
        "expectedGoals": 0.5,
        "G-xG": 1.5,
        "xGOT-xG": 1.0,
        "goalAssist": 2.0,
        "expectedAssists": 2.0,
        "keyPass": 2.0,
        "wonContest": 1.5,
        "accurateOppositionHalfPasses": 1.5,
        "bigChanceCreated": 1.0,   
        "ballRecovery":1.0
    },
    "Napastnik - Kompletny": {
        "aerialWon": 1.0,
        "aerialDuelsWonPercentage": 1.0,
        "goals": 2.0,
        "expectedGoals": 1.0,
        "G-xG": 1.5,
        "xGOT-xG": 1.5,
        "goalAssist": 1.0,
        "expectedAssists": 1.0,
        "keyPass": 1.0,
        "wonContest": 1.0,
        "accurateOppositionHalfPasses": 1.0,
        "bigChanceCreated": 0.5,
        "wasFouled": 1.0,   
        "ballRecovery":1.0
        },

    # --- SKRZYDŁOWI (W) ---
    "Skrzydłowy - Klasyczny": {
        "accurateCross": 2.0,
        "accurateCrossesPercentage": 2.0,
        "keyPass": 2.0,
        "wonContest": 2.5,
        "wonContestPercentage": 1.5,
        "expectedAssists": 1.5,
        "goalAssist": 1.5,
        "wasFouled": 2.0,
        "goals": 1.0,
        "expectedGoals": 1.0,
        
    },
    "Skrzydłowy - Odwrócony": {
        "accurateCross": 1.0,
        "accurateCrossesPercentage": 1.0,
        "keyPass": 2.0,
        "wonContest": 3.0,
        "wonContestPercentage": 2.0,
        "expectedAssists": 2.0,
        "goalAssist": 2.0,
        "wasFouled": 2.0,
        "goals": 1.5,
        "expectedGoals": 1.5,
        
    },

    # --- OFENSYWNI POMOCNICY (CAM) ---
    "Ofensywny Pomocnik - Kreator (Playmaker)": {
        "touches": 2.5,
        "keyPass": 2.0,
        "wonContest": 2.0,
        "wonContestPercentage": 1.0,
        "expectedAssists": 2.0,
        "goalAssist": 2.0,
        "wasFouled": 1.0,
        "goals": 1.0,
        "expectedGoals": 1.0,
    },
    "Ofensywny Pomocnik - Cień (Shadow Striker)": {
        "touches": 1.0,
        "keyPass": 2.0,
        "wonContest": 1.5,
        "wonContestPercentage": 1.0,
        "expectedAssists": 2.0,
        "goalAssist": 1.5,
        "wasFouled": 1.0,
        "goals": 2.0,
        "expectedGoals": 2.0,
    },

    # --- ŚRODKOWI POMOCNICY (CM/CDM) ---
    "Środkowy Pomocnik - Box-to-Box (B2B)": {
        "accuratePass": 1.5,
        "accuratePassesPercentage": 2.0,
        "goals": 1.0,
        "totalTackle": 1.5,
        "interceptionWon": 1.5,
        "wonContest": 1.0,
        "touches": 2.0,             # Musi być pod grą
        "ballRecovery": 1.5,
        "accurateLongBalls": 1.0
    },
    "Środkowy Pomocnik - Rozgrywający (Deep-Lying Playmaker)": {
        "accuratePass": 2.5,
        "accuratePassesPercentage": 2.0,
        "accurateLongBalls": 2.5,
        "accurateLongBallsPercentage": 2.0,
        "accurateOppositionHalfPasses": 2.0,
        "accurateOppositionHalfPassesPercentage": 2.0,
        "keyPass": 1.5,
        "interceptionWon": 1.0      # Pozycjonowanie
    },
    "Środkowy Pomocnik - Defensywny (Ball Winner)": {
        "wonTackle": 3.0,
        "interceptionWon": 3.0,
        "ballRecovery": 2.5,
        "duelWon": 2.0,
        "groundDuelsWonPercentage": 1.5,
        "totalClearance": 1.0,
        "wasFouled": 1.0,
        "accuratePass": 1.5,
        "accuratePassesPercentage": 1.5,        # Mniej ważne, ma odbierać
    },

    # --- BOCZNI OBROŃCY (LB/RB) ---
    "Boczny Obrońca - Ofensywny (Wing Back)": {
        "accurateCross": 2.0,
        "accurateCrossesPercentage": 2.0,
        "keyPass": 1.0,
        "wonContest": 2.5,
        "wonContestPercentage": 1.5,
        "expectedAssists": 1.0,
        "goalAssist": 1.0,
        "wasFouled": 1.5,
        "wonTackle": 1.5,
        "interceptionWon": 1.5,
        "duelWon": 2.0,
        "totalClearance": 1.0,
        "possessionLostCtrl": 1.5,  # Ważne żeby nie tracił
        "aerialWon": 1.0,
        "errorLeadToAShot": 2.5
        
    },
    "Boczny Obrońca - Defensywny": {
        "accurateCross": 1.0,
        "accurateCrossesPercentage": 1.0,
        "keyPass": 0.5,
        "wonContest": 1.5,
        "wonContestPercentage": 1.0,
        "expectedAssists": 1.0,
        "goalAssist": 1.0,
        "wasFouled": 1.0,
        "wonTackle": 2.5,
        "interceptionWon": 2.5,
        "duelWon": 3.0,
        "totalClearance": 2.0,
        "possessionLostCtrl": 1.0,  # Ważne żeby nie tracił
        "aerialWon": 2.0,
        "errorLeadToAShot": 2.5      # Unikać błędów"
    },

    # --- ŚRODKOWI OBROŃCY (CB) ---
    "Środkowy Obrońca - Klasyczny Stoper": {
        "accuratePass": 1.5,
        "accuratePassesPercentage": 1.5,
        "wonTackle": 2.5,
        "interceptionWon": 2.5,
        "duelWon": 2.0,
        "totalClearance": 2.0,
        "possessionLostCtrl": 2.0,  # Ważne żeby nie tracił
        "aerialWon": 2.0,
        "aerialDuelsWonPercentage": 2.0,
        "outfielderBlock": 2.5,     # Zablokowane strzały
        "errorLeadToAShot": 2.5      # Unikać błędów"
    },
    "Środkowy Obrońca - Grający Piłką (Ball Playing CB)": {
        "accuratePass": 3.0,
        "accuratePassesPercentage": 3.0,
        "accurateLongBalls": 1.5,
        "accurateLongBallsPercentage": 1.5,
        "wonContest": 1.0,
        "wonContestPercentage": 1.0,
        "interceptionWon": 2.0,
        "wonTackle": 2.0,
        "totalPass": 2.0,
        "aerialWon": 2.0,
        "aerialDuelsWonPercentage": 2.0,
        "errorLeadToAShot": 2.5,
        "outfielderBlock": 1.5, 
        "touches": 1.5
    },

    # --- BRAMKARZE (GK) ---
    "Bramkarz - Klasyczny (Shot Stopper)": {
        "goalsPrevented": 3.0,      # xGOTA - Gole
        "saves": 2.5,
        "savedShotsFromInsideTheBox": 2.0,
        "cleanSheet": 1.5,
        "goalsConceded": 1.5
    },
    "Bramkarz - Nowoczesny (Sweeper Keeper)": {
        "goodHighClaim": 2.0,
        "totalKeeperSweeper": 2.0,
        "accuratePass": 2.0,
        "accurateLongBalls": 2.0,
        "goalsPrevented": 2.5
    }
}
# new_config.py

class S:
    """
    Kompletny słownik statystyk ("S").
    Zawiera WSZYSTKIE kolumny generowane przez loader 'Ultimate'.
    Używaj: S.nazwa_zmiennej (np. S.gole_suma lub S.rywal_gole_suma).
    """

    # =========================================================================
    # 🟢 1. GŁÓWNE I BRAMKI
    # =========================================================================
    ocena_srednia = "Ocena (Średnia)"
    rywal_ocena_srednia = "Rywal - Ocena (Średnia)"

    mecze = "Mecze"
    minuty_suma = "Minuty (Suma)"

    gole_suma = "Gole (Suma)"
    gole_srednia = "Gole (Średnia)"
    rywal_gole_suma = "Rywal - Gole (Suma)"
    rywal_gole_srednia = "Rywal - Gole (Średnia)"

    xg_suma = "xG (Suma)"
    xg_srednia = "xG (Średnia)"
    rywal_xg_suma = "Rywal - xG (Suma)"
    rywal_xg_srednia = "Rywal - xG (Średnia)"

    asysty_suma = "Asysty (Suma)"
    asysty_srednia = "Asysty (Średnia)"
    rywal_asysty_suma = "Rywal - Asysty (Suma)"
    rywal_asysty_srednia = "Rywal - Asysty (Średnia)"

    xa_suma = "xA (Suma)"
    xa_srednia = "xA (Średnia)"
    rywal_xa_suma = "Rywal - xA (Suma)"
    rywal_xa_srednia = "Rywal - xA (Średnia)"

    # Metryki zaawansowane (Delta)
    g_minus_xg_suma = "Gole - xG (G-xG) (Suma)"
    rywal_g_minus_xg_suma = "Rywal - Gole - xG (G-xG) (Suma)"
    
    xgot_suma = "xGOT (Suma)"
    rywal_xgot_suma = "Rywal - xGOT (Suma)"

    # =========================================================================
    # 🔵 2. PODANIA I KREACJA
    # =========================================================================
    podania_razem_suma = "Podania (Razem) (Suma)"
    podania_razem_srednia = "Podania (Razem) (Średnia)"
    rywal_podania_razem_suma = "Rywal - Podania (Razem) (Suma)"
    rywal_podania_razem_srednia = "Rywal - Podania (Razem) (Średnia)"

    podania_celne_suma = "Podania Celne (Suma)"
    podania_celne_srednia = "Podania Celne (Średnia)"
    rywal_podania_celne_suma = "Rywal - Podania Celne (Suma)"
    rywal_podania_celne_srednia = "Rywal - Podania Celne (Średnia)"
    
    # Procent (Wyliczany na końcu, więc nie ma "Suma" w nazwie)
    podania_celnosc_proc = "Celność Podań %"
    rywal_podania_celnosc_proc = "Rywal - Celność Podań %"

    kluczowe_podania_suma = "Kluczowe Podania (Suma)"
    kluczowe_podania_srednia = "Kluczowe Podania (Średnia)"
    rywal_kluczowe_podania_suma = "Rywal - Kluczowe Podania (Suma)"
    rywal_kluczowe_podania_srednia = "Rywal - Kluczowe Podania (Średnia)"

    dlugie_pilki_suma = "Długie Piłki (Suma)"
    dlugie_pilki_srednia = "Długie Piłki (Średnia)"
    rywal_dlugie_pilki_suma = "Rywal - Długie Piłki (Suma)"
    rywal_dlugie_pilki_srednia = "Rywal - Długie Piłki (Średnia)"

    dlugie_pilki_celne_suma = "Celne Długie Piłki (Suma)"
    rywal_dlugie_pilki_celne_suma = "Rywal - Celne Długie Piłki (Suma)"

    dosrodkowania_suma = "Dośrodkowania (Suma)"
    dosrodkowania_srednia = "Dośrodkowania (Średnia)"
    rywal_dosrodkowania_suma = "Rywal - Dośrodkowania (Suma)"
    rywal_dosrodkowania_srednia = "Rywal - Dośrodkowania (Średnia)"

    dosrodkowania_celne_suma = "Celne Dośrodkowania (Suma)"
    rywal_dosrodkowania_celne_suma = "Rywal - Celne Dośrodkowania (Suma)"

    kontakty_suma = "Kontakty z piłką (Suma)"
    kontakty_srednia = "Kontakty z piłką (Średnia)"
    rywal_kontakty_suma = "Rywal - Kontakty z piłką (Suma)"
    rywal_kontakty_srednia = "Rywal - Kontakty z piłką (Średnia)"

    # Strefowe
    podania_pol_rywala_suma = "Wszystkie na poł. rywala (Suma)"
    podania_pol_rywala_celne_suma = "Celne na poł. rywala (Suma)"
    rywal_podania_pol_rywala_suma = "Rywal - Wszystkie na poł. rywala (Suma)"
    rywal_podania_pol_rywala_celne_suma = "Rywal - Celne na poł. rywala (Suma)"

    podania_wlasna_pol_suma = "Wszystkie na wł. połowie (Suma)"
    podania_wlasna_pol_celne_suma = "Celne na wł. połowie (Suma)"
    rywal_podania_wlasna_pol_suma = "Rywal - Wszystkie na wł. połowie (Suma)"
    rywal_podania_wlasna_pol_celne_suma = "Rywal - Celne na wł. połowie (Suma)"

    # =========================================================================
    # 🎯 3. STRZAŁY
    # =========================================================================
    strzaly_suma = "Strzały (Suma)"
    strzaly_srednia = "Strzały (Średnia)"
    rywal_strzaly_suma = "Rywal - Strzały (Suma)"
    rywal_strzaly_srednia = "Rywal - Strzały (Średnia)"

    strzaly_celne_suma = "Celne Strzały (Suma)"
    strzaly_celne_srednia = "Celne Strzały (Średnia)"
    rywal_strzaly_celne_suma = "Rywal - Celne Strzały (Suma)"
    rywal_strzaly_celne_srednia = "Rywal - Celne Strzały (Średnia)"

    strzaly_niecelne_suma = "Niecelne Strzały (Suma)"
    rywal_strzaly_niecelne_suma = "Rywal - Niecelne Strzały (Suma)"

    strzaly_zablokowane_suma = "Zablokowane Strzały (Suma)" # Nasze strzały zablokowane przez kogoś
    rywal_strzaly_zablokowane_suma = "Rywal - Zablokowane Strzały (Suma)" # Strzały rywala zablokowane przez nas

    slupki_suma = "Słupki/Poprzeczki (Suma)"
    rywal_slupki_suma = "Rywal - Słupki/Poprzeczki (Suma)"

    setki_stworzone_suma = "Stworzone Setki (Suma)"
    setki_stworzone_srednia = "Stworzone Setki (Średnia)"
    rywal_setki_stworzone_suma = "Rywal - Stworzone Setki (Suma)"
    rywal_setki_stworzone_srednia = "Rywal - Stworzone Setki (Średnia)"

    setki_zmarnowane_suma = "Zmarnowane Setki (Suma)"
    rywal_setki_zmarnowane_suma = "Rywal - Zmarnowane Setki (Suma)"

    # =========================================================================
    # ⚡ 4. DRYBLING, POJEDYNKI I POSIADANIE
    # =========================================================================
    dryblingi_proby_suma = "Próby Dryblingu (Suma)"
    dryblingi_proby_srednia = "Próby Dryblingu (Średnia)"
    rywal_dryblingi_proby_suma = "Rywal - Próby Dryblingu (Suma)"
    rywal_dryblingi_proby_srednia = "Rywal - Próby Dryblingu (Średnia)"

    dryblingi_udane_suma = "Udane Dryblingi (Suma)"
    dryblingi_udane_srednia = "Udane Dryblingi (Średnia)"
    rywal_dryblingi_udane_suma = "Rywal - Udane Dryblingi (Suma)"
    rywal_dryblingi_udane_srednia = "Rywal - Udane Dryblingi (Średnia)"

    pojedynki_wygrane_suma = "Wygrane Pojedynki (Suma)"
    pojedynki_przegrane_suma = "Przegrane Pojedynki (Suma)"
    rywal_pojedynki_wygrane_suma = "Rywal - Wygrane Pojedynki (Suma)"
    rywal_pojedynki_przegrane_suma = "Rywal - Przegrane Pojedynki (Suma)"

    glowki_wygrane_suma = "Wygrane Główki (Suma)"
    glowki_wygrane_srednia = "Wygrane Główki (Średnia)"
    rywal_glowki_wygrane_suma = "Rywal - Wygrane Główki (Suma)"
    rywal_glowki_wygrane_srednia = "Rywal - Wygrane Główki (Średnia)"

    glowki_przegrane_suma = "Przegrane Główki (Suma)"
    rywal_glowki_przegrane_suma = "Rywal - Przegrane Główki (Suma)"

    faule_suma = "Faule (Suma)" # My faulujemy
    faule_srednia = "Faule (Średnia)"
    rywal_faule_suma = "Rywal - Faule (Suma)" # Rywal fauluje
    rywal_faule_srednia = "Rywal - Faule (Średnia)"

    faulowany_suma = "Faulowany (Suma)" # My jesteśmy faulowani
    rywal_faulowany_suma = "Rywal - Faulowany (Suma)"

    straty_suma = "Strata Piłki (Suma)" # Possession Lost Ctrl
    rywal_straty_suma = "Rywal - Strata Piłki (Suma)"
    
    zle_przyjecie_suma = "Złe przyjęcie (Suma)"
    rywal_zle_przyjecie_suma = "Rywal - Złe przyjęcie (Suma)"
    
    strata_odbior_rywala_suma = "Strata (Odbiór rywala) (Suma)" # Dispossessed
    rywal_strata_odbior_rywala_suma = "Rywal - Strata (Odbiór rywala) (Suma)"

    # =========================================================================
    # 🛡️ 5. DEFENSYWA
    # =========================================================================
    odbiory_proby_suma = "Próby Odbioru (Suma)"
    odbiory_proby_srednia = "Próby Odbioru (Średnia)"
    rywal_odbiory_proby_suma = "Rywal - Próby Odbioru (Suma)"
    rywal_odbiory_proby_srednia = "Rywal - Próby Odbioru (Średnia)"

    odbiory_udane_suma = "Udane Odbiory (Suma)"
    odbiory_udane_srednia = "Udane Odbiory (Średnia)"
    rywal_odbiory_udane_suma = "Rywal - Udane Odbiory (Suma)"
    rywal_odbiory_udane_srednia = "Rywal - Udane Odbiory (Średnia)"

    przechwyty_suma = "Przechwyty (Suma)"
    przechwyty_srednia = "Przechwyty (Średnia)"
    rywal_przechwyty_suma = "Rywal - Przechwyty (Suma)"
    rywal_przechwyty_srednia = "Rywal - Przechwyty (Średnia)"

    odzyskanie_pilki_suma = "Odzyskanie Piłki (Suma)" # Ball Recovery
    odzyskanie_pilki_srednia = "Odzyskanie Piłki (Średnia)"
    rywal_odzyskanie_pilki_suma = "Rywal - Odzyskanie Piłki (Suma)"
    rywal_odzyskanie_pilki_srednia = "Rywal - Odzyskanie Piłki (Średnia)"

    wybicia_suma = "Wybicia (Suma)"
    wybicia_srednia = "Wybicia (Średnia)"
    rywal_wybicia_suma = "Rywal - Wybicia (Suma)"
    rywal_wybicia_srednia = "Rywal - Wybicia (Średnia)"

    # "Ograny" (Challenge Lost) - ile razy obrońca został minięty
    ograny_suma = "Ograny (Drybling) (Suma)"
    rywal_ograny_suma = "Rywal - Ograny (Drybling) (Suma)"

    bloki_nasze_suma = "Zablokowane (Pole) (Suma)" # My blokujemy
    rywal_bloki_nasze_suma = "Rywal - Zablokowane (Pole) (Suma)" # Rywal blokuje

    bledy_do_strzalu_suma = "Błąd do strzału (Suma)"
    rywal_bledy_do_strzalu_suma = "Rywal - Błąd do strzału (Suma)"

    # =========================================================================
    # 🥅 6. BRAMKARZ I ZESPOŁOWE
    # =========================================================================
    posiadanie_srednia = "Posiadanie Piłki (Średnia)"
    rywal_posiadanie_srednia = "Rywal - Posiadanie Piłki (Średnia)"

    rozne_suma = "Rzuty Rożne (Suma)"
    rozne_srednia = "Rzuty Rożne (Średnia)"
    rywal_rozne_suma = "Rywal - Rzuty Rożne (Suma)"
    rywal_rozne_srednia = "Rywal - Rzuty Rożne (Średnia)"

    spalone_suma = "Spalone (Suma)"
    rywal_spalone_suma = "Rywal - Spalone (Suma)"

    # Bramkarskie
    obrony_suma = "Obrony (Suma)"
    obrony_srednia = "Obrony (Średnia)"
    rywal_obrony_suma = "Rywal - Obrony (Suma)"
    rywal_obrony_srednia = "Rywal - Obrony (Średnia)"

    obrony_z_pola_suma = "Obrony z pola karnego (Suma)"
    rywal_obrony_z_pola_suma = "Rywal - Obrony z pola karnego (Suma)"

    gole_stracone_suma = "Wpuszczone Gole (Suma)"
    gole_stracone_srednia = "Wpuszczone Gole (Średnia)"
    rywal_gole_stracone_suma = "Rywal - Wpuszczone Gole (Suma)"
    rywal_gole_stracone_srednia = "Rywal - Wpuszczone Gole (Średnia)"

    xga_suma = "xG Przeciwnika (xGA) (Suma)"
    xga_srednia = "xG Przeciwnika (xGA) (Średnia)"
    rywal_xga_suma = "Rywal - xG Przeciwnika (xGA) (Suma)"
    rywal_xga_srednia = "Rywal - xG Przeciwnika (xGA) (Średnia)"
    
    uratowane_gole_suma = "Uratowane Gole (xGOTA - Gole) (Suma)"
    rywal_uratowane_gole_suma = "Rywal - Uratowane Gole (xGOTA - Gole) (Suma)"

    czyste_konta_suma = "Czyste Konto (Suma)"
    rywal_czyste_konta_suma = "Rywal - Czyste Konto (Suma)"
    
    wyjscia_do_dosrodkowan_suma = "Udane wyjście do dośrodkowania (Suma)"
    rywal_wyjscia_do_dosrodkowan_suma = "Rywal - Udane wyjście do dośrodkowania (Suma)"
    
    nieudane_wyjscia_suma = "Nieudane wyjście do dośrodkowania (Suma)"
    rywal_nieudane_wyjscia_suma = "Rywal - Nieudane wyjście do dośrodkowania (Suma)"
    
    wyjscia_poza_pole_suma = "Wyjścia poza pole karne (Keeper-Sweeper) (Suma)"
    rywal_wyjscia_poza_pole_suma = "Rywal - Wyjścia poza pole karne (Keeper-Sweeper) (Suma)"

STATS_DEFINITIONS = {
    "Gole Oczekiwane (xG)": "Mierzy jakość sytuacji bramkowej - prawdopodobieństwo strzelenia gola z danej pozycji (skala 0-1) na podstawie historycznych danych (np. odległość, kąt, rodzaj podania).",
    "Asysty Oczekiwane (xA)": "Mierzy prawdopodobieństwo, że wykonane podanie zakończy się asystą. Zależy od jakości podania i miejsca, w które trafiła piłka (niezależnie od tego, czy strzelec trafił).",
    "xGOT (Expected Goals on Target)": "xG po strzale (post-shot xG). Uwzględnia miejsce, w które poleciała piłka w bramce. Mierzy jakość wykończenia (np. strzał w okienko ma wyższe xGOT niż w środek).",
    "Kluczowe Podanie (Key Pass)": "Ostatnie podanie do kolegi z zespołu, który następnie oddaje strzał na bramkę (ale nie kończy się golem).",
    "Stworzona Setka (Big Chance Created)": "Podanie, które stawia kolegę w sytuacji, w której 'powinien' paść gol (np. sam na sam, strzał z bliska bez presji) - minimum 0,2 xG.",
    "Zmarnowana Setka (Big Chance Missed)": "Sytuacja, w której zawodnik ma bardzo dużą szansę na gola - minimum 0,2 xG (np. rzut karny, sam na sam), ale nie trafia do bramki.",
    "Przechwyt (Interception)": "Przecięcie podania rywala poprzez czytanie gry i ustawienie się na linii podania (bez bezpośredniego starcia fizycznego).",
    "Odbiór (Tackle)": "Odebranie piłki rywalowi w bezpośrednim starciu (kontakcie), gdy rywal jest w jej posiadaniu. Może być udany (odzyskanie piłki) lub nie.",
    "Odzyskanie Piłki (Ball Recovery)": "Przejęcie 'niczyjej' piłki (np. po wybiciu, rykoszecie, błędzie technicznym rywala), gdy żadna z drużyn nie ma nad nią kontroli.",
    "Wybicie (Clearance)": "Defensywne zagranie mające na celu oddalenie piłki ze strefy zagrożenia (pola karnego), bez intencji podania do konkretnego kolegi.",
    "Pojedynek (Duel)": "Każde starcie '50-50' o piłkę między dwoma zawodnikami (na ziemi lub w powietrzu).",
    "Drybling (Dribble / Take-on)": "Próba minięcia rywala z piłką przy nodze. Udany drybling to zachowanie posiadania po minięciu obrońcy.",
}