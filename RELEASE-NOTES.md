# Slayer Best in Bank 1.0.0-beta.3 — Cannon Task Intelligence

Beta 3 expands Best-in-Bank's strategy engine with a location-aware dwarf multicannon catalog and promotes the full cannon into the Tier 1 trip loadout whenever a cannon method is selected.

## Cannon task audit

- Added a central cannon-route catalog with **81 verified task/route entries** cross-checked against current Old School RuneScape Wiki Slayer task/location guidance.
- **Smoke devils now default to `Barrage + cannon lure`**, matching the high-XP method where the cannon pulls the room and Burst/Barrage clears the stack.
- Cannon-capable tasks advertise a **Cannon + ...** method even when their original curated profile did not mention the cannon.
- RuneLite Slayer `taskLocation` is used for location-locked assignments so a monster being cannonable somewhere does not automatically mean the assigned location is cannonable.
- Existing curated cannon methods now inherit the actual assigned cannonable location. A Dagannoth assignment in Jormungand's Prison no longer still tells the player to go to the Lighthouse.

### Important location corrections

- **Dagannoth:** Lighthouse, Jormungand's Prison, and Waterbirth Island Dungeon are cannon routes; Catacombs of Kourend is not.
- **Shades:** Sepulchre of Death / Mort'ton-outskirts route is supported; Shade Catacombs and Catacombs of Kourend are excluded.
- **Metal dragons:** Brimhaven, Isle of Souls (iron dragons), and Lithkren Vault (adamant/rune dragons) are supported; Ancient Cavern mithril dragons and Catacombs are not cannon routes.
- Existing location rules remain for Bloodveld, Dust devils, Waterfiends, Wyrms, Lizardmen, Lesser Nagua, Fire giants, Nechryael, Abyssal demons, and other mixed-location tasks.

See [CANNON-TASK-COVERAGE.md](CANNON-TASK-COVERAGE.md) for the embedded route list.

## Full cannon as Tier 1 gear

A selected cannon method now requires and displays the complete dwarf multicannon directly inside the Tier 1 Loadout:

- Cannon base
- Cannon stand
- Cannon barrels
- Cannon furnace

Additional behavior:

- The four cannon parts count toward the **GEAR** readiness total instead of being hidden inside the supplies count.
- Cannonballs / granite cannonballs remain a required **Trip Supply**.
- Cannon parts and ammunition receive Tier 1 treatment in the filtered bank.
- Missing cannon components block the Ready-to-leave-bank state.
- Regular and ornamented cannon parts remain separate complete sets; the optimizer never recommends a mixed regular/ornamented cannon.

## Safety / UX

- Trip supplies do not duplicate cannon components after the components are promoted into the Tier 1 loadout.
- The filtered-bank Tier 1 heading mentions cannon only when the selected setup actually contains cannon gear.
- Cannon recommendations remain advisory and manual. The plugin never deploys, loads, withdraws, or interacts with the cannon for the player.

## Validation

- Main source compiles under Java 11 against the bundled RuneLite client with `-Xlint:deprecation -Werror`.
- Cannon regression harness: **59/59 checks passed**.
- Harness repeated **4 consecutive times with 0 failures**.
- Catalog contains **81 cannon routes** after location-safety corrections.
