# WarGames

Table-top, unit-based war military maneuver game.

## UI
- Main Menu <br/>
  -> Match Creation
- Match Creation <br/>
  -> Main Menu <br/>
  -> Match Screen
- Match Screen

## Project Structure
- ui
  - screens
    - main menu 
    - game screen
  - entity rendering 
- logic
  - models
  - game loop

## Actions Handling

### Unbound Actions - Have no associated match job
- UserAction
  - UpdateUsername
- LobbyAction
  - CreateLobby
  - CloseLobby
  - JoinLobby
  - LeaveLobby
  - UpdateLobbyName
- MatchAction
  - CreateMatch

### Bound Action - Have associated match job
- LiveMatchAction
  - SetMatchState
  - PlaceEntity
  - MoveEntity
  - SetEntityPath

## TODO
- unit rotation
- menu UI
- unit path smoothing (grid snapping maybe?)
- improve collision response
- networking
- toasts system
- command-runner units
  - unit which goes from commander to the commanded BU
