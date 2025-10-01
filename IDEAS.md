## LYIVX's Tweaks — QoL Ideas Backlog

A curated backlog of quality-of-life features to consider. Each item includes a short description and a tiny example to clarify behavior. Pick and prioritize as needed.

### Lock/Favorite Slots
1. [ ] Done
- Description: Allow marking specific inventory or hotbar slots as protected from sorting, stacking, or transfers.
- Example: Ctrl+Right-Click a slot to toggle a lock icon; sorting skips locked slots.

### Deposit All Except…
1. [ ] Done
- Description: One-click deposit all items into the open container except hotbar, favorites, or equipment.
- Example: Press a “Deposit All (Except Favorites/Hotbar)” button; only non-favorite items move.

### Undo Last Transfer
1. [ ] Done
- Description: Revert the most recent inventory operation (sort, stack, deposit) by restoring previous state.
- Example: After Quick-Stack, press “Undo” to pull items back from the chest into previous slots.

### Inventory Search + Filters
1. [ ] Done
- Description: Live filter inventory by name, tag, mod source, or rarity; optionally move all matches.
- Example: Type “potion” to dim non-matching items; click “Move Matching to Chest.”

### Custom Sorting Rules
1. [ ] Done
- Description: Users define sort priority (rarity, type, value, mod id, freshness, durability).
- Example: Rules JSON: [rarity desc, type asc, name asc]. Sort respects this ordering.

### Pickup Filters/Blacklist
1. [ ] Done
- Description: Block auto-pickup of junk or blacklist specific items; optional auto-void.
- Example: Add “rotten_flesh” to blacklist; picking it up shows a small “blocked” toast.

### Auto-Restock From Nearby Chests
1. [ ] Done
- Description: Maintain target counts for consumables/ammo/blocks by refilling from nearby containers.
- Example: Keep 64 torches and 16 food in hotbar; opening a chest auto-restocks to targets.

### Smart Stack to Tagged Chests
1. [ ] Done
- Description: Tag chests (e.g., Ores/Blocks/Loot) and route items automatically.
- Example: Shift+Click “Tag: Ores” on a chest; Quick-Stack routes ingots/ore to it.

### Container Preview on Hover
1. [ ] Done
- Description: Peek a chest’s top N items and capacity via tooltip without opening.
- Example: Hover a chest; shows “Ores Chest (54/54): iron x64, gold x32, coal x12 …”.

### Loadouts/Presets
1. [ ] Done
- Description: Save and hot-swap gear/hotbar/building kits as named profiles.
- Example: Press a hotkey to switch to “Builder Kit” presets (blocks, tools) instantly.

### Right-Click Quality Transfers
1. [ ] Done
- Description: Right-click to move same-type stacks across inventories, or to fill partial stacks.
- Example: Right-click a torch stack in chest to merge all torches into your partial stack.

### Radial Quick-Use Menu
1. [ ] Done
- Description: Hold a key to open a radial for quick-select favorites (potions/tools/blocks).
- Example: Hold R; flick to “Strength Potion”; release to use.

### Trash Slot + Rules
1. [ ] Done
- Description: Safe trash with confirm; define “always-trash” items. Optional auto-sell near trader.
- Example: Drag unwanted item into trash; confirm dialog appears unless item is whitelisted.

### Crafting Queue + Pinning
1. [ ] Done
- Description: Pin recipes, queue crafts, set craft-all caps; craft from nearby containers.
- Example: Pin “Ladders”; click “Craft 3x” pulls sticks from chests in range.

### Auto-Compact Items
1. [ ] Done
- Description: Auto-convert currencies/materials (e.g., nuggets↔ingots) on pickup or on demand.
- Example: Click “Compact” to convert 9 nuggets → 1 ingot if space allows.

### Better Splitting (Scroll/Modifiers)
1. [ ] Done
- Description: Use mouse wheel or modifier keys to split stacks by 1/5/half efficiently.
- Example: Shift+Scroll down on a stack to drop 1 item per tick into chest.

### Durability/Uses Overlay
1. [ ] Done
- Description: Show uses left, break-time, or DPS/efficiency deltas on item tooltips.
- Example: Tooltip: “Uses left: 112” or “+15% vs current pickaxe.”

### Buff/Consumable Helpers
1. [ ] Done
- Description: Auto-use or reminders for health/hunger/air; cooldown bars; best-potion selection.
- Example: Below 30% HP, auto-use best available healing potion (configurable).

### Chest Finder/Search
1. [ ] Done
- Description: Global search to locate where an item exists across player-chested storage.
- Example: Search “Ender Pearl” → highlights and pings the chest containing it.

### Distribution Tools (Multiplayer)
1. [ ] Done
- Description: Split stacks fairly among nearby party members or per-role quotas.
- Example: “Distribute 64 rockets” → auto-gives 16 to each of 4 players.

### Per-World/Per-Player Profiles
1. [ ] Done
- Description: Separate configs per world/player; import/export shareable JSON.
- Example: Export your sorting rules; a friend imports to match your setup.

### Comprehensive Keybinds
1. [ ] Done
- Description: Expose keybinds for all actions (smart deposit, restock, sort presets 1–3).
- Example: Bind Alt+1/2/3 to trigger specific sort modes instantly.

### Controller-Friendly Flow
1. [ ] Done
- Description: Radial menus, larger focus targets, hold-to-transfer speed curves for controllers.
- Example: Hold A on a stack to speed-transfer; release to stop.

### Accessibility Options
1. [ ] Done
- Description: Font scaling, colorblind-friendly rarity colors, high-contrast focus outlines.
- Example: Toggle “Deuteranopia palette” for rarity and UI accents.

### Performance Toggles
1. [ ] Done
- Description: Lightweight mode for toasters; reduce animations, throttle scans, lazy tooltips.
- Example: Enable “Low Impact Mode” in big storage rooms to keep FPS smooth.
