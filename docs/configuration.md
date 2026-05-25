# HQM Configuration

The config file is created automatically at first launch:

```
<game-directory>/config/hqm/config.json
```

Delete the file to reset all settings to their defaults.

---

## Hardcore

| Key | Default | Description |
|---|---|---|
| `DEFAULT_LIVES` | `3` | How many lives players start with |
| `HEART_ROT_TIME` | `120` | Seconds until a heart rots away |
| `HEART_ROT_ENABLE` | `false` | Enable the heart rot timer |
| `MAX_LIVES` | `20` | Maximum lives a player can have |

---

## Starting (server start / modes)

| Key | Default | Description |
|---|---|---|
| `AUTO_HARDCORE` | `false` | If true, new worlds automatically activate Hardcore mode |
| `AUTO_QUESTING` | `true` | If true, new worlds automatically activate Questing mode |

---

## General

| Key | Default | Description |
|---|---|---|
| `SPAWN_BOOK` | `false` | Grant a quest book to players when they first spawn into a world |
| `LOSE_QUEST_BOOK` | `true` | Drop the quest book on death. Set to false to keep it in inventory |
| `MULTI_REWARD` | `true` | Allow every party member to claim rewards individually. Set to false to give the party one shared set of rewards |
| `ENABLE_TEAMS` | `true` | Enable team support (note: currently has known bugs) |
| `NBT_SUBSET_FILTER` | `["RepairCost"]` | NBT tags to ignore when comparing items with NBT subset matching |

---

## Loot Bags

| Key | Default | Description |
|---|---|---|
| `ALWAYS_USE_TIER` | `false` | Always show the tier name instead of the individual bag name when opening a reward bag |
| `REWARD_INTERFACE` | `true` | Show a GUI displaying bag contents when a reward bag is opened |

---

## Messages

| Key | Default | Description |
|---|---|---|
| `NO_HARDCORE_MESSAGE` | `false` | Send a server status message when Hardcore Questing mode is off |
| `OP_REMINDER` | `true` | Show a reminder to use `/hqm op` when an operator uses `/hqm edit` |

---

## Edit Mode

| Key | Default | Description |
|---|---|---|
| `USE_EDITOR` | `false` | Automatically enable edit mode when entering worlds in single-player. Has no effect in multiplayer |

---

## Interface — Quest Set Colours

All values use HTML hex format, e.g. `#ffffff`.

| Key | Default | Description |
|---|---|---|
| `COMPLETED_SELECTED_IN_BOUNDS_SET` | `#40bb40` | Colour for a completed quest set when selected |
| `COMPLETED_UNSELECTED_IN_BOUNDS_SET` | `#10a010` | Colour for a completed quest set when not selected |
| `UNCOMPLETED_SELECTED_IN_BOUNDS_SET` | `#aaaaaa` | Colour for an incomplete quest set when selected |
| `UNCOMPLETED_UNSELECTED_IN_BOUNDS_SET` | `#666666` | Colour for an incomplete quest set when not selected |
| `DISABLED_SET` | `#dddddd` | Colour for a disabled quest set |

---

## Interface — Quest Colours

All values use HTML hex format with alpha channel, e.g. `#55FFFFFF` (ARGB order: alpha, red, green, blue).

| Key | Default | Description |
|---|---|---|
| `QUEST_INVISIBLE` | `#55FFFFFF` | Colour for quests that are not yet visible |
| `QUEST_DISABLED` | `#FF888888` | Colour for quests that are visible but not yet accessible |
| `QUEST_COMPLETE` | `#FFFFFFFF` | Colour for completed non-repeatable quests |
| `QUEST_COMPLETE_REPEATABLE` | `#FFFFFFCC` | Colour for completed repeatable quests |
| `QUEST_AVAILABLE` | `#554286f4` | Colour for quests that are available to complete |
| `SINGLE_COLOUR` | `false` | Use a fixed colour for available quests instead of the default animated colour pulse |
