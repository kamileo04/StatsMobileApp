import json
import os

from new_config import DATA_DIR, PIZZA_CHART_TEMPLATES, LOWER_IS_BETTER_STATS, STATS_PL_MAP

import pandas as pd
import numpy as np
from scipy.stats import percentileofscore


if os.environ.get("DOCKER_ENV"):
    SHARED_DATA_DIR = os.path.join("/app", DATA_DIR)
else:
    base_project_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    SHARED_DATA_DIR = os.path.join(base_project_dir, DATA_DIR)


def load_season_teams(league: str, season_year: str) -> list:
    """Odczytuje zapisaną listę drużyn z pliku JSON."""
    filename = f"teams_{league.replace(' ','_')}_{season_year.replace('/','-')}.json"
    filepath = os.path.join(SHARED_DATA_DIR, filename)

    if os.path.exists(filepath):
        try:
            with open(filepath, 'r', encoding='utf-8') as f:
                return json.load(f)
        except Exception as e:
            print(f"Error loading teams: {e}")
    return []


def load_season_players(league: str, season_year: str) -> dict:
    """Odczytuje zapisaną listę zawodników i ID przypisanych do drużyn."""
    filename = f"season_players_{league.replace(' ','_')}_{season_year.replace('/','-')}.json"
    filepath = os.path.join(SHARED_DATA_DIR, filename)

    if os.path.exists(filepath):
        try:
            with open(filepath, 'r', encoding='utf-8') as f:
                return json.load(f)
        except Exception as e:
            print(f"Error loading players: {e}")
    return {}


