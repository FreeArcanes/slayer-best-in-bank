# Slayer Best in Bank Release-Candidate Validation

Validation date: 2026-08-24

## Current status

The current release candidate is published on a source review branch and is
ready to be pinned by the Plugin Hub update. The remaining manual checks below
are retained as the ongoing in-client regression matrix.

Automated result:

```text
306 tests passed
0 failures
0 errors
```

The complete Gradle task graph was recompiled and rerun with:

```text
.\gradlew.bat clean test
```

Main source targets Java 11 and enforces:

```text
--release 11
-Xlint:deprecation
-Werror
```

The test-only developer launcher still produces an unchecked-operation note.
It is not part of the shipped main plugin source.

## Automated coverage

### Catalog and task routing

- Every registered task alias resolves to its intended curated profile.
- Broad aliases cannot replace specialized profiles.
- Strategy names and required metadata are complete and collision-audited.
- Every embedded Slayer-master assignment has a combat classification and safe
  fallback profile.
- Cannon routes honor known prohibited locations.
- Ancient AoE, elemental weakness, and specialized weapon rules propagate into
  resolved strategies.

### Loadout correctness

- Task-specific weapon effects and melee affinities beat incompatible raw-stat
  choices where appropriate.
- Target-locked weapons are rejected off target.
- Required protection overrides unsafe equipment.
- Two-handed weapons cannot coexist with an off-hand.
- Ranged weapons require compatible ammunition unless they use no ammo slot.
- Tier 2 and Tier 3 remain complete coherent loadouts while displaying only
  changed swaps.
- Weapon alternatives rebuild ammunition and off-hand choices together.

### Low-risk mode

- The cap applies to the combined equipment loadout.
- Pinned equipment remains a hard override.
- Required safety items remain selected even when the cap cannot contain them.
- Alternative loadouts cannot increase the applicable Tier 1 risk ceiling.

### Supply planning

- Full, short, and custom trip lengths cap planned kills correctly.
- Food and Prayer safety levels scale and round correctly.
- Goading and Prayer regeneration can be suggested or disabled.
- Bastion and Divine/regular boost preference ordering is covered.
- Potion estimates can be disabled without removing potion recommendations.
- Task-scoped decrease, Auto, and increase controls preserve safe minimums.
- Profile-backed supply override keys are stable and trigger recommendation
  refreshes.
- Exact potion doses and remaining withdrawal badges are covered.
- Whole-potion, item, cannon-ammunition, worn-ammunition, and supported combat
  cast prices contribute to the modeled trip and GP-per-kill estimate.
- Visible Ava variants apply their documented recovery rates; hidden quiver
  state is not guessed.

### Polished task workflow

- Task-aware Objective suggestions can be applied and restored from the panel.
- Withdrawal checklist entries distinguish banked withdrawals from missing gear.
- Versioned presets round-trip punctuation and Unicode and reject malformed,
  oversized, wrong-version, or incompatible input.
- Supply consumption tracks canonical potion-dose variants and reports only
  observed decreases after the bank closes.
- Boss-preview selection cannot replace the underlying Slayer assignment amount
  used by completion tracking.
- Inactive charge variants and fully degraded Barrows equipment are excluded;
  functional charged and cosmetic/specialty variants remain eligible.

### Bank flow

- Bank open/close and filtered-view transitions are debounced.
- Withdrawal bursts queue one recalculation.
- Strategy changes cannot stack client-thread transitions.
- Bank sessions lock the active plan and debounce explicit refresh requests.
- Pending task/config changes clear on refresh or bank close.
- Closing the bank cancels pending filtered-bank work.
- Four-column zigzag positions are deterministic.
- Withdrawn items retain reserved positions so later items do not shift.

### Preparation focus and capacity

- All, Missing, Gear, and Supplies focus modes cycle deterministically.
- Current inventory, pending Tier 1 gear, potion withdrawals, food, and stacks
  contribute the correct number of planned slots.
- Completed bank exits preserve prepared supply quantities for the active trip;
  potion use and cannon deployment remain satisfied until the next bank opens.
- Gear readiness remains live while the silent supply allowance is active.
- Optional food is reduced before lower-priority optional supplies.
- Required supplies are never capacity-trimmed.
- Plans that cannot fit after optional reductions report their remaining
  overflow.

