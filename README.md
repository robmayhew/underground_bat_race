# underground_bat_race
You're a bat underground; there is a flood.

---

## WebSocket API

**Endpoint:** `ws://<host>/ws/game`

### Connecting

| Intent | URL |
|--------|-----|
| Create a new game | `ws://host/ws/game` |
| Join an existing game | `ws://host/ws/game?gameId=ABCD1234` |

Up to **4 players** may join a game. If the requested game is full (or not found), a new game is created automatically.

---

### Server → Client Messages

#### `config` — sent immediately on connect
```json
{
  "type": "config",
  "gameId": "ABCD1234",
  "seed": -4872634123456,
  "playerId": 2,
  "existingPlayers": [1]
}
```
| Field | Type | Description |
|-------|------|-------------|
| `gameId` | string | Share this ID so other players can join the same game |
| `seed` | long | Random seed — identical for all players in the game |
| `playerId` | int | This player's assigned ID (1–4) |
| `existingPlayers` | int[] | Player IDs already in the game |

---

#### `player_joined` — broadcast when a new player joins
```json
{
  "type": "player_joined",
  "playerId": 3,
  "playerCount": 3
}
```

---

#### `player_left` — broadcast when a player disconnects
```json
{
  "type": "player_left",
  "playerId": 3,
  "playerCount": 2
}
```

---

#### `player_state` — relayed when another player sends their state
```json
{
  "type": "player_state",
  "playerId": 1,
  "data": { }
}
```
The `data` object contains whatever the originating client sent — position, velocity, etc.

---

### Client → Server Messages

#### `state` — send this player's current game state
```json
{
  "type": "state",
  "data": {
    "x": 120,
    "y": 45,
    "vel": { "x": 2, "y": -1 }
  }
}
```
The server relays this to all other players in the same game as a `player_state` message. The `data` payload is arbitrary JSON.