def get_player_match_history(player_id: int, season_year: str, league_name: str) -> list:
    """
    Przeszukuje zapisane pliki player_stats_*.json i zbiera mecze danego zawodnika.
    Oblicza też statystyki pochodne (procenty, xG diff) - analogicznie do starego loadera.
    Zwraca listę słowników {match_id, label, minutes, stats} posortowaną od najnowszego.
    """
    player_id_str = str(player_id)
    found_matches = []

    # Budujemy mapę match_id -> etykieta z all_matches_*.json
    matches_info_map = {}
    all_matches_file = os.path.join(
        SHARED_DATA_DIR,
        f"all_matches_{league_name.replace(' ','_')}_{season_year.replace('/','-')}.json"
    )
    if os.path.exists(all_matches_file):
        try:
            with open(all_matches_file, 'r', encoding='utf-8') as f:
                raw = json.load(f)
            # Obsługa obu formatów: lista lub {"item1": [...]}
            match_list = raw if isinstance(raw, list) else raw.get('item1', [])
            for m in match_list:
                if not isinstance(m, dict):
                    continue
                mid = str(m.get('id', ''))
                if not mid:
                    continue
                h = m.get('homeTeam', {})
                a = m.get('awayTeam', {})
                h_name = h.get('name', '') if isinstance(h, dict) else str(h)
                a_name = a.get('name', '') if isinstance(a, dict) else str(a)
                hs = m.get('homeScore', {})
                as_ = m.get('awayScore', {})
                h_score = hs.get('display', hs.get('current', '?')) if isinstance(hs, dict) else hs
                a_score = as_.get('display', as_.get('current', '?')) if isinstance(as_, dict) else as_
                ts = m.get('startTimestamp', 0)
                try:
                    from datetime import datetime, timezone
                    ds = datetime.fromtimestamp(ts, tz=timezone.utc).strftime('%Y-%m-%d') if ts else 'Unknown'
                except Exception:
                    ds = 'Unknown'
                matches_info_map[mid] = (f"{h_name} {h_score}-{a_score} {a_name} ({ds})", ts)
        except Exception as e:
            print(f"Błąd wczytywania all_matches: {e}")

    if not os.path.exists(SHARED_DATA_DIR):
        return []

    player_files = [f for f in os.listdir(SHARED_DATA_DIR) if f.startswith("player_stats_") and f.endswith(".json")]

    for fname in player_files:
        match_id = fname.replace("player_stats_", "").replace(".json", "")
        try:
            with open(os.path.join(SHARED_DATA_DIR, fname), 'r', encoding='utf-8') as f:
                m_data = json.load(f)

            p_list = m_data if isinstance(m_data, list) else m_data.get('players', [])

            for p_entry in p_list:
                if not isinstance(p_entry, dict):
                    continue
                entry_pid = str(p_entry.get('player', {}).get('id', ''))
                if entry_pid != player_id_str:
                    continue

                raw_stats = p_entry.get('statistics', {})
                mins = raw_stats.get('minutesPlayed', 0)
                rating = raw_stats.get('rating', '-')

                # Spłaszczanie zagnieżdżonych słowników
                flat_stats = {}
                for k, v in raw_stats.items():
                    if isinstance(v, dict):
                        for sk, sv in v.items():
                            flat_stats[f"{k}_{sk}"] = sv
                    else:
                        flat_stats[k] = v

                # ---------- Statystyki pochodne ----------
                def _pct(num_key, den_key):
                    n = flat_stats.get(num_key, 0) or 0
                    d = flat_stats.get(den_key, 0) or 0
                    return round((n / d) * 100, 1) if d > 0 else 0.0

                flat_stats['accuratePassesPercentage'] = _pct('accuratePass', 'totalPass')
                flat_stats['accurateLongBallsPercentage'] = _pct('accurateLongBalls', 'totalLongBalls')
                flat_stats['accurateCrossesPercentage'] = _pct('accurateCross', 'totalCross')
                flat_stats['accurateOppositionHalfPassesPercentage'] = _pct('accurateOppositionHalfPasses', 'totalOppositionHalfPasses')
                flat_stats['accurateOwnHalfPassesPercentage'] = _pct('accurateOwnHalfPasses', 'totalOwnHalfPasses')
                flat_stats['wonContestPercentage'] = _pct('wonContest', 'totalContest')
                flat_stats['wonTacklePercentage'] = _pct('wonTackle', 'totalTackle')

                aw = flat_stats.get('aerialWon', 0) or 0
                al = flat_stats.get('aerialLost', 0) or 0
                flat_stats['aerialDuelsWonPercentage'] = round((aw / (aw + al)) * 100, 1) if (aw + al) > 0 else 0.0

                dw = flat_stats.get('duelWon', 0) or 0
                dl = flat_stats.get('duelLost', 0) or 0
                flat_stats['groundDuelsWonPercentage'] = round((dw / (dw + dl)) * 100, 1) if (dw + dl) > 0 else 0.0

                goals = flat_stats.get('goals', 0) or 0
                xg = flat_stats.get('expectedGoals', 0) or 0
                xgot = flat_stats.get('xGOT', 0) or 0

                # xGOT z shotmap jeśli brakuje
                if xgot == 0:
                    shotmap_file = os.path.join(SHARED_DATA_DIR, f"shotmap_{match_id}.json")
                    if os.path.exists(shotmap_file):
                        try:
                            with open(shotmap_file, 'r', encoding='utf-8') as sf:
                                shot_data = json.load(sf)
                            shots = shot_data.get('shotmap', shot_data) if isinstance(shot_data, dict) else shot_data
                            if isinstance(shots, list):
                                pid_int = int(player_id)
                                for shot in shots:
                                    if isinstance(shot, dict):
                                        sp = shot.get('player', {})
                                        if isinstance(sp, dict) and sp.get('id') == pid_int:
                                            xgot += shot.get('xgot', 0) or 0
                            flat_stats['xGOT'] = round(xgot, 4)
                        except Exception:
                            pass

                flat_stats['G-xG'] = round(goals - xg, 4)
                flat_stats['xGOT-xG'] = round(xgot - xg, 4)
                flat_stats['G-xGOT'] = round(goals - xgot, 4)

                # Bramkarskie
                saves = flat_stats.get('saves', 0) or 0
                goals_conceded = flat_stats.get('goalsConceded', 0) or 0
                flat_stats['savePercentage'] = round((saves / (saves + goals_conceded)) * 100, 1) if (saves + goals_conceded) > 0 else 0.0
                flat_stats['goalsPrevented'] = round(flat_stats.get('xGOTA', 0) - goals_conceded, 4)

                # Etykieta meczu
                match_info = matches_info_map.get(match_id)
                match_name = match_info[0] if isinstance(match_info, tuple) else (match_info or f"Mecz {match_id}")
                match_ts = match_info[1] if isinstance(match_info, tuple) else 0

                # Fallback: buduj etykietę z drużyn obecnych w pliku
                if not match_name:
                    teams = list(set(
                        p.get('teamName') for p in p_list
                        if isinstance(p, dict) and p.get('teamName')
                    ))
                    match_name = f"{teams[0]} vs {teams[1]}" if len(teams) >= 2 else f"Mecz {match_id}"

                label = f"{match_name} | Ocena: {rating} | {mins}'"

                found_matches.append({
                    'match_id': match_id,
                    'label': label,
                    'minutes': mins,
                    'rating': rating,
                    'timestamp': match_ts,
                    'stats': flat_stats,
                })
                break  # każdy zawodnik jest raz w pliku

        except Exception as e:
            print(f"Błąd parsowania {fname}: {e}")
            continue

    found_matches.sort(
        key=lambda x: (x.get('timestamp', 0), int(x['match_id']) if str(x['match_id']).isdigit() else 0),
        reverse=True
    )
    return found_matches


