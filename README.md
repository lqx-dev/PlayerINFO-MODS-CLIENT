# ClientInfo (flat layout)

All source files, plugin.yml and config.yml live directly in the repository root.
Push to GitHub, the workflow in .github/workflows/build.yml builds the JAR (Actions -> Artifacts -> ClientInfo).

Commands: /mods <player> | /mods self | /mods join | /mods reload
Permissions: clientinfo.mods, clientinfo.mods.self, clientinfo.join, clientinfo.reload
Mod list protocol for client mods: see PROTOCOL.md
