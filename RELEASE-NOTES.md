# Slayer Best in Bank — SBIB V1.83

## September 2026 gameplay sync

- Added explicit scoring and target-aware DPS support for the Amulets of Air,
  Water, Earth, and Fire and the combined Elemental amulet. Their hidden +2
  base max hit is applied only to matching elemental spells and before Magic
  damage and elemental-weakness modifiers, matching the live game behavior.
- Confirmed the Necklace of Fangs requires no plugin-specific override: its
  +12 Ranged Accuracy and +1 Ranged Strength flow through RuneLite's current
  equipment stats and existing Ranged scoring.
- Confirmed the consumable Ghommal's Lucky Penny unlock does not require a
  wearable-item recommendation. The plugin does not infer account unlock
  state or incorrectly reserve the ring slot for the consumed passive.
- Audited the September 16 pet update. Its pet behavior, reduced low-level
  energy-potion drops, Mortimer Partner Slayer fix, Arkan Blade reclaim option,
  Medallion of the Deep equipment change, and capitalization fixes do not
  change SBIB's gear, supply, task, or travel recommendation rules.

# Previous public release — SBIB V1.82

This release candidate consolidates the current loadout, supply-planning,
bank-layout, customization, and support improvements into one reviewable update.

## Polished planning and feedback

- Added one-click task-aware Objective switching with a restore action.
- Added modeled trip cost and GP-per-kill estimates for quantity-planned
  supplies, compatible worn ammunition, and supported Magic casts.
- Applied visible Ava recovery rates without guessing hidden Dizana's quiver
  upgrades or inaccessible loaded ammunition.
- Added conservative charge-state filtering for inactive, empty, broken,
  damaged, and fully degraded equipment while retaining usable specialty forms
  such as `(i)`, `(f)`, ornamented, locked, and Max cape variants.
- Added explicit charge reminders for powered weapons, Crystal equipment,
  wilderness weapons, Barrows gear, and Dizana's quiver variants.
- Added validated, versioned task presets for sharing a method, Objective,
  preferences, trip settings, and Low-risk policy.
- Added session-only observed supply consumption and elapsed-time summaries.
- Added an optional single chat summary when an assignment ends or changes.
- Prepared the complete public update notice for **SBIB V1.82**.

## Internal reliability

- Consolidated bank, inventory, and worn-item canonicalization so duplicate
  snapshots no longer repeat equipment metadata and stat lookups.
- Supply ownership now builds exact-variant and canonical views in one pass
  instead of decoding every bank and inventory item twice.
- Centralized strategy cloning so newly added constraints and rankings cannot
  be silently omitted by separate copy blocks.
- Removed redundant broad boss aliases and added a catalog regression guard so
  curated task registrations cannot be silently discarded.

## Preparation quality of life

- Added All, Missing, Gear, and Supplies focus views in the sidebar.
- Packed entries remain visible but visually quieter in the complete views.
- Alternative-only Tier 2/3 slots stay out of the Tier 1 Missing view and are
  labeled as swaps when backups are shown.
- Added combined ready-item and planned-inventory progress.
- Opening the bank locks the active owned loadout and withdrawal order.
- Task, method, profile, and setting changes wait for an explicit Refresh while
  the bank plan is locked.
- Live withdrawals still refresh packed/banked status from the frozen owned
  selection pool.
- A completed bank exit silently preserves that trip's prepared supplies, so
  consumed potions, food, and deployed cannon parts do not become false missing
  preparation; opening the next bank resets the check.
- Added a 28-slot inventory-capacity guard covering current inventory, pending
  Tier 1 gear, and remaining supply withdrawals.
- Optional food and secondary supplies are reduced first when the requested
  plan would not fit.
- Required supplies are never capacity-trimmed; unresolved overflow is shown as
  a warning.
- Added Island of Stone as a recognized Dagannoth cannon-location alias for
  Jormungand's Prison assignments.

## Coherent loadout tiers

- One-handed boss, phase, and special-attack switches now include the best
  owned style-appropriate off-hand when it is not already equipped; two-handed
  switches remain off-hand-free.
- Fixed Shellbane Gryphon preparation: Tortugan shield is now enforced in its
  actual cape slot, a normal off-hand can be selected independently, and the
  boss loadout is automatically adjusted to at least 40 kg equipped weight.
- Re-audited every Slayer-monster row twice against the current OSRS Wiki,
  correcting stale affinities, attributes, and elemental weaknesses for Cave
  kraken, Hellhounds/Cerberus, Crocodiles, Scorpions/Scorpia, Banshees,
  Crawling hands, Flesh crawlers, Jungle horrors, Otherworldly beings, Sea
  snakes, Spiritual creatures, Ankou, Hydras, and the Shellbane Gryphon.
- Replaced several generic Slayer-boss fallbacks with encounter-aware weapon
  methods for Araxxor, Cerberus, Duke Sucellus, Sarachnis, Vardorvis, Abyssal
  Sire, Kalphite Queen, and Vet'ion.
- Tightened melee attack-style classification so weapons such as the Arkan
  blade, rapiers, halberds, whips, staves, and sickles cannot be ranked on
  combat styles they cannot actually use.
- Audited every curated task and boss profile against its current OSRS Wiki
  strategy page, correcting mismatched styles and separating distinct methods.
- Aquanites now compare Soulreaper axe and multi-hit Scythe value against the
  reduced-defence stab route instead of forcing every weapon through Stab.
- Araxyte assignments now rank Crush for the Araxxor main weapon and expose the
  Noxious halberd separately as a hatched-araxyte and mirrorback switch.
- Scythe scoring now accounts for two- and three-hit target sizes, while
  Soulreaper axe scoring includes conservative sustained Soul-stack value.
- Corrected Wiki-aligned method details for Gryphons, Kalphites, Trolls, Warped
  creatures, Wyrms, Cerberus, Duke Sucellus, Sarachnis, Vardorvis, Abyssal Sire,
  and Kalphite Queen.
- Duke Sucellus now applies his reduced Demonbane effectiveness rather than the
  unrestricted demon multiplier.
- Curated weapon names are now true close tie-breakers instead of large bonuses
  that could overpower attack speed, accuracy, Strength, or target passives.
- One-handed weapon plus off-hand packages are compared against complete
  two-handed setups before choosing Tier 1.
- Ancient Magicks methods now score Virtus robes with their additional 3%
  damage per piece, correctly ranking them above Ancestral for Burst and
  Barrage loadouts while leaving ordinary Magic rankings unchanged.
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

- Added a Prayer-restore dropdown that strictly selects either Prayer potions
  or Super restores for trip preparation and bank matching.
- Added an opt-in Slayer-bracelet switch with separate Expeditious and
  Slaughter preferences.
- Added the Crystal chime as required preparation for warped creatures.
- Added an explicit Max cape home-teleport preference alongside the existing
  Dramen staff, Lunar staff, Quest cape, and route-specific travel choices.
- Max cape travel matching now requires the actual base Max cape and no longer
  accepts Magic or combat-oriented max-cape variants without its utility.
- Bank scans now identify boxed Dwarf cannon sets and explain that they must be
  exchanged at a Grand Exchange clerk; the four usable parts remain required.
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
- Full release-candidate suite: **304 tests passed, zero failures**.
- Prohibited API and lifecycle review completed.
- Remaining in-client and screenshot gates are documented in
  [VALIDATION.md](VALIDATION.md).