def get_player_season_stats(player_id: int, season_year: str) -> dict:
    """
    Wczytuje plik player_database_*.json i zwraca wiersz danych dla danego zawodnika.
    Ten plik produkuje skrypt run_build_database (generowany na serwerze ze Streamlitem).
    Oblicza też statystyki pochodne (G-xG, xGOTA-based goalsPrevented, percentyle pozostają po stronie API).
    """
    db_file = os.path.join(SHARED_DATA_DIR, f"player_database_{season_year.replace('/', '-')}.json")
    if not os.path.exists(db_file):
        return {}

    try:
        with open(db_file, 'r', encoding='utf-8') as f:
            data = json.load(f)
    except Exception as e:
        print(f"Błąd wczytywania player_database: {e}")
        return {}

    pid_str = str(player_id)
    for row in data:
        if not isinstance(row, dict):
            continue
        row_id = str(row.get('player id', row.get('id', '')))
        if row_id == pid_str:
            # Oblicz pochodne jeśli jeszcze nie ma w pliku
            goals = float(row.get('goals', 0) or 0)
            xg = float(row.get('expectedGoals', 0) or 0)
            xgot = float(row.get('xGOT', 0) or 0)
            xgota = float(row.get('xGOTA', 0) or 0)
            goals_conceded = float(row.get('goalsConceded', 0) or 0)

            row['G-xG'] = round(goals - xg, 4)
            row['xGOT-xG'] = round(xgot - xg, 4)
            row['G-xGOT'] = round(goals - xgot, 4)
            row['goalsPrevented'] = round(xgota - goals_conceded, 4)

            return row

    return {}

