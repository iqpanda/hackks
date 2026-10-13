# Private Client (Fabric, Minecraft 26.2)

Client-side mod for your own private/single-player worlds.

## Features
- **Storage ESP**: chests, trapped chests, ender chests, barrels, shulker boxes (each a different color), through walls
- **Ore ESP**: ancient debris, diamond ore, deepslate diamond ore, deepslate emerald ore (each toggleable)
- **Freecam** (G): detached camera, WASD + Space/Shift, Ctrl to sprint
- **Spectator**: menu buttons run `/gamemode spectator|creative|survival` (needs cheats on in your world)
- **Anti-knockback**: ignores server velocity packets aimed at you
- **Menu** (Right Shift): toggles, scan range, freecam speed. Settings saved to `config/privateclient.json`

## Build
Needs JDK 25 and Gradle 9.3+.

    gradle wrapper        # once, to generate ./gradlew
    ./gradlew build

The jar is in `build/libs/privateclient-1.0.0.jar`. Put it in your `mods` folder with
Fabric Loader 0.19.3+ and Fabric API 0.150.1+26.2 for Minecraft 26.2.

## STATUS: NOT COMPILED OR RUN
This was written without access to the 26.2 jars, so names were taken from the 26.x porting
notes and memory. If something fails, fix in this order:

1. **Plugin/Loom not found**: in `gradle.properties` try `loom_version=1.15.5` or `1.15.2`.
2. **`EspRenderer.java` (most likely)**: the 26.x rendering API changed.
   - `LevelRenderEvents.DEBUG_RENDER` might be named differently or may only fire with debug
     rendering. Try `END_MAIN`, `AFTER_ENTITIES`, or whatever the autocomplete shows.
   - `net.minecraft.gizmos.Gizmos` / `GizmoStyle` / `setAlwaysOnTop()` may have other names.
3. **`ClientPacketListenerMixin`**: `packet.getId()` may be `packet.id()`.
4. **`KeyboardInputMixin`**: field names `keyPresses` / `moveVector` on `ClientInput`.
5. **`CameraMixin`**: method `setup` and `setPosition(double,double,double)`.
6. **`PrivateClient`**: `KeyMapping.Category.register(...)` and the KeyMapping constructor.

Mixins use `defaultRequire = 1`, so a wrong target name crashes at launch with a clear
message naming the exact method.

To check a real name quickly, run `javap -p` on the deobfuscated jar in
`~/.gradle/caches/fabric-loom/`.

## Known limits
- Explosion knockback is not blocked, only direct-hit/damage knockback.
- In freecam your own player model isn't drawn, and attack/use keys still act on the real player.
- The menu uses stock buttons, not a custom-painted GUI.
