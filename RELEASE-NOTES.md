# Slayer Best in Bank — Unreleased Release Candidate

This release candidate consolidates the current loadout, supply-planning,
bank-layout, customization, and support improvements into one reviewable update.

## Coherent loadout tiers

- Tier 1 remains the strongest complete owned setup.
- Tier 2 and Tier 3 now start from Tier 1 and introduce progressively different
  fallback combinations instead of unrelated per-slot rankings.
- Higher tiers display only the swaps that differ from the better setup.
- Ranged weapon changes rebuild compatible ammunition.
- One-handed/two-handed changes rebuild the off-hand.
- Required protection, pinned gear, and Low-risk constraints remain active in
  alternative tiers.
- The setting previously called **Choices per slot** is now **Loadout tiers**.

## Combined Low-risk mode

- Low-risk mode now caps the combined guide value of the complete equipment
  loadout.
- Required safety gear and explicitly preferred items remain hard overrides.
- When an override exceeds the cap, remaining slots use the strongest safer
  lower-value choices available.
- The sidebar shows the estimated Tier 1 value against the configured cap.

## Task-scaled trip supplies

- Added Full assignment, Short trip, and Custom kills planning.
- Added Light, Normal, and Extra food/Prayer safety levels.
- Added quantity estimates for food, Prayer support, combat boosts, Goading,
  Prayer regeneration, protection potions, run energy, and cannon ammunition.
- Added Bastion support and Divine-versus-regular boost preference.
- Added **Potion Estimate (BETA)**; disabling it keeps potion recommendations
  while removing their quantity targets.
- Added task-scoped decrease, Auto, and increase controls.
- Optional supplies can be turned off and restored without disappearing from the
  sidebar.
- Profile switches now refresh recommendations and per-account supply
  overrides immediately.

## Stable bank preparation

- Tier 1 equipment and supplies use separate four-column zigzag paths.
- Withdrawn equipment, potions, food, and cannon items reserve their positions
  instead of shifting the next item under the pointer.
- Exact potion-dose widgets are preferred before canonical fallbacks.
- Remaining manual withdrawal counts are shown for quantity-planned supplies.
- Bank open/close, config refresh, strategy changes, and withdrawal bursts use a
  debounced client-thread flow.

## Catalog and scoring hardening

- Added catalog-wide alias, metadata, collision, strategy, and Slayer-master
  consistency checks.
- Curated task profiles retain priority over broad boss aliases.
- Specialized weapon rules propagate through safe fallback profiles.
- Existing location-aware cannon and monster-affinity audits remain enforced.

## Sidebar and support

- Removed the old beta/task-aware subtitle and duplicate Bank Highlights button.
- Bank Highlights remains in the normal plugin settings.
- Added one compact Discord support icon beside the plugin title.
- The support invite opens only after a direct click through RuneLite's standard
  browser handler.
- No background Discord request, tracking parameter, or client-data upload was
  added.

## Validation

- Main source compiles for Java 11 with deprecation warnings treated as errors.
- Full release-candidate suite: **112 tests passed, zero failures**.
- Prohibited API and lifecycle review completed.
- Remaining in-client and screenshot gates are documented in
  [VALIDATION.md](VALIDATION.md).
