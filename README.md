# ClientInfo

Paper 1.21.11 plugin that shows which client a player joined with and, if a compatible
client mod reports it, their mod list.

## Commands
- `/mods <player>` – info about another online player (`clientinfo.mods`)
- `/mods self` – your own client info (`clientinfo.mods.self`)
- `/mods join` – toggle your own join notification (`clientinfo.join`)
- `/mods reload` – reload config (`clientinfo.reload`)

## Notes
- The client brand is what the client reports; it can be spoofed and is shown as-is.
- Mod lists are only available from clients running a compatible mod, see `PROTOCOL.md`.
  Otherwise "Not available" is shown.
- Player settings are stored in `plugins/ClientInfo/players.yml`.

## Build
Push to GitHub – the workflow in `.github/workflows/build.yml` builds the JAR and uploads it
as an artifact named `ClientInfo`.
