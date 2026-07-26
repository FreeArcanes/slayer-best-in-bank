# Slayer Best in Bank 1.0.0-beta.3 Validation

Validation date: 2026-07-25

## Scope

Beta 3 changes were validated around:

- location-aware cannon task routing;
- Smoke Devil barrage + cannon behavior;
- correct suppression in known no-cannon locations;
- Dagannoth Jormungand/Lighthouse/Waterbirth handling;
- Shade and Metal dragon location corrections;
- existing curated cannon strategy location overrides;
- full four-part cannon Tier 1 presentation/readiness integration;
- cannon ammunition requirements;
- preservation of Java 11 / RuneLite API compatibility.

## Compile gate

All main Java source was rebuilt with:

```text
--release 11
-Xlint:deprecation
-Werror
```

against the bundled RuneLite 1.12.34 shaded client/API.

Result:

```text
MAIN_COMPILE_WERROR_OK
```

No deprecation warning is present in the shipped main plugin source.

## Cannon functionality harness

The standalone regression harness checks 59 invariants, including:

- Smoke Devils expose cannon and retain Ancient AoE;
- Smoke Devils use cannon as the primary barrage lure strategy;
- Bloodveld Catacombs suppress cannon while Meiyerditch/Iorwerth allow it;
- Dust Devil Catacombs suppress cannon while cannonable Smoke Dungeon routes remain available;
- Dagannoth Catacombs suppress cannon;
- Dagannoth Lighthouse, Jormungand's Prison, and Waterbirth Island Dungeon allow cannon;
- location-locked existing cannon strategies display the assigned location;
- Waterfiend Ancient Cavern vs Iorwerth behavior;
- Wyrm Karuulm vs Neypotzli behavior;
- Lizardmen Canyon behavior;
- Lesser Nagua Tapoyauik vs Neypotzli behavior;
- Shade Catacombs suppression and Sepulchre/Mort'ton route support;
- Metal dragon Ancient Cavern/Catacombs suppression;
- Brimhaven, Isle of Souls, and Lithkren metal-dragon cannon routes;
- Drakes, Hydras, Skeletal Wyverns, Custodian Stalkers and newer task coverage;
- known no-cannon categories remain excluded;
- cannon strategy name detection.

Final result, repeated four consecutive times:

```text
PASS_1 CANNON_HARNESS_OK pass=59 fail=0 routes=81
PASS_2 CANNON_HARNESS_OK pass=59 fail=0 routes=81
PASS_3 CANNON_HARNESS_OK pass=59 fail=0 routes=81
PASS_4 CANNON_HARNESS_OK pass=59 fail=0 routes=81
```

## Project test-source gate

The actual repository test sources (`GearScorerTest` and `CannonTaskCatalogTest`) were compiled and executed against the locally available RuneLite client/API using a minimal local JUnit-compatible runner (the temporary runner/stubs are not shipped).

Result:

```text
TESTS_OK pass=30 fail=0
```

## Packaged-source gate

The final `1.0.0-beta.3` source ZIP was extracted into a clean directory and rebuilt independently with the same Java 11 warnings-as-errors gate. The 59-check cannon harness was then executed against that extracted package.

Result:

```text
PACKAGE_COMPILE_WERROR_OK
CANNON_HARNESS_OK pass=59 fail=0 routes=81
```

## Full Gradle test

The release/update helper runs the repository's normal `gradlew.bat clean test` suite on the Windows RuneLite development machine before launching or publishing the new commit. The sandbox validation uses the locally available RuneLite client/API because the Gradle wrapper cannot download its distribution when outbound access is unavailable.
