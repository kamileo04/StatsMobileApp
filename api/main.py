import os
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import List, Optional, Dict, Any

import new_data_loader as data
from new_config import LEAGUE_NAME
DEFAULT_SEASON = "25-26"


API_PASSWORD = os.environ.get("API_PASSWORD", "")

app = FastAPI(
    title="SofaMobile API",
    description="Wewnętrzne API dla aplikacji mobilnej do pobierania statystyk",
    version="1.2.0"
)

# --- CORS ---
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ---------------------------------------------------------------------------
# Pydantic Models (DTOs)
# ---------------------------------------------------------------------------
class LoginRequest(BaseModel):
    password: str

class LoginResponse(BaseModel):
    success: bool
    token: Optional[str] = None
    message: str

class TeamsResponse(BaseModel):
    season: str
    teams: List[str]

class Player(BaseModel):
    id: int
    name: str

class PlayersResponse(BaseModel):
    team: str
    players: List[Player]

class MatchHistoryItem(BaseModel):
    match_id: str
    label: str
    minutes: int
    rating: Any  # może być float lub '-'
    timestamp: int

class MatchReportResponse(BaseModel):
    match_id: str
    label: str
    minutes: int
    rating: Any
    stats: Dict[str, Any]

class SeasonStatsResponse(BaseModel):
    player_id: int
    season: str
    stats: Dict[str, Any]

# ---------------------------------------------------------------------------
# Endpoints
# ---------------------------------------------------------------------------
@app.get("/")
def read_root():
    return {"status": "ok", "message": "SofaMobile API is running", "version": "1.2.0"}

@app.post("/login", response_model=LoginResponse)
def login(request: LoginRequest):
    if request.password == API_PASSWORD and API_PASSWORD:
        return LoginResponse(success=True, token="secret_token_123", message="Zalogowano pomyślnie")
    raise HTTPException(status_code=401, detail="Nieprawidłowe hasło")

@app.get("/teams", response_model=TeamsResponse)
def get_teams():
    try:
        teams = data.load_season_teams(LEAGUE_NAME, DEFAULT_SEASON)
        if not teams:
            raise HTTPException(status_code=404, detail="Brak dostępnych drużyn na ten sezon.")
        return TeamsResponse(season=DEFAULT_SEASON, teams=teams)
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/players/{team}", response_model=PlayersResponse)
def get_players(team: str):
    try:
        players_map = data.load_season_players(LEAGUE_NAME, DEFAULT_SEASON)
        if not players_map or team not in players_map:
            raise HTTPException(status_code=404, detail=f"Brak zawodników dla drużyny '{team}'")

        players_list = [
            Player(id=p_id, name=p_name)
            for p_name, p_id in players_map[team].items()
            if p_id is not None
        ]
        players_list.sort(key=lambda x: x.name)
        return PlayersResponse(team=team, players=players_list)
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/matches/{player_id}", response_model=List[MatchHistoryItem])
def get_match_history(player_id: int):
    """Zwraca listę meczów danego zawodnika (posortowaną od najnowszego)."""
    try:
        matches = data.get_player_match_history(player_id, DEFAULT_SEASON, LEAGUE_NAME)
        if not matches:
            raise HTTPException(status_code=404, detail=f"Brak meczów dla zawodnika o ID {player_id}")
        return [
            MatchHistoryItem(
                match_id=m['match_id'],
                label=m['label'],
                minutes=int(m.get('minutes', 0)),
                rating=m.get('rating', '-'),
                timestamp=int(m.get('timestamp', 0)),
            )
            for m in matches
        ]
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/match_report/{player_id}/{match_id}", response_model=MatchReportResponse)
def get_match_report(player_id: int, match_id: str):
    """Zwraca pełne statystyki zawodnika z konkretnego meczu (do raportu pomeczowego)."""
    try:
        matches = data.get_player_match_history(player_id, DEFAULT_SEASON, LEAGUE_NAME)
        match = next((m for m in matches if str(m['match_id']) == str(match_id)), None)
        if not match:
            raise HTTPException(
                status_code=404,
                detail=f"Brak danych dla zawodnika {player_id} w meczu {match_id}"
            )
        return MatchReportResponse(
            match_id=match['match_id'],
            label=match['label'],
            minutes=int(match.get('minutes', 0)),
            rating=match.get('rating', '-'),
            stats=match['stats'],
        )
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/season_stats/{player_id}", response_model=SeasonStatsResponse)
def get_season_stats(player_id: int):
    """
    Zwraca statystyki sezonowe zawodnika z player_database_*.json
    (agregowane przez skrypt run_build_database ze Streamlita).
    Używane do budowania wykresów radarowych i tabelek percentyli.
    """
    try:
        stats = data.get_player_season_stats(player_id, DEFAULT_SEASON)
        if not stats:
            raise HTTPException(
                status_code=404,
                detail=f"Brak danych sezonowych dla zawodnika o ID {player_id}"
            )
        return SeasonStatsResponse(player_id=player_id, season=DEFAULT_SEASON, stats=stats)
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
