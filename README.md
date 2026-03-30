# StatsMobileApp
## Funkcjonalności: 
- porównywanie statystyk z jednego meczu do całego sezonu
- porównywanie statystyk między wieloma zawodnikami
- wizualizacja danych poprzez wykresy radarowe i słupkowe
- grupowanie zawodników według pozycji i klubu
- system "ulubionych" zawodników/klubów w celu szybkiego dostępu

## Niefunkcjonalności:
- wieloplatformowość (współdzielony kod logiki i UI za pomocą Kotlin Multiplatform i Compose Multiplatform)
- responsywny layout (dopasowanie do ekranów telefonów w pionie oraz wersji webowej przeglądarki)
- intuicyjna obsługa i wsparcie dla Dark Mode / Light Mode
- lokalne cachowanie danych (aby część podstawowych statystyk i ulubionych działała również bez dostępu do sieci)

## Struktura Bazy Danych (File-based JSON)
Aplikacja opiera się na wydajnej, plikowej strukturze bazy danych odświeżanej w głównej aplikacji webowej. W głównym katalogu znajdują się:
* `teams_<league>_<season>.json` – Słownik drużyn występujących w danej lidze z podziałem na sezony.
* `season_players_<league>_<season>.json` – Mapowanie składów poszczególnych drużyn (nazwa drużyny -> nazwisko zawodnika -> ID zawodnika).
* `all_matches_<league>_<season>.json` – Informacje o każdym meczu (wyniki, daty, identyfikatory).
* `custom_matches_cache.json` – Bufor dla meczów z problematycznym formatowaniem (nietypowe złączenia rund pobrane z nowego API po lutym 2026).
* `player_stats_<match_id>.json` – Surowe, szczegółowe statystyki wewnątrz-meczowe dla wszystkich zawodników występujących w danym meczu.
* `shotmap_<match_id>.json` – Dane pozycjonalne strzałów do kalkulacji xGOT (Expected Goals on Target).
* `player_database_<season>.json` – Pełna baza danych zagregowanych statystyk sezonowych wszystkich zawodników, która służy do rysowania wykresów radarowych i porównywania zawodników z medianą ligową.

## Dostępne Endpointy API (Backend FastAPI)
Backend działa w środowisku serwerowym (FastAPI w pliku `api/main.py`) na porcie `8000` i dostarcza następujące ścieżki (zabezpieczone lokalnie hasłem aplikacyjnym):

* `GET /` - Sprawdza status żywotności (Health check).
* `POST /login` - Obsługuje jednorazowe uwierzytelnianie przez token dostępowy z wymaganym hasłem (body: json `{"password": "<pass>"}`).
* `GET /teams` - Pobiera listę dostępnych drużyn na ten sezon.
* `GET /players/{team}` - Pobiera zawodników należących do wskazanego klubu (`{team}`).
* `GET /matches/{player_id}` - Zwraca kompletną, chronologicznie posortowaną listę rozegranych meczów wraz z wynikami i statusem dla głównej historii meczów.
* `GET /match_report/{player_id}/{match_id}` - Udostępnia szczegółowe statystyki zawodnika za pojedynczy mecz (obrona, podania, wygrane pojedynki).
* `GET /season_stats/{player_id}` - Pobiera przekrojowe i zsumowane parametry gracza za dany rok rozgrywkowy w celach analitycznych.
* `GET /player_percentiles/{player_id}` - Kalkuluje "w locie" porównania do median ligi dla radar charts oraz tabel statystycznych wraz ze wskaźnikami filtrowanymi przez `min_minutes` i wymuszaniem pozycji.