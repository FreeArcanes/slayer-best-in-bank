# Slayer Best in Bank — Combat Task Coverage

Embedded combat-affinity audit for the current **Slayer Best in Bank** release candidate.

This catalog covers every unique Slayer assignment category currently embedded in `SlayerMasterCatalog`. It records only mechanics that are stable for the whole task category: target attributes, special weapon families, preferred melee attack types, and Standard-spellbook elemental weaknesses. When a task can be completed using variants with conflicting weaknesses, the plugin deliberately leaves that field neutral instead of inventing one answer.

- **Unique Slayer-master assignment categories audited:** 116
- **Categories with an encoded task-wide affinity/attribute:** 70
- **Conservative generic categories:** 46
- **Location-aware cannon routes:** 81 (documented separately in `CANNON-TASK-COVERAGE.md`)

## Target-specific weapon families

- **Golembane:** Granite hammer gains its current +30% accuracy and +30% damage on golem targets such as Gargoyles. Barronite mace gains its golem damage bonus.
- **Demonbane:** Silverlight/Darklight, Arclight/Emberlight, Burning claws, Scorching bow, Holy water, and Purging staff + Demonbane spell logic receive their target-specific treatment when appropriate.
- **Dragonbane:** Dragon hunter lance, crossbow, and wand use their current draconic accuracy/damage bonuses.
- **Kalphite/Scabarite:** Keris-family damage is modeled, including the rare triple-damage proc as expected value; Breaching receives its accuracy modifier.
- **Leafbane:** Turoth/Kurask restrictions are enforced and Leaf-bladed battleaxe receives its 17.5% target damage bonus.
- **Ratbane:** Bone mace, Bone shortbow, and Bone staff receive the rat-specific +10 max-hit effect and are rejected outside rat targets.
- **Shade:** Gadderhammer receives its Shade-specific expected damage uplift.
- **Vampyre:** Vampyre-specific weapons and their modern target effects remain part of the task rule.
- **Wilderness weapons:** Charged Craw/Webweaver, Viggora/Ursine, and Thammaron/Accursed weapons receive their +50% Wilderness monster accuracy/damage effect on Revenant tasks.

## Important ranking behavior

- A weapon must support the task's preferred melee attack type unless it has a real target-specific passive that justifies an exception.
- **Gargoyle regression:** Crystal halberd cannot use Crush and has no Golembane passive, so its raw strength can no longer beat Granite hammer simply because its item-sheet stats are larger.
- **Waterfiend exception:** Crush remains the normal melee affinity, but a real Demonbane weapon such as Emberlight can still compete because its target-specific effect is stronger than a generic attack-style filter.
- Curated methods such as Barrage, Venator, and Cannon remain primary; weakness-derived methods are added as alternatives rather than blindly replacing the existing strategy.
- The catalog is intentionally conservative for broad assignment categories such as Birds, Bats, Bears, Spiders, Spiritual creatures, and Fossil Island wyverns, where different valid variants do not share one universal elemental weakness.

## Full assignment audit

