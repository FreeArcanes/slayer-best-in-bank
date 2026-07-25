# Slayer Best-in-Bank 1.0.0-beta.2 — UI Overhaul

Beta 2 keeps the RC2 recommendation/bank logic intact and replaces the sidebar presentation with a cleaner, lower-noise RuneLite dashboard.

## UI overhaul

- Rebuilt the sidebar around a clear scan order: **Task → Readiness → Tier 1 loadout → Trip supplies → optional detail**.
- Removed the old horizontal section-header treatment that could visually collide/overlap at RuneLite's narrow panel width.
- Added a compact current-task hero with combat-style badge, method, location, rationale, and method switcher.
- Added a dedicated readiness card with gear/supply counters, protection/ammo checks, spell status, and concise critical warnings.
- Replaced long equipment-list rows with compact rounded item cards.
- Tier 2 / Tier 3 choices are hidden by default behind **Show backups** so Tier 1 remains the visual focus.
- Item reasoning stays available by clicking a loadout row instead of permanently consuming vertical space.
- Rebuilt supplies as consistent item rows with clear **PACKED / PACKED+ / BANK / MISSING** state badges.
- Moved long task notes, safety notes, master assignments, and alternate-method text into a collapsed **Task notes & safety** section.
- Replaced the stock wide scrollbar with a slim rounded dark scrollbar with hidden arrow buttons.
- Reworked spacing, typography, card borders, hover states, muted text, and status colors for a modern RuneLite-native look.
- Bank-highlight control was reduced to a compact rounded state control in the header.

## Behavior retained from RC2

- Consumables remain visible in the bank while packed and more remain banked.
- Tier 1 + supplies stay together in the primary filtered-bank section.
- Tier 1 gear remains stable while withdrawing/equipping.
- Raw/burnt food stays excluded.
- Smart supplies, whole-loadout compatibility, master/task coverage, bank-close prep reminders, protection rules, and strategy switching remain unchanged.

## Version

`1.0.0-beta.2`
