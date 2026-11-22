# Warp

Named warps and player teleport requests for a **PaperMC Minecraft server**, shipped as a single plugin jar for **Paper 26.3** (Minecraft 26.3) on **Java 25**.

```
plugins/warp-26.3.jar
```

## What it does

* `/warp create <name>` saves the current position of the player as a named warp.
* `/warp <name>` teleports the player to that warp. Coordinates, yaw and pitch are
  remembered, so the teleport feels like walking home.
* `/warp del <name>` deletes a warp.
* `/warp list` lists the saved warps.

A `/tpa` request asks a player to teleport to you. The requester picks the target
with `/tpa <player> [reason]`, the target answers with `/tpa accept <player>` and the
requester is teleported to the acceptor's location. `/tpa deny <player>` refuses the
request and `/tpa cancel <player>` lets the requester drop their own request.

Every request expires on its own, so nobody is stuck waiting forever.

## Commands

| Command | Permission | What it does |
| --- | --- | --- |
| `/warp <name>` | `warp.use` | Teleport to a saved warp |
| `/warp create <name>` | `warp.create` | Save the current position as a warp |
| `/warp del <name>` | `warp.delete` | Delete a warp |
| `/warp list` | `warp.command` | List the saved warps |
| `/tpa <player> [reason]` | `warp.tpa` | Send a teleport request to a player |
| `/tpa accept <player>` | `warp.tpa` | Accept a pending request |
| `/tpa deny <player>` | `warp.tpa` | Deny a pending request |
| `/tpa cancel <player>` | `warp.tpa` | Cancel your own pending request |

`/wp` is an alias of `/warp`. All permissions default to operators.

## Configuration

`plugins/Warp/config.yml`:

```yaml
teleport:
  request-expiry-seconds: 30
```

`plugins/Warp/warps.yml` stores every named warp:

```yaml
home:
  owner: Steve
  world: world
  x: 100.5
  y: 64.0
  z: -200.25
  yaw: 90.0
  pitch: 0.0
```

## Build

```bash
mvn -B verify
# target/warp-26.3.jar
```

or `./build.sh`, which also runs `tools/verify-jar.sh`.

Tests cover the warp name rules, the warp store and the teleport request handling:
name normalisation, validation, store save/get/delete/list and the accept, deny,
cancel and expiry logic of a `/tpa` request.

## How it works

* The warp name is validated in the `Warp` value object, so a bad name never
  reaches the store.
* The `WarpStore` keeps the warps in memory and the plugin persists them to
  `warps.yml` on every change and reloads them on start.
* The `TeleportManager` owns the pending requests and expires them, so a server
  restart or a stale request cannot leave a player waiting.
* The commands are the only entry point: no events, no magic, everything goes
  through the same small services.

Paper 26.3 / Minecraft 26.3, Java 25, API version `26.3`.