| Task | Weapon rule | Melee affinity | Traits | Element | Note |
| --- | --- | --- | --- | --- | --- |
| Aberrant spectres | ANY | — | SPECTRAL,  UNDEAD | Air 50% | Undead/spectral target; Air spells receive the listed elemental weakness. |
| Abyssal demons | DEMONBANE | — | DEMON | — | Demonic target: Demonbane weapon/spell effects are included in scoring. |
| Ankou | ANY | — | UNDEAD | — | Undead target. |
| Aquanites | ANY | — | — | — | Generic / no task-wide special weapon modifier encoded. |
| Araxytes | ANY | — | — | Fire 50% | Araxytes have a strong Fire elemental weakness. |
| Aviansies | ANY | — | FLYING | Air 45% | Flying target with a strong Air elemental weakness. |
| Bandits | ANY | — | — | — | Generic / no task-wide special weapon modifier encoded. |
| Banshees | ANY | — | — | Air 30% | Banshees have a 30% Air elemental weakness. |
| Basilisks | ANY | Crush | — | Earth 40% | Basilisks favour Crush and have a 40% Earth elemental weakness. |
| Bats | ANY | — | FLYING | — | Bat assignments include variants with different Air weakness values; no single task-wide element is forced. |
| Bears | ANY | — | — | — | Bear assignments include variants with different Fire weakness values; no single task-wide element is forced. |
| Birds | ANY | — | FLYING | — | Bird assignments span many variants with different or absent elemental weaknesses; no single task-wide element is forced. |
| Black demons | DEMONBANE | — | DEMON | Water 40% | Demonic target: Demonbane weapon/spell effects are included in scoring. Water Magic receives a 40% weakness. |
| Black dragons | DRAGONBANE | — | DRAGON,  FIERY | Water 50% | Draconic target: Dragonbane effects are included in scoring. Water Magic receives a 50% weakness. |
| Black knights | ANY | — | — | — | Generic / no task-wide special weapon modifier encoded. |
| Bloodveld | DEMONBANE | — | DEMON | — | Demonic target: Demonbane weapon/spell effects are included in scoring. |
| Blue dragons | DRAGONBANE | — | DRAGON,  FIERY | Water 50% | Draconic target: Dragonbane effects are included in scoring. Water Magic receives a 50% weakness. |
| Bosses | ANY | — | — | — | Generic / no task-wide special weapon modifier encoded. |
| Brine rats | RATBANE | — | RAT | — | Rat-bone weapons receive their rat-specific +10 max-hit effect. |
| Catablepon | ANY | — | — | — | Generic / no task-wide special weapon modifier encoded. |
| Cave bugs | ANY | — | — | Fire 50% | Cave bugs have a 50% Fire elemental weakness. |
| Cave crawlers | ANY | — | — | — | Generic / no task-wide special weapon modifier encoded. |
| Cave horrors | ANY | — | — | Fire 30% | Cave horrors have a 30% Fire elemental weakness. |
| Cave kraken | ANY | — | — | Earth 50% | Cave kraken have a 50% Earth elemental weakness. |
| Cave slimes | ANY | — | — | Earth 50% | Cave slimes have a 50% Earth elemental weakness. |
| Chaos druids | ANY | — | — | — | Generic / no task-wide special weapon modifier encoded. |
| Cockatrice | ANY | — | — | — | Generic / no task-wide special weapon modifier encoded. |
| Cows | ANY | — | — | — | Generic / no task-wide special weapon modifier encoded. |
| Crabs | ANY | — | — | — | Generic / no task-wide special weapon modifier encoded. |
| Crawling hands | ANY | — | — | — | Generic / no task-wide special weapon modifier encoded. |
| Crocodiles | ANY | Stab | — | — | Stab is the preferred melee attack type for this target. |
| Custodian stalkers | ANY | — | — | Fire 30% | Mature Custodian stalkers have a Fire elemental weakness. |
| Dagannoth | ANY | — | — | — | Generic / no task-wide special weapon modifier encoded. |
| Dark beasts | ANY | — | — | Earth 60% | Dark beasts have a 60% Earth elemental weakness. |
| Dark warriors | ANY | Crush | — | — | Crush is the preferred melee attack type for this target. |
| Dogs | ANY | — | — | — | Generic / no task-wide special weapon modifier encoded. |
| Drakes | DRAGONBANE | — | DRAGON,  FIERY | Water 50% | Draconic target: Dragonbane effects are included in scoring. Water Magic receives a 50% weakness. |
| Dust devils | ANY | — | — | Air 35% | Dust devils have an Air elemental weakness. |
| Dwarves | ANY | — | — | — | Generic / no task-wide special weapon modifier encoded. |
| Earth warriors | ANY | Crush | — | — | Crush is the preferred melee attack type for this target. |
| Elves | ANY | — | — | — | Generic / no task-wide special weapon modifier encoded. |
| Ents | ANY | — | — | Fire 40% | Ents have a 40% Fire elemental weakness. |
| Fever spiders | ANY | — | — | Fire 25% | Fever spiders have a 25% Fire elemental weakness. |
| Fire giants | ANY | — | FIERY | Water 100% | Fiery target with a 100% Water elemental weakness. |
| Flesh crawlers | ANY | — | — | — | Generic / no task-wide special weapon modifier encoded. |
| Fossil island wyverns | DRAGONBANE | — | DRAGON | — | Fossil Island wyvern variants are draconic but their elemental values vary by variant; Dragonbane is scored without forcing one task-wide element. |
| Frost dragons | DRAGONBANE | — | DRAGON | Fire 100% | Draconic icy target: Dragonbane applies and Fire Magic receives a 100% elemental weakness. |
| Gargoyles | GOLEMBANE | Crush | GOLEM | Earth 40% | Gargoyles are golems: Granite hammer gains Golembane, Crush is favoured, and Earth Magic is an alternative. |
| Ghosts | ANY | — | UNDEAD | Air 50% | Undead target with an Air elemental weakness. |
| Ghouls | ANY | — | — | — | Current Slayer task data does not classify Ghouls with a special target attribute. |
| Goblins | ANY | — | — | — | Generic / no task-wide special weapon modifier encoded. |
| Greater demons | DEMONBANE | — | DEMON | Water 40% | Demonic target: Demonbane weapon/spell effects are included in scoring. Water Magic receives a 40% weakness. |
| Green dragons | DRAGONBANE | — | DRAGON,  FIERY | Water 50% | Draconic target: Dragonbane effects are included in scoring. Water Magic receives a 50% weakness. |
| Gryphons | ANY | — | FLYING | Air 50% | Flying target with a 50% Air elemental weakness. |
| Harpie bug swarms | ANY | — | — | Fire 50% | Harpie bug swarms have a Fire elemental weakness. |
| Hellhounds | DEMONBANE | — | DEMON | Water 50% | Demonic target: Demonbane weapon/spell effects are included in scoring. Water Magic receives a 50% weakness. |
| Hill giants | ANY | — | — | — | Generic / no task-wide special weapon modifier encoded. |
| Hobgoblins | ANY | — | — | — | Generic / no task-wide special weapon modifier encoded. |
| Hydras | DRAGONBANE | — | DRAGON | — | Draconic target: Dragonbane effects are included in scoring. |
| Ice giants | ANY | — | — | Fire 100% | Ice giants have a 100% Fire elemental weakness. |
| Ice warriors | ANY | Crush | — | Fire 100% | Ice warriors favour Crush and have a 100% Fire elemental weakness. |
| Icefiends | DEMONBANE | — | DEMON | Fire 100% | Icefiends are demons: Demonbane passives apply and Fire Magic has a 100% elemental weakness. |
| Infernal mages | ANY | — | — | — | Generic / no task-wide special weapon modifier encoded. |
| Jellies | ANY | — | — | — | Generic / no task-wide special weapon modifier encoded. |
| Jungle horrors | ANY | — | — | — | Generic / no task-wide special weapon modifier encoded. |
| Kalphites | KALPHITE | — | KALPHITE | — | Keris weapons gain Kalphite/Scabarite damage effects; Breaching also gains target accuracy. |
| Killerwatts | ANY | — | FLYING | Air 60% | Flying target with a 60% Air elemental weakness. |
| Kurask | LEAF_BLADED | — | LEAFY | — | Only valid leaf-bane/Slayer weapons can damage this target; Leaf-bladed battleaxe gains a target damage bonus. |
| Lava Dragons | DRAGONBANE | — | DRAGON,  FIERY | Water 50% | Draconic target: Dragonbane effects are included in scoring. Water Magic receives a 50% weakness. |
| Lesser demons | DEMONBANE | — | DEMON | Water 40% | Demonic target: Demonbane weapon/spell effects are included in scoring. Water Magic receives a 40% weakness. |
| Lesser Nagua | ANY | — | SPECTRAL | — | Spectral target; no stable task-wide weapon passive is forced by the current optimizer. |
| Lizardmen | ANY | — | — | — | Generic / no task-wide special weapon modifier encoded. |
| Lizards | ANY | — | — | — | Generic / no task-wide special weapon modifier encoded. |
| Magic axes | ANY | — | — | — | Generic / no task-wide special weapon modifier encoded. |
| Mammoths | ANY | — | — | — | Generic / no task-wide special weapon modifier encoded. |
| Metal dragons | DRAGONBANE | Stab | DRAGON,  FIERY | Earth 50% | Draconic metal target: Dragonbane applies; stab is the melee route and Earth Magic receives a 50% weakness. |
| Minions of Scabaras | KALPHITE | — | KALPHITE | — | Keris weapons gain Kalphite/Scabarite damage effects; Breaching also gains target accuracy. |
| Minotaurs | ANY | — | — | — | Generic / no task-wide special weapon modifier encoded. |
| Mogres | ANY | — | — | — | Generic / no task-wide special weapon modifier encoded. |
| Molanisks | ANY | Crush | — | Earth 60% | Molanisks favour Crush and have a 60% Earth elemental weakness. |
| Monkeys | ANY | — | — | — | Generic / no task-wide special weapon modifier encoded. |
| Moss giants | ANY | — | — | Fire 50% | Moss giants have a Fire elemental weakness. |
| Mutated zygomites | ANY | — | — | — | Generic / no task-wide special weapon modifier encoded. |
| Nechryael | DEMONBANE | — | DEMON | — | Demonic target: Demonbane weapon/spell effects are included in scoring. |
| Ogres | ANY | — | — | — | Generic / no task-wide special weapon modifier encoded. |
| Otherworldly beings | ANY | — | — | — | Generic / no task-wide special weapon modifier encoded. |
| Pirates | ANY | — | — | — | Generic / no task-wide special weapon modifier encoded. |
| Pyrefiends | DEMONBANE | — | DEMON,  FIERY | Water 100% | Demonic/fiery target: Demonbane applies and Water Magic receives a 100% elemental weakness. |
| Rats | RATBANE | — | RAT | — | Rat-bone weapons receive their rat-specific +10 max-hit effect. |
| Red dragons | DRAGONBANE | — | DRAGON,  FIERY | Water 50% | Draconic target: Dragonbane effects are included in scoring. Water Magic receives a 50% weakness. |
| Revenants | ANY | — | UNDEAD,  WILDERNESS | Air 30% | Undead Wilderness target: charged Wilderness weapons gain +50% accuracy/damage, and Air Magic has a 30% elemental weakness. |
| Rockslugs | ANY | — | — | Earth 25% | Rockslugs have a 25% Earth elemental weakness. |
| Rogues | ANY | — | — | — | Generic / no task-wide special weapon modifier encoded. |
| Scorpions | ANY | Slash | — | — | Slash is the preferred melee attack type for this target. |
| Sea snakes | ANY | — | — | — | Generic / no task-wide special weapon modifier encoded. |
| Shades | SHADE | — | SHADE,  SPECTRAL,  UNDEAD | Air 40% | Shades are undead/spectral; Gadderhammer has a Shade-specific damage effect and Air Magic is effective. |
| Shadow warriors | ANY | Crush | — | — | Crush is the preferred melee attack type for this target. |
| Skeletal wyverns | DRAGONBANE | — | DRAGON | Fire 25% | Draconic target: Dragonbane effects are included in scoring. Fire Magic receives a 25% weakness. |
| Skeletons | ANY | Crush | UNDEAD | — | Skeleton variants generally favour Crush, but elemental weakness varies by variant/location; no single task-wide element is forced. |
| Smoke devils | ANY | — | — | Air 30% | Smoke devils have an Air elemental weakness; Burst/Barrage remains the primary multi-target XP method. |
| Sourhogs | ANY | — | — | — | Generic / no task-wide special weapon modifier encoded. |
| Spiders | ANY | — | — | — | Spider assignments can be completed on substantially different spider variants; no single task-wide element is forced. |
| Spiritual creatures | ANY | — | — | — | Elemental weakness is faction-dependent: non-Zaros spiritual creatures use Air weakness while Zarosian variants use a much larger Fire weakness; no single task-wide element is forced. |
| Suqahs | ANY | — | — | Earth 20% | Suqahs have a 20% Earth elemental weakness. |
| Terror dogs | ANY | — | — | — | Generic / no task-wide special weapon modifier encoded. |
| Trolls | ANY | — | — | Fire 50% | Mountain-troll routes have a Fire elemental weakness. |
| Turoth | LEAF_BLADED | — | LEAFY | — | Only valid leaf-bane/Slayer weapons can damage this target; Leaf-bladed battleaxe gains a target damage bonus. |
| TzHaar | ANY | — | — | — | Generic / no task-wide special weapon modifier encoded. |
| Vampyres | VAMPYRE | — | UNDEAD,  VAMPYRE | — | Vampyre-specific weapons are required/strongly favoured; modern Sunspear/Hallowed/Blisterwood effects are scored. |
| Wall beasts | ANY | — | — | — | Generic / no task-wide special weapon modifier encoded. |
| Warped Creatures | ANY | — | — | — | Generic / no task-wide special weapon modifier encoded. |
| Waterfiends | DEMONBANE | Crush | DEMON | Earth 100% | Waterfiends are demons: Demonbane passives apply. Crush is the normal melee affinity, while Earth Magic has a 100% elemental weakness. |
| Werewolves | ANY | — | — | — | Werewolf task: do not prefer Wolfbane in Canifis because it prevents the human citizen from transforming, and human-form kills do not count for the Werewolf Slayer assignment. |
| Wolves | ANY | — | — | — | Generic / no task-wide special weapon modifier encoded. |
| Wyrms | DRAGONBANE | — | DRAGON | Earth 50% | Draconic target: Dragonbane effects are included in scoring. Earth Magic receives a 50% weakness. |
| Zombies | ANY | — | UNDEAD | — | Undead target. |

## Research references

The implementation was cross-checked against current Old School RuneScape Wiki task/monster mechanics and the Wiki DPS calculator data/model, including:

- https://oldschool.runescape.wiki/w/Slayer_task
- https://oldschool.runescape.wiki/w/Slayer_task/Gargoyles
- https://oldschool.runescape.wiki/w/Demonbane_weapons
- https://oldschool.runescape.wiki/w/Draconic_(attribute)
- https://oldschool.runescape.wiki/w/Leaf-bladed_battleaxe
- https://oldschool.runescape.wiki/w/Keris
- https://oldschool.runescape.wiki/w/Passive_effect
- https://oldschool.runescape.wiki/w/Slayer_task/Waterfiends
- https://github.com/weirdgloop/osrs-dps-calc

Best-in-Bank does **not** fetch these sites at runtime. The rules are bundled and reviewable in the plugin source.
