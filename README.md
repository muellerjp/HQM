
# Hardcore Questing Mode

HQM is a minecraft mod that provides a questing system that
modpacks can utilize to guide players or reward progress.

HQM has a couple supporting features to complement the questing system:
- Teams: Support for teams of players that share quest progress
- Lives: Support for a hardcore-like system with lives that can be gained through items
- Loot bags: Openable reward items with loot tables that work slightly differently to vanilla loot tables 
- Reputations: Support for creating stats that can be used with quest rewards and quest conditions

The latest versions of the mod support fabric, forge, and neoforge,
and can be downloaded from [curseforge](https://www.curseforge.com/minecraft/mc-mods/hardcore-questing-mode).

## How to Build

### Prerequisites

- JDK 21 (for NeoForge) or JDK 17 (for Fabric/Forge)
- No other setup required — Gradle downloads all dependencies automatically

### Choosing a platform

The active platform is controlled by the `platform` property in [`gradle.properties`](gradle.properties):

```properties
platform=neoforge   # fabric | forge | neoforge | all
```

Override it for a single command without changing the file:

```bash
./gradlew <task> -Pplatform=fabric
```

### Building

```bash
# Build the active platform
./gradlew :<platform>:build

# Examples
./gradlew :neoforge:build
./gradlew :fabric:build
./gradlew :forge:build
```

### Running the client

```bash
# Uses the platform set in gradle.properties
./gradlew runClient

# Override platform for this run only
./gradlew runClient -Pplatform=fabric
```

### NeoForge: loading companion mods at runtime

The NeoForge run client can optionally load local builds of companion mods.
Set any of the following in `gradle.properties` (or pass as `-P` flags):

```properties
# Paths to local sibling project checkouts
minecoloniesProjectDir=C:/path/to/minecolonies
tfcProjectDir=C:/path/to/TerraFirmaCraft
tfcMinecolCompatProjectDir=C:/path/to/tfc-minecol-compat
```

When a path is set and its `build/libs` directory contains a built jar, that jar
is copied into the run directory before the client starts. When no local path is
set, Maven artifacts are used as a fallback (see `minecolonies_maven_version` and
`tfc_maven_version` in `gradle.properties`).

