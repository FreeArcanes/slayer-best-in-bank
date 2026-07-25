# Slayer Best-in-Bank 1.0.0-beta.2 Validation

## Scope

Beta 2 is primarily a sidebar UI overhaul on top of the already smoke-tested RC2 bank interaction fixes. The release gates therefore verify that the UI refactor still compiles against RuneLite and that the existing recommendation/safety regression suite remains intact.

## Environment

- Java source level: 11
- RuneLite client/API: bundled 1.12.34 shaded client from the uploaded development checkout
- Shipped main source compiled with `-Xlint:deprecation -Werror`

## Four final-code validation passes

Each pass used a fresh output directory and performed:

1. Full main-source compile with Java 11 and deprecation warnings treated as errors.
2. Compile of the existing `GearScorerTest` regression suite.
3. Execution of all 24 regression methods through a standalone test harness.
4. UI source invariants verifying the modern scrollbar, collapsed backups, collapsed task details, trip-supply section, and removal of the old overlapping loadout-heading implementation.

Results:

```text
PASS_1: main_compile=WERROR_OK FUNCTIONALITY_HARNESS_OK pass=24 fail=0 ui_invariants=OK
PASS_2: main_compile=WERROR_OK FUNCTIONALITY_HARNESS_OK pass=24 fail=0 ui_invariants=OK
PASS_3: main_compile=WERROR_OK FUNCTIONALITY_HARNESS_OK pass=24 fail=0 ui_invariants=OK
PASS_4: main_compile=WERROR_OK FUNCTIONALITY_HARNESS_OK pass=24 fail=0 ui_invariants=OK
```

## Regression coverage retained

The 24-method harness covers task/profile coverage, current Slayer-master pools, Ancient AoE thresholds, mandatory protection, 2H/off-hand safety, ranged ammo behavior, stable Tier 1 withdrawals, smart consumable states, dragonfire/antifire behavior, and unsafe food filtering.

## Live-client gate

Swing compilation and logic regression tests cannot fully reproduce RuneLite's actual side-panel rendering at every Windows DPI/font scale. The recommended live check is to open the plugin panel at normal RuneLite width and verify task text, readiness, loadout rows, backup expansion, item-reason expansion, supplies, task-note expansion, and scrollbar appearance.

## Package verification

After the four final-code passes, the source directory was zipped, extracted into a clean directory, and validated again. The extracted package also reported:

```text
PASS_package: main_compile=WERROR_OK FUNCTIONALITY_HARNESS_OK pass=24 fail=0 ui_invariants=OK
PACKAGE_TREE_MATCH_OK
```

This verifies that the downloadable source package contains the same source tree that passed the release gates.
