from fastapi import FastAPI, HTTPException, Depends
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import List, Optional

app = FastAPI(
    title="SofaMobile API",
    description="Wewnętrzne API dla aplikacji mobilnej do pobierania statystyk",
    version="1.0.0"
)

# --- CORS Configuration ---
# Pozwala aplikacji KMP (w tym WebAssembly na localhost) na odpytywanie API
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # W produkcji można ograniczyć do konkretnych domen
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# --- Pydantic Models (DTOs) ---

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

# --- Endpoints ---

@app.get("/")
def read_root():
    return {"status": "ok", "message": "SofaMobile API is running"}

@app.post("/login", response_model=LoginResponse)
def login(request: LoginRequest):
    # TODO: Logika weryfikacji hasła
    if request.password == "test": # Prosty mock
        return LoginResponse(success=True, token="mock_token_123", message="Zalogowano pomyślnie")
    raise HTTPException(status_code=401, detail="Nieprawidłowe hasło")

@app.get("/teams", response_model=TeamsResponse)
def get_teams():
    # TODO: Podpięcie pobierania drużyn z dataloadera
    mock_teams = ["Podbeskidzie Bielsko-Biała", "Ruch Chorzów", "Wisła Kraków"]
    return TeamsResponse(season="25/26", teams=mock_teams)

@app.get("/players/{team}", response_model=PlayersResponse)
def get_players(team: str):
    # TODO: Podpięcie pobierania zawodników dla danej drużyny
    mock_players = [
        Player(id=1, name="Jan Kowalski"),
        Player(id=2, name="Piotr Nowak")
    ]
    return PlayersResponse(team=team, players=mock_players)

@app.get("/matches/{player_id}", response_model=List[MatchHistoryItem])
def get_match_history(player_id: int):
    # TODO: Podpięcie historii meczów zawodnika
    mock_history = [
        MatchHistoryItem(match_id="m1", label="Podbeskidzie vs Arka - 2:0"),
        MatchHistoryItem(match_id="m2", label="Podbeskidzie vs GKS - 1:1")
    ]
    return mock_history

# Uruchamianie lokalnie (do testów):
# Możesz odpalić z konsoli wpisując: uvicorn api.main:app --reload
if __name__ == "__main__":
    import uvicorn
    uvicorn.run("api.main:app", host="0.0.0.0", port=8000, reload=True)
