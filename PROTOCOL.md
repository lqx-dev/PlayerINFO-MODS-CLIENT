# ClientInfo – Mod List Protocol (v1)

A Paper server cannot read the mods installed on a client. This plugin therefore opens a
plugin messaging channel that a **compatible client mod** can use to report its mod list.
Without such a client mod, the plugin always shows "Not available" – it never guesses.

## Channel

`clientinfo:modlist` (client -> server)

The server registers this channel, so it is announced to the client via `minecraft:register`.
A client mod can use that to check whether the ClientInfo plugin is installed before sending.

## Payload

UTF-8 text, lines separated by `\n`:

```
v1
fabric-api|Fabric API|0.100.0
sodium|Sodium|0.6.0
lithium
modmenu|Mod Menu
```

- Line 1: protocol version, must be exactly `v1`.
- Every following line: `modid|Display Name|version`. Name and version are optional.
- Max. 500 mods, max. 128 characters per line, max. 32 000 bytes total.
- Send once after joining (e.g. when the play phase starts). Sending again replaces the old list.

The plugin sanitizes all data (removes `§`, `&` and control characters) and stores it only in
memory for the sending player, until that player disconnects.

## Fabric example (client side, sketch)

```java
Identifier CHANNEL = Identifier.of("clientinfo", "modlist");

StringBuilder sb = new StringBuilder("v1\n");
for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
    ModMetadata m = mod.getMetadata();
    sb.append(m.getId()).append('|')
      .append(m.getName()).append('|')
      .append(m.getVersion().getFriendlyString()).append('\n');
}
byte[] payload = sb.toString().getBytes(StandardCharsets.UTF_8);
// send payload as a custom payload packet on CHANNEL once the player has joined
```