### Support link

- The support URL is a plain HTTPS `discord.gg` invite.
- It contains no query string, fragment, or tracking parameters.
- The browser is opened only by the sidebar button's action listener.

## Compliance review

The shipped main source was scanned for Plugin Hub review risks.

No use was found of:

- reflection or runtime class loading;
- JNI or native-library loading;
- subprocesses or external program execution;
- runtime source/code downloads;
- sockets, HTTP clients, or background web requests;
- filesystem writes or deletion;
- background or automatic clipboard collection;
- automatic browser navigation;
- automated gameplay actions.

The only external destination is the user-clicked Discord support invite, opened
with RuneLite's `LinkBrowser`. Preset clipboard access occurs only after the
player clicks **Copy preset** or **Import preset**; it is never polled in the
background and no clipboard contents leave the client.

Lifecycle review confirmed paired registration/removal for overlays and the
sidebar navigation button. Bank widgets and filtered-view mappings are cleared
on bank close, logout/world transition, and plugin shutdown.

## In-client checks already confirmed

- [x] Best-in-Bank filtered bank view opens and closes.
- [x] Potion/cannon withdrawals retain stable click positions.
- [x] Quantity controls and estimated supply withdrawals operate locally.
- [x] Discord support icon opens the intended invite after a direct click.
- [x] Preset codec, charge filtering, trip cost, withdrawal checklist, and
      canonical-dose consumption tracking pass automated regression tests.

## Manual release-candidate matrix

Complete these checks in the developer client before publishing:

- [ ] Restart the developer client and confirm the plugin starts with no stale
      bank or task state.
- [ ] Switch RuneLite configuration profiles and confirm settings refresh.
- [ ] Switch accounts or game-mode profiles and confirm task supply overrides do
      not carry across profiles.
- [ ] Test one ordinary melee assignment.
- [ ] Test one ranged or Venator assignment.
- [ ] Test one standard Magic assignment.
- [ ] Test one Ancient Burst/Barrage assignment.
- [ ] Test one cannonable task in an allowed location.
- [ ] Test a mixed-location task in a prohibited cannon location.
- [ ] Test required Slayer protection and a required off-hand.
- [ ] Test one self-ammo ranged weapon and one crossbow/ammunition pairing.
- [ ] Test Tier 1, Tier 2, and Tier 3 with enough owned alternatives.
- [ ] Test Low-risk mode below, exactly at, and above the configured cap.
- [ ] Test Always prefer and Never recommend together.
- [ ] Test Potion Estimate enabled and disabled.
- [ ] Cycle All, Missing, Gear, and Supplies focus views.
- [ ] Change a method/setting with the bank open and verify it waits for
      Refresh without moving the active bank plan.
- [ ] Verify a plan below 28 slots, an optional-supply trim to 28, and a
      required-item overflow above 28.
- [ ] Test zeroed optional supplies and reset them with Auto.
- [ ] Test an empty/partial bank and a fully stocked bank.
- [ ] Close the bank with missing preparation and verify the reminder.
- [ ] Leave fully prepared, place the cannon, drink a potion, and eat food;
      verify preparation stays complete until the next bank opening.
- [ ] Reopen the bank and verify deployed/consumed supplies are required again
      for the next trip.
- [ ] Disable bank highlights and the prep reminder from plugin settings.
- [ ] Apply and restore a suggested Objective with the bank both open and closed.
- [ ] Copy/import a task preset and verify a malformed token is rejected.
- [ ] Complete a short task after drinking a potion and verify exactly one task
      summary appears when the chat option is enabled.
- [ ] Compare the trip-cost card on one crossbow setup and one powered-staff
      setup against current GE guide prices.

## Documentation and packaging gate

Before publishing:

- [ ] Recapture the side-panel screenshot with the new prep focus, bank lock,
      Refresh, and inventory-capacity controls.
- [x] Recapture the settings screenshot with all current sections.
- [x] Recapture the four-column equipment and supply paths.
- [x] Replace README references to older screenshots that no longer match the
      UI.
- [x] Close the developer client and run a clean build.
- [ ] Build the final source/archive from the exact intended commit.
- [ ] Verify `runelite-plugin.properties`, README, release notes, and the Plugin
      Hub manifest all point to that same source state.