def get_player_radar_and_table_stats(player_id: int, season_year: str, override_position="Auto"):
    """
    Zwraca kompleksowe dane JSON potrzebne dla UI KMP:
    1. radar_chart: znormalizowane % dla danej pozycji (z configu).
    2. full_table: każda statystyka z medianą, percentylem i wartością P90.
    """
    db_file = os.path.join(SHARED_DATA_DIR, f"player_database_{season_year.replace('/', '-')}.json")
    if not os.path.exists(db_file):
        return None
        
    try:
        df = pd.read_json(db_file)
    except Exception as e:
        print(f"Error loading full league data: {e}")
        return None
        
    if df.empty:
        return None
        
    df['player id'] = pd.to_numeric(df['player id'], errors='coerce')
    p_row = df[df['player id'] == player_id]
    if p_row.empty:
        return None
    p_row = p_row.iloc[0]
    
    p_name = p_row.get('name', 'Nieznany')
    p_minutes = float(p_row.get('totalSeasonMinutes_stats', p_row.get('minutesPlayed', 0)))
    
    calc_pos = p_row.get('Calculated Position', 'N/A')
    raw_pos = p_row.get('position', 'N/A')
    detailed_positions = ["ST", "W", "CAM", "RM/LM", "CM/CDM", "LB", "RB", "LB/RB", "CB", "GK"]
    
    if override_position and override_position != "Auto":
        pos = override_position
    elif calc_pos in detailed_positions:
        pos = calc_pos
    else:
        pos = raw_pos
        
    if 'Calculated Position' in df.columns:
        df['Best_Pos'] = np.where(df['Calculated Position'].isin(detailed_positions), df['Calculated Position'], df['position'])
    else:
        df['Best_Pos'] = df['position']
        
    df['totalSeasonMinutes_stats'] = pd.to_numeric(df.get('minutesPlayed', 0), errors='coerce').fillna(0)
    
    df_group = df[(df['Best_Pos'] == pos) & (df['totalSeasonMinutes_stats'] >= 300)]
    if len(df_group) < 5:
        fallback_map = {"ST": "F", "W": "M", "CAM": "M", "RM/LM": "M", "CM/CDM": "M", "LB": "D", "RB": "D", "LB/RB": "D", "CB": "D"}
        broad_pos = fallback_map.get(pos, raw_pos)
        df_group = df[(df['position'] == broad_pos) & (df['totalSeasonMinutes_stats'] >= 300)]
        
    if df_group.empty:
        df_group = df[df['totalSeasonMinutes_stats'] >= 300]
        
    group_size = len(df_group)
    
    radar_chart = []
    stats_to_check = PIZZA_CHART_TEMPLATES.get(pos, [])
    if not stats_to_check:
        fallback_template_pos = {"RM/LM": "W", "LB/RB": "LB", "CM": "CM/CDM"}.get(pos, raw_pos)
        stats_to_check = PIZZA_CHART_TEMPLATES.get(fallback_template_pos, PIZZA_CHART_TEMPLATES.get("CM/CDM", []))
        
    for stat in stats_to_check:
        if stat in df.columns:
            val = float(p_row.get(stat, 0) or 0)
            dist = df_group[stat].fillna(0).astype(float)
            
            perc = percentileofscore(dist, val, kind='weak')
            
            if stat in LOWER_IS_BETTER_STATS:
                perc = 100 - perc
                
            median = float(dist.median())
            radar_chart.append({
                "statKey": stat,
                "label": STATS_PL_MAP.get(stat, stat),
                "value": val,
                "percentile": int(perc),
                "median": median
            })

    full_table = []
    apps = float(p_row.get('appearances', 0))
    if apps == 0 and p_minutes > 0: apps = 1
    
    ignore = ['player id', 'name', 'position', 'Calculated Position', 'Best_Pos', 'team', 'teamName', 'slug', 'id']
    
    for col in p_row.index:
        if col in ignore or isinstance(p_row[col], (dict, list, str)): continue
        if col not in STATS_PL_MAP: continue
        
        try: val_total = float(p_row[col])
        except: continue
        
        val_p90 = 0.0
        if col == 'rating':
            val_p90 = val_total / apps if apps > 0 else 0
            val_total = val_p90
        elif 'Percentage' in col or '%' in col or col in ['appearances', 'minutesPlayed', 'totalSeasonMinutes_stats']:
            val_p90 = val_total
        else:
            val_p90 = (val_total / p_minutes) * 90 if p_minutes > 0 else 0
            
        percentile = 0
        median = 0.0
        if not df_group.empty and col in df_group.columns:
            group_vals = df_group[col].fillna(0).astype(float)
            
            if col == 'rating':
                g_apps = df_group['appearances'].fillna(0).astype(float).replace(0, 1)
                group_vals = group_vals / g_apps
                compare_val = val_p90
            elif col not in ['appearances', 'minutesPlayed', 'totalSeasonMinutes_stats'] and 'Percent' not in col:
                g_mins = df_group['totalSeasonMinutes_stats'].fillna(0).replace(0, 1)
                group_vals = (group_vals / g_mins) * 90
                compare_val = val_p90
            else:
                compare_val = val_total
                
            percentile = percentileofscore(group_vals, compare_val, kind='weak')
            median = float(np.median(group_vals))
            
            if col in LOWER_IS_BETTER_STATS:
                percentile = 100 - percentile
                
        if abs(val_total) > 0.001 or col == 'rating':
            full_table.append({
                "statKey": col,
                "label": STATS_PL_MAP.get(col, col),
                "totalValue": round(val_total, 2),
                "p90Value": round(val_p90, 2),
                "percentile": int(percentile),
                "median": round(median, 2)
            })
            
    full_table.sort(key=lambda x: x["label"])

    return {
        "playerName": p_name,
        "position": pos,
        "groupSize": group_size,
        "minutes": p_minutes,
        "radarChart": radar_chart,
        "fullTable": full_table
    }
