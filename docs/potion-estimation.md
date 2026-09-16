# Potion estimation model

SBIB estimates **doses**, not whole bottles. A four-dose potion in the bank
therefore satisfies a one- to four-dose target without asking the player to
withdraw four separate potions.

## Inputs

- Planned kills from Full assignment, Short trip, or Custom kills.
- The selected encounter and a realistic maximum kills-per-trip cap for bosses.
- The upper (conservative) TTK from the selected owned loadout.
- The player's real Prayer level.
- The total Prayer bonus on the recommended loadout.
- The selected Prayer-potion or Super-restore preference.
- Light, Normal, or Extra safety and the existing per-task manual override.

For normal Slayer, estimated Prayer drain is:

```text
resistance = 60 + 2 × prayer bonus
drain points = active minutes × base points/minute × 60/resistance
restore/dose = floor(prayer level/4) + 7 (Prayer potion)
             = floor(prayer level/4) + 8 (Super restore)
doses = ceil(max(0, drain points - starting prayer) / restore/dose)
```

The base drain distinguishes an offensive-prayer method from one which also
calls for a protection prayer. For calibrated boss encounters, observed doses
per kill replace generic drain and are then scaled for the player's Prayer
level and loadout Prayer bonus.

Prayer-regeneration doses cover eight minutes and restore 66 points over their
effect. When enabled on a long enough trip, that restoration is subtracted from
ordinary Prayer potion/Super restore demand so SBIB does not double-count both.

## Timed effects

| Category | Conservative duration per dose |
|---|---:|
| Divine/ordinary combat or Ranged re-dose | 5 minutes |
| Goading | 6 minutes |
| Antifire | 6 minutes |
| Extended anti-venom+ | 6.3 minutes |
| Poison protection | 6 minutes |
| Prayer regeneration | 8 minutes |

## Calibration evidence

The model uses reproducible guide inputs as anchors rather than treating an
inventory screenshot as an exact consumption rate:

- [Prayer mechanics and drain resistance](https://oldschool.runescape.wiki/w/Prayer)
  and [Prayer potion restoration](https://oldschool.runescape.wiki/w/Prayer_potion).
- [Divine super combat potion](https://oldschool.runescape.wiki/w/Divine_super_combat_potion),
  [extended anti-venom+](https://oldschool.runescape.wiki/w/Extended_anti-venom%2B),
  and [dragonfire potion durations](https://oldschool.runescape.wiki/w/Dragonfire).
- Wiki money-making inputs imply approximately 3.43 restore doses per Araxxor
  kill, 3.33 per Alchemical Hydra kill, 2.13 per Vorkath kill, 2.0 per Zulrah
  kill, 2.0 per Vardorvis kill, 1.6 per Duke Sucellus kill, and 1.45 per
  Abyssal Sire kill at the guide's stated kills per hour.
- The [Cerberus strategy](https://oldschool.runescape.wiki/w/Cerberus/Strategies)
  documents about two Prayer-potion doses per kill when its ghost drain is
  handled correctly.
- The [Fight Caves inventory](https://oldschool.runescape.wiki/w/TzHaar_Fight_Cave/Strategies)
  recommends 13–15 four-dose restores for a full encounter.
- [Gnomonkey's Alchemical Hydra guide](https://www.youtube.com/watch?v=7ehTfsD--gM)
  provides a player-facing inventory and encounter sanity check alongside the
  Wiki's measurable hourly inputs.
- Community reports show the expected variance: [Cerberus players report one
  to three doses per kill](https://www.reddit.com/r/ironscape/comments/hn8583/how_many_prayer_pots_per_cerberus_kill_is_normal/)
  depending on ghost handling and equipment, while [recent first-cape Inferno
  inventories](https://www.reddit.com/r/2007scape/comments/1rid9h9/got_my_inferno_cape_finally_d/)
  trade Super restores against newer Prayer-regeneration potions.
- Archived player/forum reports, such as early [Vorkath kill-time and trip
  observations](https://forums.rs/en/380,381,450,65977970,goto,3.html), were
  used only as range checks because later equipment and balance changes make
  their raw inventories unsuitable as modern calibration anchors.

These anchors are planning baselines, not guarantees. Avoided mechanics,
flicking, altars, supply drops, bone restoration, special attacks, and player
experience can move actual use substantially. The Light/Normal/Extra control
and per-task override remain the final authority.
