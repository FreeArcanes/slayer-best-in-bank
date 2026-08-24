package com.freearcanes.slayergear;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

final class TaskProfiles
{
	private static final AliasCatalog<SlayerTaskProfile> PROFILES = new AliasCatalog<>(TaskProfiles::normalize);

	static
	{
		register(profile("aberrant-spectres", "Aberrant spectres",
				"Air Magic exploits their elemental weakness; cannon-assisted combat remains a fast route in cannonable locations.",
				"A Slayer helmet or nose peg is required. Protect from Magic supports the offensive method; a separate no-prayer method favors magic defence.",
				elementalMagic("Air Magic", "Slayer Tower / Stronghold Slayer Cave",
					"Air spells exploit the current 50% elemental weakness."),
				melee("Cannon + melee", "Stronghold Slayer Cave / Deepfin Mine", AttackType.SLASH,
					"With Protect from Magic active, ranks offensive melee gear while a cannon supplies extra hits.", "slayer helm", "nose peg"),
				meleeMagicDef("No-prayer defensive melee", "Slayer Tower / Deepfin Mine", AttackType.SLASH,
					"Without Protect from Magic, trades some damage for magic defence.", "slayer helm", "nose peg")),
			"aberrant spectres", "aberrant spectre");

		register(profile("abyssal-demons", "Abyssal demons",
				"Multi-target Catacombs methods are ranked ahead of ordinary melee.",
				"Use Protect from Melee when stacking demons.",
				ancients("Catacombs barrage", "Catacombs of Kourend",
					"Highest XP path: prioritize magic damage and prayer because their magic defence is low."),
				venator("Catacombs Venator", "Catacombs of Kourend",
					"Lower-effort multi-target option when a Venator bow is owned."),
				demonMelee("Demonbane melee", "Catacombs / Slayer Tower",
					"Fallback for accounts without an eligible multi-target setup.")),
			"abyssal demons", "abyssal demon");

		register(profile("ankou", "Ankou",
				"Multi-target Magic or Venator setups beat single-target combat in the Catacombs.",
				"Prayer is optional at high defence.",
				ancients("Catacombs barrage", "Catacombs of Kourend",
					"Fast multi-target Slayer experience."),
				venator("Catacombs Venator", "Catacombs of Kourend",
					"Low-effort multi-target fallback."),
				melee("Single-target melee", "Catacombs of Kourend", AttackType.SLASH,
					"Budget fallback when AoE options are unavailable")),
			"ankou", "ankous");

		register(profile("aquanites", "Aquanites",
				"Open with slash to sever the lure, then compare the Wiki-listed melee and Ranged methods.",
				"They attack with Magic, so magic defence and Protect from Magic are useful.",
				GearStrategy.builder()
					.name("Lure sever + melee")
					.location("Ynysdail Cavern")
					.rationale("Models the slash opener, then ranks Soulreaper/Scythe and the reduced-defence stab options from the strategy guide.")
					.combatStyle(CombatStyle.MELEE)
					.attackType(AttackType.BALANCED)
					.targetTrait(TargetTrait.SCYTHE_TWO_HIT)
					.magicDefenceWeight(0.28)
					.preferredItem("soulreaper axe")
					.preferredItem("scythe of vitur")
					.preferredItem("ghrazi rapier")
					.preferredItem("blade of saeldor")
					.preferredItem("osmumten's fang")
					.preferredItem("oathplate")
					.build(),
				ranged("Ranged", "Ynysdail Cavern",
					"Wiki-listed alternative using the strongest owned Ranged setup.",
					"toxic blowpipe", "twisted bow", "bow of faerdhinen", "zaryte crossbow", "crystal bow")),
			"aquanites", "aquanite");

		register(profile("araxytes", "Araxytes",
				"Cannon and Venator bow is the current high-XP normal-task method.",
				"Bring venom protection.",
				venator("Cannon + Venator", "Morytania Spider Nest", "Top multi-target XP method."),
				GearStrategy.builder()
					.name("Araxxor - Crush melee")
					.location("Araxxor's lair")
					.rationale("Boss route: ranks the Wiki's main-hand Crush options, including Scythe multi-hit value.")
					.combatStyle(CombatStyle.MELEE)
					.attackType(AttackType.CRUSH)
					.targetTrait(TargetTrait.ARAXXOR)
					.targetTrait(TargetTrait.SCYTHE_THREE_HIT)
					.build(),
				GearStrategy.builder()
					.name("Araxxor - Noxious halberd switch")
					.location("Araxxor's lair")
					.rationale("Dedicated Araxyte and mirrorback switch; it is not treated as Araxxor's default main weapon.")
					.combatStyle(CombatStyle.MELEE)
					.attackType(AttackType.SLASH)
					.requiredWeapon("noxious halberd")
					.targetTrait(TargetTrait.ARAXXOR)
					.build(),
				melee("Melee fallback", "Morytania Spider Nest", AttackType.CRUSH,
					"Uses the best offensive melee gear owned")),
			"araxytes", "araxyte");

		register(profile("aviansies", "Aviansies",
				"Aviansies require Ranged or Magic; this profile defaults to Ranged.",
				"God protection and Protect from Missiles may be needed in God Wars Dungeon.",
				ranged("Ranged", "God Wars Dungeon", "Ranks ranged damage and accuracy.")),
			"aviansies", "aviansie");

		register(profile("basilisks", "Basilisks",
				"Uses a one-handed setup with mandatory gaze protection.",
				"A mirror shield or V's shield is required.",
				GearStrategy.builder()
					.name("Protected melee")
					.location("Fremennik Slayer Dungeon / Jormungand's Prison")
					.rationale("Prioritizes offensive melee gear while enforcing a valid shield.")
					.combatStyle(CombatStyle.MELEE)
					.attackType(AttackType.CRUSH)
					.requiredOffhand("mirror shield|v's shield")
					.preferredItem("slayer helm")
					.build()),
			"basilisks", "basilisk");

		register(profile("black-demons", "Black demons",
				"Demonbane melee is prioritized; profitable gorilla variants need more specialized switching.",
				"Protect from Melee for ordinary black demons.",
				demonMelee("Demonbane melee", "Catacombs / Chasm of Fire",
					"Emberlight and Arclight receive explicit demonbane priority."),
				ranged("Ranged fallback", "Catacombs / Chasm of Fire",
					"Alternative if the owned ranged setup scores better for comfort.")),
			"black demons", "black demon");

		register(profile("black-dragons", "Black dragons",
				"Dragonbane Ranged is preferred, followed by dragonbane stab melee.",
				"Use adequate dragonfire protection.",
				dragonRanged("Dragonbane Ranged", "Brutal black dragons / regular dragons"),
				dragonMelee("Dragonbane stab", "Baby / regular black dragons")),
			"black dragons", "black dragon");

		register(profile("bloodveld", "Bloodveld",
				"Cannon and Venator bow at mutated Bloodvelds is prioritized for XP.",
				"Protect from Melee prevents their magic-based melee damage. A separate no-prayer method favors magic defence.",
				venator("Cannon + Venator", "Meiyerditch Laboratories / Iorwerth Dungeon",
					"Current top multi-target XP method when a Venator bow is owned."),
				melee("Cannon + melee", "Meiyerditch Laboratories", AttackType.SLASH,
					"Protect from Melee nullifies incoming damage, so this method ranks offensive melee gear."),
				meleeMagicDef("No-prayer defensive melee", "Stronghold Slayer Cave / Slayer Tower", AttackType.SLASH,
					"When not using Protect from Melee, trades some damage for the Wiki-recommended magic defence.")),
			"bloodveld", "bloodvelds");

		register(profile("blue-dragons", "Blue dragons",
				"Dragonbane Ranged is preferred; stab melee is the fallback.",
				"Use adequate dragonfire protection.",
				dragonRanged("Dragonbane Ranged", "Taverley Dungeon / Vorkath"),
				dragonMelee("Dragonbane stab", "Taverley Dungeon")),
			"blue dragons", "blue dragon");

		register(profile("boss", "Boss task",
				"Boss assignments vary; this safe fallback ranks broadly useful on-task melee gear.",
				"Open the sidebar explanation and verify the specific boss mechanics.",
				melee("General boss melee", "Boss-dependent", AttackType.BALANCED,
					"Fallback profile for the variable boss assignment category")),
			"boss", "bosses");

		register(profile("cave-horrors", "Cave horrors",
				"Fast melee setup with required scream protection.",
				"Wear a witchwood icon for the recommended melee setup; safe-spot/prayer methods can differ.",
				melee("Melee", "Mos Le'Harmless Caves", AttackType.SLASH,
					"Cannon-compatible melee setup", "witchwood icon")),
			"cave horrors", "cave horror");

		register(profile("cave-kraken", "Cave kraken",
				"Kraken can only be damaged effectively with Magic.",
				"Magic defence is not a priority; maximize damage.",
				magic("Magic", "Kraken Cove", "Prioritizes magic damage, then accuracy and prayer.")),
			"cave kraken", "cave krakens", "the cave kraken boss", "kraken");

		register(profile("dagannoth", "Dagannoth",
				"Venator or cannon-assisted combat is favored for normal Dagannoth tasks.",
				"Protection choice depends on location.",
				venator("Venator multi-target", "Lighthouse / Catacombs",
					"Low-effort multi-target XP when the bow is owned."),
				melee("Cannon + melee — Lighthouse", "Lighthouse", AttackType.SLASH,
					"Classic high-throughput cannon route."),
				melee("Cannon + melee — Island of Stone", "Island of Stone",
					AttackType.SLASH,
					"Cannon route through Jormungand's Prison on the Island of Stone."),
				melee("Cannon + melee — Waterbirth", "Waterbirth Island Dungeon",
					AttackType.SLASH,
					"Cannon-compatible route in Waterbirth Island Dungeon.")),
			"dagannoth", "dagannoths");

		register(profile("dark-beasts", "Dark beasts",
				"A short, straightforward melee task.",
				"Protect from Melee reduces supply use.",
				melee("Melee", "Mourner Tunnels / Iorwerth Dungeon", AttackType.SLASH,
					"Dark beasts have high Defence; slash accuracy from Oathplate can outperform Torva with slash weapons.",
					"oathplate")),
			"dark beasts", "dark beast");

		register(profile("drakes", "Drakes",
				"Drakes are draconic and have a 50% Water weakness; dragonbane and Water Magic are both surfaced.",
				"Boots of stone or a heat-protecting upgrade may be required in Karuulm.",
				dragonMelee("Dragonbane stab", "Karuulm Slayer Dungeon"),
				dragonRanged("Dragonbane Ranged", "Karuulm Slayer Dungeon"),
				dragonMagic("Water Magic / dragonbane", "Karuulm Slayer Dungeon",
					"Water spells gain the elemental bonus; Dragon hunter wand gains its draconic bonus.")),
			"drakes", "drake");

		register(profile("dust-devils", "Dust devils",
				"Ancient Magicks AoE is the high-XP method, with Venator second.",
				"A Slayer helmet or facemask is required.",
				ancients("Catacombs barrage", "Catacombs of Kourend",
					"Prioritizes magic damage and prayer over excess accuracy."),
				venator("Venator multi-target", "Catacombs / Smoke Dungeon",
					"Lower-effort multi-target fallback."),
				melee("Melee fallback", "Catacombs / Smoke Dungeon", AttackType.SLASH,
					"Budget single-target setup", "slayer helm", "facemask")),
			"dust devils", "dust devil");

		register(profile("elves", "Elves",
				"Straightforward melee profile for a low-efficiency task.",
				"Use protection prayers as needed.",
				melee("Melee", "Prifddinas / Lletya", AttackType.SLASH,
					"Ranks owned melee damage gear")),
			"elves", "elf");

		register(profile("fire-giants", "Fire giants",
				"Water spells exploit their elemental weakness; Venator is the low-effort Catacombs alternative.",
				"Protection is usually optional.",
				elementalMagic("Water Magic", "Catacombs / Waterfall Dungeon",
					"Uses the 100% water weakness for strong Magic experience."),
				venator("Catacombs Venator", "Catacombs of Kourend",
					"Multi-target ranged alternative."),
				melee("Melee fallback", "Catacombs / Waterfall Dungeon", AttackType.SLASH,
					"Budget fallback")),
			"fire giants", "fire giant");

		register(profile("fossil-wyverns", "Fossil Island wyverns",
				"Prefer an owned one-handed dragonbane weapon; otherwise use the strongest protected Ranged setup.",
				"An elemental, mind, dragonfire, or ancient wyvern shield is required.",
				GearStrategy.builder()
					.name("Protected dragonbane Ranged")
					.location("Fossil Island Wyvern Cave")
					.rationale("Uses an owned dragon hunter crossbow while enforcing wyvern protection.")
					.combatStyle(CombatStyle.RANGED)
					.requiredOffhand("elemental shield|mind shield|dragonfire shield|ancient wyvern shield")
					.requiredWeapon("dragon hunter crossbow")
					.preferredItem("dragon hunter crossbow")
					.build(),
				GearStrategy.builder()
					.name("Protected dragonbane Melee")
					.location("Fossil Island Wyvern Cave")
					.rationale("Uses an owned dragon hunter lance with tank-oriented melee gear and wyvern protection.")
					.combatStyle(CombatStyle.MELEE)
					.attackType(AttackType.BALANCED)
					.requiredOffhand("elemental shield|mind shield|dragonfire shield|ancient wyvern shield")
					.requiredWeapon("dragon hunter lance")
					.preferredItem("dragon hunter lance")
					.build(),
				GearStrategy.builder()
					.name("Protected Ranged fallback")
					.location("Fossil Island Wyvern Cave")
					.rationale("Ranks the strongest compatible owned Ranged setup when no ranged dragonbane weapon is owned.")
					.combatStyle(CombatStyle.RANGED)
					.requiredOffhand("elemental shield|mind shield|dragonfire shield|ancient wyvern shield")
					.preferredItem("hunter's sunlight crossbow")
					.build()),
			"fossil island wyverns", "fossil island wyvern");

		register(profile("frost-dragons", "Frost dragons",
				"Fire Magic exploits a 100% weakness; dragonbane remains a strong fallback.",
				"Use complete icy-dragonfire protection.",
				elementalMagic("Fire Magic", "Grimstone Dungeon",
					"Exploits the 100% fire weakness."),
				dragonRanged("Dragonbane Ranged", "Grimstone Dungeon"),
				melee("Crush / dragonbane melee", "Grimstone Dungeon", AttackType.CRUSH,
					"Frost dragons are weakest to crush, then stab", "dragon hunter lance")),
			"frost dragons", "frost dragon");

		register(profile("gargoyles", "Gargoyles",
				"Crush melee is scored with Gargoyle-specific Golembane effects instead of raw sheet stats alone.",
				"A Granite hammer doubles as the finishing hammer; otherwise keep a rock hammer or rock thrownhammer available.",
				GearStrategy.builder()
					.name("Golembane crush")
					.location("Slayer Tower")
					.rationale("Compares real Crush-capable weapons while applying Golembane accuracy/damage to Granite hammer.")
					.combatStyle(CombatStyle.MELEE)
					.attackType(AttackType.CRUSH)
					.weaponRule(WeaponRule.GOLEMBANE)
					.build()),
			"gargoyles", "gargoyle");

		register(profile("greater-demons", "Greater demons",
				"Demonbane weapons are prioritized, including for Tormented Demon alternatives.",
				"Protect from Melee against ordinary Greater demons.",
				demonMelee("Demonbane melee", "Catacombs / Chasm / Tormented Demons",
					"Emberlight and Arclight receive explicit priority."),
				ranged("Ranged fallback", "Task-dependent", "Alternative for boss variants")),
			"greater demons", "greater demon");

		register(profile("gryphons", "Gryphons",
				"Heavy melee equipment avoids knockback while preserving offensive strength.",
				"Aim for at least 30 kg worn weight; the superior/boss requires a tortugan shield.",
				melee("Heavy melee + cannon", "Great Conch", AttackType.STAB,
					"Balances melee damage with heavy armour", "tortugan shield", "dragonfire shield")),
			"gryphons", "gryphon");

		register(profile("hellhounds", "Hellhounds",
				"Venator is the multi-target option; ordinary Hellhounds are demonic but have no elemental weakness.",
				"Protect from Melee removes ordinary Hellhound damage.",
				venator("Catacombs Venator", "Catacombs of Kourend",
					"Multi-target, low-effort Slayer XP."),
				demonMelee("Demonbane melee / Cerberus", "Catacombs / Cerberus",
					"Demonbane weapons receive their target-specific bonus.")),
			"hellhounds", "hellhound");

		register(profile("kalphites", "Kalphites",
				"Cannon-assisted melee is the fast normal-task method.",
				"Poison protection is suggested for soldiers and guardians; the prep plan also covers boosts, Prayer, food, and travel.",
				GearStrategy.builder()
					.name("Cannon + Keris melee")
					.location("task-only Kalphite Cave")
					.rationale("Keris/partisan effects receive priority against Kalphites.")
					.combatStyle(CombatStyle.MELEE)
					.attackType(AttackType.CRUSH)
					.weaponRule(WeaponRule.KALPHITE)
					.preferredItem("keris partisan of breaching")
					.preferredItem("keris partisan")
					.preferredItem("keris")
					.build()),
			"kalphites", "kalphite");

		register(profile("kurask", "Kurask",
				"Only valid Kurask weapons are considered.",
				"Leaf-bladed weapons, broad ammunition, or Slayer Dart are required.",
				GearStrategy.builder()
					.name("Leaf-bladed melee")
					.location("Fremennik Slayer Dungeon / Iorwerth Dungeon")
					.rationale("Rejects unusable ordinary weapons and ranks valid leaf-bladed choices.")
					.combatStyle(CombatStyle.MELEE)
					.attackType(AttackType.SLASH)
					.weaponRule(WeaponRule.LEAF_BLADED)
					.preferredItem("leaf-bladed battleaxe")
					.preferredItem("leaf-bladed sword")
					.preferredItem("leaf-bladed spear")
					.build()),
			"kurask", "kurasks");

		register(profile("lizardmen", "Lizardmen",
				"Ranged with a cannon is the practical fast-task setup.",
				"Shayzien armour may be required against shaman poison attacks.",
				ranged("Cannon + Ranged", "Lizardman Canyon",
					"Ranks ranged damage while favoring Shayzien protection for shamans", "shayzien")),
			"lizardmen", "lizardman");

		register(profile("metal-dragons", "Metal dragons",
				"Dragonbane weapons dominate; stab melee is the default high-level route.",
				"Use adequate dragonfire protection.",
				dragonMelee("Dragon hunter lance", "Dragon-dependent"),
				dragonRanged("Dragonbane Ranged", "Dragon-dependent"),
				magic("Elemental Magic fallback", "Dragon-dependent",
					"Fallback for accounts without dragonbane weapons")),
			"metal dragons", "metal dragon", "bronze dragons", "bronze dragon",
			"iron dragons", "iron dragon", "steel dragons", "steel dragon",
			"mithril dragons", "mithril dragon", "adamant dragons", "adamant dragon",
			"rune dragons", "rune dragon");

		register(profile("mutated-zygomites", "Mutated zygomites",
				"Short melee task.",
				"Keep fungicide spray available for finishing blows.",
				melee("Melee", "Zanaris", AttackType.SLASH,
					"Ranks ordinary melee damage", "fungicide spray")),
			"mutated zygomites", "mutated zygomite", "zygomites", "zygomite");

		register(profile("nechryael", "Nechryael",
				"Catacombs barraging is prioritized well ahead of single-target melee.",
				"Protect from Melee while stacking Greater Nechryaels.",
				ancients("Catacombs barrage", "Catacombs of Kourend",
					"High-XP multi-target method."),
				demonMelee("Demonbane melee fallback", "Slayer Tower / Catacombs",
					"Demonbane single-target fallback when not using Ancient AoE.")),
			"nechryael", "nechryaels");

		register(profile("red-dragons", "Red dragons",
				"Dragonbane combat is prioritized.",
				"Use adequate dragonfire protection.",
				dragonMelee("Dragonbane stab", "Forthos / Brimhaven"),
				dragonRanged("Dragonbane Ranged", "Forthos / Brimhaven")),
			"red dragons", "red dragon");

		register(profile("skeletal-wyverns", "Skeletal wyverns",
				"One-handed dragonbane Ranged with mandatory icy-breath protection.",
				"An elemental, mind, dragonfire, or ancient wyvern shield is required.",
				GearStrategy.builder()
					.name("Protected dragonbane Ranged")
					.location("Asgarnian Ice Dungeon")
					.rationale("Ranks one-handed ranged weapons while enforcing a valid shield.")
					.combatStyle(CombatStyle.RANGED)
					.requiredOffhand("elemental shield|mind shield|dragonfire shield|ancient wyvern shield")
					.preferredItem("dragon hunter crossbow")
					.build()),
			"skeletal wyverns", "skeletal wyvern");

		register(profile("smoke-devils", "Smoke devils",
				"Barraging with a cannon lure is the highest-XP Slayer method, with melee retained as a safe fallback.",
				"A Slayer helmet or facemask is required.",
				ancients("Barrage + cannon lure", "Smoke Devil Dungeon",
					"Use the cannon to pull/group smoke devils, then Burst/Barrage the stack for high Slayer XP."),
				melee("Melee fallback", "Smoke Devil Dungeon", AttackType.SLASH,
					"Budget fallback when Ancient Magicks is not the desired method", "slayer helm", "facemask")),
			"smoke devils", "smoke devil");

		register(profile("spiritual-creatures", "Spiritual creatures",
				"Straightforward offensive melee setup.",
				"God protection and environmental supplies depend on the God Wars area.",
				melee("Melee", "God Wars Dungeon", AttackType.SLASH,
					"Ranks melee damage for low-hitpoint spiritual creatures")),
			"spiritual creatures", "spiritual creature");

		register(profile("suqahs", "Suqahs",
				"Cannon-assisted melee is the fast-task setup.",
				"Use Protect from Magic and solid melee defence. A separate no-prayer method values magic defence.",
				melee("Cannon + melee", "Lunar Isle", AttackType.SLASH,
					"Protect from Magic makes offensive melee gear the priority while the cannon supplies extra hits."),
				meleeMagicDef("No-prayer defensive melee", "Lunar Isle", AttackType.SLASH,
					"Without Protect from Magic, trades some damage for magic defence.")),
			"suqahs", "suqah");

		register(profile("trolls", "Trolls",
				"Cannon-assisted melee is location-specific; mountain and ice-troll weapon styles are kept separate.",
				"Use protection prayers against high-damage ice trolls.",
				GearStrategy.builder().name("Mountain trolls - melee").location("South of Mount Quidamortem")
					.rationale("Ranks the Wiki-listed Soulreaper and strong general melee options for mountain trolls.")
					.combatStyle(CombatStyle.MELEE).attackType(AttackType.BALANCED).build(),
				melee("Ice trolls - stab", "Fremennik Isles / Trollweiss", AttackType.STAB,
					"Ranks the Wiki-listed stab route separately from mountain trolls."),
				melee("Ice trolls - crush", "Fremennik Isles / Trollweiss", AttackType.CRUSH,
					"Ranks the Wiki-listed crush route, including Inquisitor's mace and Sarachnis cudgel.")),
			"trolls", "troll");

		register(profile("tzhaar", "TzHaar",
				"Blood-spell barraging in Mor Ul Rek is prioritized for normal TzHaar.",
				"Fight Caves and Inferno assignments require encounter-specific supplies.",
				ancients("Ancient AoE", "Inner Mor Ul Rek",
					"High-XP multi-target method using the strongest Ancient AoE spell your Magic level supports."),
				ranged("Fight Caves Ranged", "Fight Caves", "Fallback for Jad-oriented assignments")),
			"tzhaar", "tzhaar creatures");

		register(profile("jad", "TzTok-Jad",
				"Ranged is the standard Fight Caves setup.",
				"Encounter supplies and prayer switching matter more than small gear-score differences.",
				GearStrategy.builder()
					.name("Fight Caves Blowpipe")
					.location("Fight Caves")
					.rationale("Uses the Toxic blowpipe as the primary owned Fight Caves weapon for fast waves and Jad.")
					.combatStyle(CombatStyle.RANGED)
					.requiredWeapon("toxic blowpipe")
					.preferredItem("toxic blowpipe")
					.build(),
				ranged("Fight Caves Ranged fallback", "Fight Caves",
					"Ranks the best compatible ranged setup when no Toxic blowpipe is owned.",
					"twisted bow", "bow of faerdhinen", "venator bow", "crystal bow")),
			"tztok-jad", "jad");

		register(profile("zuk", "TzKal-Zuk",
				"Ranged is the primary Inferno setup.",
				"Treat the result as a shortlist; Inferno loadouts are highly account-specific.",
				ranged("Inferno Ranged", "The Inferno", "Ranks ranged damage and prayer gear")),
			"tzkal-zuk", "zuk");

		register(profile("vampyres", "Vampyres",
				"Vampyre-specific weapon effects are scored dynamically, including modern Sunspear, Hallowed and Blisterwood options.",
				"Higher-tier Vampyres require an appropriate silver/blisterwood/hallowed weapon.",
				GearStrategy.builder()
					.name("Vampyre melee")
					.location("Darkmeyer / Meiyerditch / Vampyrium")
					.rationale("Compares valid owned Vampyre weapons with their target-specific accuracy and damage effects.")
					.combatStyle(CombatStyle.MELEE)
					.attackType(AttackType.BALANCED)
					.weaponRule(WeaponRule.VAMPYRE)
					.build()),
			"vampyres", "vampyre", "vyrewatch");

		register(profile("venators", "Venators",
				"Mortimer-exclusive Vampyrium task. Blisterwood stakes provide the dedicated Ranged method; Sunspear and Hallowed Flail are compared for melee, with Efaritay's aid prioritized over ordinary combat rings.",
				"Requires 74 Slayer and completion of The Blood Moon Rises. React to the Venator's screech attack; a Sunspear finishing special can sustain the trip.",
				GearStrategy.builder()
					.name("Blisterwood stakes Ranged")
					.location("Vampyrium")
					.rationale("Uses the purpose-built fast Ranged stake method without confusing this monster with the Venator bow.")
					.combatStyle(CombatStyle.RANGED)
					.requiredWeapon("blisterwood stakes|blisterwood stake")
					.preferredItem("blisterwood stakes")
					.weaponRule(WeaponRule.VAMPYRE)
					.build(),
				GearStrategy.builder()
					.name("Sunspear Vampyre melee")
					.location("Vampyrium")
					.rationale("Prioritizes Sunspear for its Venator finishing special, then compares other valid Vampyre melee weapons.")
					.combatStyle(CombatStyle.MELEE)
					.attackType(AttackType.BALANCED)
					.weaponRule(WeaponRule.VAMPYRE)
					.preferredItem("sunspear")
					.preferredItem("hallowed flail")
					.preferredItem("efaritay's aid")
					.build()),
			"venators", "venator");

		register(profile("warped-creatures", "Warped creatures",
				"Warped terrorbirds and tortoises have distinct Wiki methods; cannon support remains useful where available.",
				"They use melee and ranged in multicombat; use protection and sustain.",
				melee("Warped tortoises - Crush", "Poison Waste Dungeon", AttackType.CRUSH,
					"Targets the tortoise's Crush weakness for the cannon-supported method"),
				melee("Warped terrorbirds - melee", "Poison Waste Dungeon", AttackType.SLASH,
					"Separate terrorbird melee route"),
				ranged("Ranged safe method", "Poison Waste Dungeon",
					"Wiki-listed Ranged alternative for safer positioning")),
			"warped creatures", "warped creature");

		register(profile("waterfiends", "Waterfiends",
				"Crush melee and Earth Magic both target current weaknesses; Earth spells receive a 100% elemental bonus.",
				"They attack with two styles; defensive balance may help.",
				elementalMagic("Earth Magic", "Ancient Cavern",
					"Earth spells exploit the current 100% elemental weakness."),
				meleeMagicDef("Crush melee", "Ancient Cavern", AttackType.CRUSH,
					"Crush accuracy receives priority")),
			"waterfiends", "waterfiend");

		register(profile("wyrms", "Wyrms",
				"Wyrms support top general melee weapons as well as dragonbane, Ranged, and their 50% Earth weakness.",
				"Boots of stone or a heat-protecting upgrade may be required in Karuulm.",
				GearStrategy.builder().name("Melee / dragonbane").location("Karuulm Slayer Dungeon")
					.rationale("Compares Soulreaper, Scythe and Noxious halberd with Dragon hunter lance and stab alternatives.")
					.combatStyle(CombatStyle.MELEE).attackType(AttackType.BALANCED)
					.weaponRule(WeaponRule.DRAGONBANE).targetTrait(TargetTrait.DRAGON)
					.targetTrait(TargetTrait.SCYTHE_THREE_HIT).build(),
				dragonRanged("Dragonbane Ranged", "Karuulm Slayer Dungeon"),
				dragonMagic("Earth Magic / dragonbane", "Karuulm Slayer Dungeon",
					"Earth spells gain the elemental bonus; Dragon hunter wand gains its draconic bonus.")),
			"wyrms", "wyrm");


		register(profile("banshees", "Banshees",
				"Straightforward low-level melee task with mandatory hearing protection.",
				"Wear earmuffs or a Slayer helmet.",
				melee("Protected melee", "Slayer Tower", AttackType.SLASH,
					"Ranks melee damage while the safety engine enforces hearing protection.")),
			"banshees", "banshee");

		register(profile("cockatrice", "Cockatrice",
				"One-handed melee is paired with mandatory gaze protection.",
				"A mirror shield or V's shield is required.",
				GearStrategy.builder().name("Protected melee").location("Fremennik Slayer Dungeon")
					.rationale("Ranks one-handed melee while enforcing a valid mirror shield.")
					.combatStyle(CombatStyle.MELEE).attackType(AttackType.SLASH)
					.requiredOffhand("mirror shield|v's shield").build()),
			"cockatrice", "cockatrices");

		register(profile("fever-spiders", "Fever spiders",
				"Fast melee task once the required gloves are equipped.",
				"Slayer gloves are required.",
				melee("Protected melee", "Braindeath Island", AttackType.SLASH,
					"Ranks melee damage while enforcing Slayer gloves.")),
			"fever spiders", "fever spider");

		register(profile("harpie-bug-swarms", "Harpie bug swarms",
				"Simple melee task with a mandatory lit bug lantern.",
				"Equip a lit bug lantern before fighting them.",
				melee("Lantern melee", "Karamja", AttackType.SLASH,
					"Ranks melee damage around the required lantern.")),
			"harpie bug swarms", "harpie bug swarm");

		register(profile("hydras", "Hydras",
				"Dragonbane equipment is prioritized for Karuulm hydras.",
				"Use Karuulm heat-protection boots unless your account has an applicable exemption.",
				dragonRanged("Dragonbane Ranged", "Karuulm Slayer Dungeon"),
				dragonMelee("Dragonbane melee", "Karuulm Slayer Dungeon")),
			"hydras", "hydra");

		register(profile("jellies", "Jellies",
				"Catacombs jellies are excellent Ancient Magicks multi-target tasks when available.",
				"Protect from Melee mitigates all damage while stacking. A separate no-prayer melee method values magic defence.",
				ancients("Catacombs burst / barrage", "Catacombs of Kourend",
					"Uses the highest Ancient AoE spell your Magic level supports."),
				melee("Protected melee fallback", "Fremennik Slayer Dungeon / Catacombs", AttackType.SLASH,
					"With Protect from Melee active, ranks offensive gear for the single-target fallback."),
				meleeMagicDef("No-prayer defensive melee", "Fremennik Slayer Dungeon", AttackType.SLASH,
					"Without Protect from Melee, trades some damage for magic defence.")),
			"jellies", "jelly");

		register(profile("killerwatts", "Killerwatts",
				"Low-level task where insulated footwear greatly reduces their special damage.",
				"Wear insulated boots.",
				melee("Insulated melee", "Killerwatt plane", AttackType.SLASH,
					"Ranks melee damage while enforcing insulated boots.")),
			"killerwatts", "killerwatt");

		register(profile("lizards", "Lizards",
				"Basic desert melee task; the finishing item matters more than gear complexity.",
				"Carry ice coolers to finish desert lizards.",
				melee("Desert melee", "Kharidian Desert", AttackType.SLASH,
					"Ranks straightforward melee damage.")),
			"lizards", "lizard", "desert lizards", "desert lizard");

		register(profile("mogres", "Mogres",
				"Fishing explosives are required to lure Mogres out before combat.",
				"Carry fishing explosives.",
				melee("Mogre melee", "Mudskipper Point", AttackType.SLASH,
					"Ranks straightforward melee damage.")),
			"mogres", "mogre");

		register(profile("molanisks", "Molanisks",
				"A Slayer bell is required to dislodge Molanisks before attacking.",
				"Carry a Slayer bell.",
				melee("Molanisk melee", "Dorgesh-Kaan South Dungeon", AttackType.CRUSH,
					"Ranks melee damage after the Slayer bell lure.")),
			"molanisks", "molanisk");

		register(profile("rockslugs", "Rockslugs",
				"Basic melee task with a mandatory finishing item.",
				"Carry a bag of salt to finish rockslugs.",
				melee("Rockslug melee", "Fremennik Slayer Dungeon", AttackType.SLASH,
					"Ranks straightforward melee damage.")),
			"rockslugs", "rockslug");

		register(profile("turoth", "Turoth",
				"Only Turoth-compatible weapons are considered.",
				"Use leaf-bladed weapons, broad ammunition, or Slayer Dart.",
				GearStrategy.builder().name("Leaf-bladed melee").location("Fremennik Slayer Dungeon")
					.rationale("Rejects ordinary melee weapons that cannot damage Turoth.")
					.combatStyle(CombatStyle.MELEE).attackType(AttackType.SLASH)
					.weaponRule(WeaponRule.LEAF_BLADED).build()),
			"turoth", "turoths");

		register(profile("wall-beasts", "Wall beasts",
				"Short melee assignment with mandatory head protection.",
				"Wear a spiny helmet or Slayer helmet.",
				melee("Protected melee", "Lumbridge Swamp Caves", AttackType.SLASH,
					"Ranks melee damage while enforcing head protection.")),
			"wall beasts", "wall beast");

		registerBossAliases();
	}

	private TaskProfiles()
	{
	}

	static Optional<SlayerTaskProfile> find(String taskName)
	{
		return find(taskName, null);
	}

	static Optional<SlayerTaskProfile> find(String taskName, String assignedLocation)
	{
		return find(taskName, assignedLocation, false);
	}

	static Optional<SlayerTaskProfile> find(
		String taskName,
		String assignedLocation,
		boolean turaelAyaSpeedMode)
	{
		if (taskName == null)
		{
			return Optional.empty();
		}
		SlayerTaskProfile exact = PROFILES.get(taskName);
		SlayerTaskProfile base = exact != null ? exact : generic(taskName);
		base = withCannonOption(base, taskName, assignedLocation);
		if (turaelAyaSpeedMode)
		{
			base = TuraelSpeedProfiles.apply(base, taskName);
		}
		return Optional.of(TaskCombatCatalog.enrich(base, taskName, assignedLocation));
	}

	static int profileCount()
	{
		return PROFILES.distinctValueCount();
	}

	static Map<String, SlayerTaskProfile> catalogSnapshot()
	{
		return PROFILES.snapshot();
	}

	static Map<String, java.util.List<SlayerTaskProfile>> ignoredAliasCollisions()
	{
		return PROFILES.ignoredCollisionsSnapshot();
	}

	private static SlayerTaskProfile withCannonOption(SlayerTaskProfile profile, String taskName, String assignedLocation)
	{
		Optional<CannonTaskCatalog.CannonRoute> knownRoute = CannonTaskCatalog.find(taskName);
		if (!knownRoute.isPresent()) return profile;

		// RuneLite stores a location for Konar/location-locked assignments. Cannon
		// support is route-specific: a monster can be cannonable somewhere while the
		// assigned area itself is prohibited or simply not one of its verified routes.
		Optional<CannonTaskCatalog.CannonRoute> route = CannonTaskCatalog.find(taskName, assignedLocation);
		if (!route.isPresent())
		{
			if (assignedLocation != null && !assignedLocation.trim().isEmpty())
			{
				return withoutCannonAtLocation(profile, assignedLocation);
			}
			return profile;
		}

		boolean alreadyHasCannon = profile.getStrategies().stream()
			.anyMatch(strategy -> NameMatcher.normalize(strategy.getName()).contains("cannon"));
		String cannonLocation = assignedLocation == null || assignedLocation.trim().isEmpty()
			? route.get().getLocation()
			: assignedLocation.trim();
		String summary = profile.getSummary();
		if (!NameMatcher.normalize(summary).contains("cannon"))
		{
			summary = summary + " Cannon route available at " + cannonLocation + ".";
		}

		SlayerTaskProfile.Builder builder = SlayerTaskProfile.builder()
			.key(profile.getKey())
			.displayName(profile.getDisplayName())
			.summary(summary)
			.protectionAdvice(profile.getProtectionAdvice());
		boolean assignedCannonAdded = false;
		for (GearStrategy strategy : profile.getStrategies())
		{
			// When RuneLite provides a location-locked assignment, keep curated
			// cannon methods but point them at the actual assigned cannonable area.
			// This avoids a Dagannoth task in Jormungand's Prison still telling the
			// player to go to the Lighthouse, for example.
			if (alreadyHasCannon && SmartSupplyAdvisor.isCannon(strategy)
				&& assignedLocation != null && !assignedLocation.trim().isEmpty())
			{
				if (!assignedCannonAdded)
				{
					builder.strategy(copyStrategyAtLocation(strategy, cannonLocation));
					assignedCannonAdded = true;
				}
			}
			else
			{
				builder.strategy(strategy);
			}
		}

		if (!alreadyHasCannon)
		{
			CannonTaskCatalog.CannonRoute cannon = route.get();
			GearStrategy.Builder cannonStrategy = GearStrategy.builder()
				.name("Cannon + " + cannon.getCombatStyle().name().toLowerCase(Locale.ENGLISH))
				.location(cannonLocation)
				.rationale(cannon.getRationale())
				.combatStyle(cannon.getCombatStyle())
				.attackType(cannon.getAttackType());
			if (cannon.getCombatStyle() == CombatStyle.MAGIC)
			{
				cannonStrategy.preferredItem("kodai wand").preferredItem("nightmare staff")
					.preferredItem("ancient sceptre").preferredItem("ancient staff");
			}
			builder.strategy(cannonStrategy.build());
		}
		return builder.build();
	}

	private static GearStrategy copyStrategyAtLocation(GearStrategy source, String location)
	{
		return source.toBuilder().location(location).build();
	}

	private static SlayerTaskProfile withoutCannonAtLocation(SlayerTaskProfile profile, String assignedLocation)
	{
		String location = assignedLocation == null || assignedLocation.trim().isEmpty() ? "assigned area" : assignedLocation.trim();
		SlayerTaskProfile.Builder builder = SlayerTaskProfile.builder()
			.key(profile.getKey())
			.displayName(profile.getDisplayName())
			.summary("Location-locked at " + location + ": dwarf multicannon use is unavailable here. Best non-cannon owned method shown.")
			.protectionAdvice(profile.getProtectionAdvice());

		boolean added = false;
		for (GearStrategy strategy : profile.getStrategies())
		{
			if (!SmartSupplyAdvisor.isCannon(strategy))
			{
				builder.strategy(strategy);
				added = true;
			}
		}

		// Some curated profiles were originally cannon-only (for example the
		// Bloodveld fast method). Preserve the underlying combat plan, but remove the
		// cannon label so supplies/readiness no longer demand cannon gear.
		if (!added && !profile.getStrategies().isEmpty())
		{
			GearStrategy source = profile.getStrategies().get(0);
			GearStrategy fallback = source.toBuilder()
				.name(source.getName().replace("Cannon + ", "").replace(" + cannon", "").replace("cannon + ", ""))
				.location(location)
				.rationale("Cannon is unavailable at this assigned location; " + source.getRationale())
				.build();
			builder.strategy(fallback);
		}
		return builder.build();
	}

	private static void register(
		SlayerTaskProfile profile, String... taskNames)
	{
		// First registration wins: a later broad boss alias must never overwrite
		// a task-specific curated profile.
		PROFILES.register(profile, AliasCatalog.CollisionPolicy.KEEP_FIRST, taskNames);
	}

	private static void registerBoss(
		SlayerTaskProfile profile, String... taskNames)
	{
		// Boss names are separate aliases from their broader assignments. Keeping
		// the first registration makes an accidental duplicate visible through the
		// catalog collision guard instead of silently overwriting another profile.
		PROFILES.register(profile, AliasCatalog.CollisionPolicy.KEEP_FIRST, taskNames);
	}

	private static SlayerTaskProfile profile(
		String key,
		String displayName,
		String summary,
		String protection,
		GearStrategy... strategies)
	{
		SlayerTaskProfile.Builder builder = SlayerTaskProfile.builder()
			.key(key)
			.displayName(displayName)
			.summary(summary)
			.protectionAdvice(protection);
		for (GearStrategy strategy : strategies)
		{
			builder.strategy(strategy);
		}
		return builder.build();
	}

	private static GearStrategy melee(
		String name,
		String location,
		AttackType attackType,
		String rationale,
		String... preferredItems)
	{
		GearStrategy.Builder builder = GearStrategy.builder()
			.name(name)
			.location(location)
			.rationale(rationale)
			.combatStyle(CombatStyle.MELEE)
			.attackType(attackType);
		for (String item : preferredItems)
		{
			builder.preferredItem(item);
		}
		return builder.build();
	}

	private static GearStrategy meleeMagicDef(
		String name,
		String location,
		AttackType attackType,
		String rationale,
		String... preferredItems)
	{
		GearStrategy.Builder builder = GearStrategy.builder()
			.name(name)
			.location(location)
			.rationale(rationale)
			.combatStyle(CombatStyle.MELEE)
			.attackType(attackType)
			.magicDefenceWeight(0.28);
		for (String item : preferredItems)
		{
			builder.preferredItem(item);
		}
		return builder.build();
	}

	private static GearStrategy ranged(
		String name, String location, String rationale, String... preferredItems)
	{
		GearStrategy.Builder builder = GearStrategy.builder()
			.name(name)
			.location(location)
			.rationale(rationale)
			.combatStyle(CombatStyle.RANGED);
		for (String item : preferredItems)
		{
			builder.preferredItem(item);
		}
		return builder.build();
	}

	private static GearStrategy magic(String name, String location, String rationale)
	{
		return GearStrategy.builder()
			.name(name)
			.location(location)
			.rationale(rationale)
			.combatStyle(CombatStyle.MAGIC)
			.build();
	}

	private static GearStrategy elementalMagic(String name, String location, String rationale)
	{
		return GearStrategy.builder()
			.name(name)
			.location(location)
			.rationale(rationale)
			.combatStyle(CombatStyle.MAGIC)
			.preferredItem("tome")
			.preferredItem("staff")
			.build();
	}

	private static GearStrategy ancients(String name, String location, String rationale)
	{
		return GearStrategy.builder()
			.name(name)
			.location(location)
			.rationale(rationale)
			.combatStyle(CombatStyle.MAGIC)
			.minimumMagic(62)
			.ancientAoe(true)
			.preferredItem("kodai wand")
			.preferredItem("nightmare staff")
			.preferredItem("ancient sceptre")
			.preferredItem("ancient staff")
			.prayerWeight(1.8)
			.build();
	}

	private static GearStrategy venator(String name, String location, String rationale)
	{
		return GearStrategy.builder()
			.name(name)
			.location(location)
			.rationale(rationale)
			.combatStyle(CombatStyle.RANGED)
			.minimumRanged(80)
			.requiredWeapon("venator bow")
			.preferredItem("venator bow")
			.build();
	}

	private static GearStrategy demonMelee(String name, String location, String rationale)
	{
		return GearStrategy.builder()
			.name(name)
			.location(location)
			.rationale(rationale)
			.combatStyle(CombatStyle.MELEE)
			.attackType(AttackType.SLASH)
			.weaponRule(WeaponRule.DEMONBANE)
			.build();
	}

	private static GearStrategy dragonMelee(String name, String location)
	{
		return GearStrategy.builder()
			.name(name)
			.location(location)
			.rationale("Prioritizes owned dragonbane weapons, then stab damage.")
			.combatStyle(CombatStyle.MELEE)
			.attackType(AttackType.STAB)
			.weaponRule(WeaponRule.DRAGONBANE)
			.build();
	}

	private static GearStrategy dragonRanged(String name, String location)
	{
		return GearStrategy.builder()
			.name(name)
			.location(location)
			.rationale("Prioritizes owned ranged dragonbane weapons.")
			.combatStyle(CombatStyle.RANGED)
			.weaponRule(WeaponRule.DRAGONBANE)
			.build();
	}

	private static GearStrategy dragonMagic(String name, String location, String rationale)
	{
		return GearStrategy.builder()
			.name(name)
			.location(location)
			.rationale(rationale)
			.combatStyle(CombatStyle.MAGIC)
			.weaponRule(WeaponRule.DRAGONBANE)
			.build();
	}

	private static void registerBossAliases()
	{
		registerBoss(profile("amoxliatl-boss", "Amoxliatl",
				"Crush-focused melee using Blood moon weapons and armour where owned.",
				"Avoid unstable ice and preserve inventory room for encounter supplies.",
				GearStrategy.builder().name("Amoxliatl - Crush").location("Ruins of Tapoyauik")
					.rationale("Wiki-ranked Crush setup led by Scythe, Dual macuahuitl and Inquisitor options.")
					.combatStyle(CombatStyle.MELEE).attackType(AttackType.CRUSH)
					.preferredItem("dual macuahuitl").preferredItem("blood moon").build()),
			"amoxliatl");

		registerBoss(profile("branda-boss", "Branda the Fire Queen",
				"Water Magic is the dedicated elemental counter to the Fire Queen.",
				"Bring the Royal Titans encounter supplies and movement tools.",
				GearStrategy.builder().name("Branda - Water Magic").location("Royal Titans arena")
					.rationale("Ranks the strongest owned Water spell setup for Branda.")
					.combatStyle(CombatStyle.MAGIC).elementalWeakness(ElementalWeakness.WATER, 100).build()),
			"branda the fire queen");

		registerBoss(profile("brutus-boss", "Brutus",
				"Earth Magic exploits Brutus' elemental weakness.",
				"Keep the Cowbell amulet and encounter food available.",
				GearStrategy.builder().name("Brutus - Earth Magic").location("Brutus arena")
					.rationale("Ranks the strongest owned Earth spell setup from the current strategy guide.")
					.combatStyle(CombatStyle.MAGIC).elementalWeakness(ElementalWeakness.EARTH, 100)
					.preferredItem("cowbell amulet").build()),
			"brutus");

		registerBoss(profile("bryophyta-boss", "Bryophyta",
				"Fast melee clears Bryophyta and her growthlings reliably.",
				"An axe is required to finish the growthlings.",
				melee("Bryophyta - Slash", "Bryophyta's lair", AttackType.SLASH,
					"Ranks fast Slash melee and owned strength gear.")),
			"bryophyta");

		registerBoss(profile("demonic-gorillas-boss", "Demonic Gorillas",
				"Their protection prayers require a melee plus Ranged combat switch.",
				"Bring both styles; the panel keeps the selected method as the primary set.",
				demonMelee("Demonic gorillas - Demonbane melee", "Crash Site Cavern",
					"Ranks Emberlight and other real Demonbane effects."),
				GearStrategy.builder().name("Demonic gorillas - Ranged switch").location("Crash Site Cavern")
					.rationale("Ranks the best owned Ranged switch for protection-prayer changes.")
					.combatStyle(CombatStyle.RANGED).targetTrait(TargetTrait.DEMON)
					.preferredItem("scorching bow").build()),
			"demonic gorillas", "demonic gorilla");

		registerBoss(profile("eldric-boss", "Eldric the Ice King",
				"Fire Magic exploits the Ice King's elemental weakness.",
				"Bring the Royal Titans encounter supplies and movement tools.",
				GearStrategy.builder().name("Eldric - Fire Magic").location("Royal Titans arena")
					.rationale("Ranks the strongest owned Fire spell setup for Eldric.")
					.combatStyle(CombatStyle.MAGIC).elementalWeakness(ElementalWeakness.FIRE, 100).build()),
			"eldric the ice king");

		registerBoss(profile("obor-boss", "Obor",
				"Strong melee is the straightforward Hill Giant boss method.",
				"Protect from Melee and bring enough food for his knockback damage.",
				melee("Obor - Melee", "Edgeville Dungeon", AttackType.SLASH,
					"Ranks the strongest owned fast melee setup.")),
			"obor");

		registerBoss(profile("scurrius-boss", "Scurrius",
				"Rat-bone weapons receive their real +10 max-hit value against Scurrius.",
				"Use protection prayer and handle falling debris during the fight.",
				GearStrategy.builder().name("Scurrius - Ratbane melee").location("Varrock Sewers")
					.rationale("Prioritizes the Bone mace and models its Ratbane max-hit effect.")
					.combatStyle(CombatStyle.MELEE).attackType(AttackType.CRUSH)
					.weaponRule(WeaponRule.RATBANE).targetTrait(TargetTrait.RAT).build()),
			"scurrius");

		registerBoss(profile("shellbane-gryphon-boss", "Shellbane Gryphon",
				"The Wiki setup balances melee DPS with the encounter's 40 kg threshold.",
				"A Tortugan shield is mandatory in the cape slot; heavy equipment prevents the devastating knockback.",
				GearStrategy.builder().name("Shellbane Gryphon - Heavy melee").location("The Great Conch")
					.rationale("Ranks the Wiki melee order while enforcing the cape-slot Tortugan shield and a 40 kg equipped setup.")
					.combatStyle(CombatStyle.MELEE).attackType(AttackType.BALANCED)
					.requiredCape("tortugan shield").minimumEquippedWeightKg(40.0)
					.rankedWeapon("scythe of vitur")
					.rankedWeapon("soulreaper axe")
					.rankedWeapon("ghrazi rapier")
					.rankedWeapon("noxious halberd")
					.rankedWeapon("osmumten's fang")
					.rankedWeapon("blade of saeldor")
					.rankedWeapon("abyssal tentacle")
					.rankedWeapon("zamorakian hasta")
					.rankedWeapon("abyssal whip")
					.rankedWeapon("abyssal dagger")
					.rankedWeapon("zombie axe")
					.rankedWeapon("belle's folly")
					.rankedWeapon("arkan blade")
					.rankedWeapon("colossal blade")
					.rankedWeapon("dragon scimitar")
					.preferredItem("amulet of rancour")
					.preferredItem("burning claws").build()),
			"shellbane gryphon", "the shellbane gryphon");

		registerBoss(profile("tormented-demons-boss", "Tormented Demons",
				"Demonbane weapons and combat-style switches are required around their protection prayer.",
				"Use Darklight once to remove the fire shield before the main Demonbane rotation.",
				demonMelee("Tormented Demons - Demonbane melee", "Ancient Guthixian Temple",
					"Ranks Emberlight/Arclight for the melee portion."),
				GearStrategy.builder().name("Tormented Demons - Scorching bow").location("Ancient Guthixian Temple")
					.rationale("Uses the purpose-built ranged Demonbane switch when owned.")
					.combatStyle(CombatStyle.RANGED).targetTrait(TargetTrait.DEMON)
					.requiredWeapon("scorching bow").preferredItem("scorching bow").build()),
			"tormented demons", "tormented demon");

		registerBoss(profile("araxxor-boss", "Araxxor",
				"Crush is Araxxor's primary weakness; Noxious halberd is kept as a separate encounter switch.",
				"Use a Noxious halberd or another safe answer for hatched araxytes and mirrorbacks.",
				GearStrategy.builder().name("Araxxor - Crush melee").location("Araxxor's lair")
					.rationale("Ranks main-hand Crush DPS and models all three Scythe hits on Araxxor.")
					.combatStyle(CombatStyle.MELEE).attackType(AttackType.CRUSH)
					.targetTrait(TargetTrait.ARAXXOR).targetTrait(TargetTrait.SCYTHE_THREE_HIT)
					.preferredItem("amulet of rancour").preferredItem("amulet of torture")
					.preferredItem("amulet of blood fury").build(),
				GearStrategy.builder().name("Araxxor - Noxious halberd switch").location("Araxxor's lair")
					.rationale("Dedicated Araxyte and mirrorback switch from the strategy guide.")
					.combatStyle(CombatStyle.MELEE).attackType(AttackType.SLASH)
					.requiredWeapon("noxious halberd").targetTrait(TargetTrait.ARAXXOR).build()),
			"araxxor");

		registerBoss(profile("cerberus-boss", "Cerberus",
				"Crush is the normal defence weakness, while valid Demonbane weapons remain competitive.",
				"Verify spectral-shield and ghost-cycle supplies for the chosen method.",
				GearStrategy.builder().name("Cerberus — Crush / Demonbane").location("Cerberus' Lair")
					.rationale("Ranks Crush DPS while retaining real Demonbane passives.")
					.combatStyle(CombatStyle.MELEE).attackType(AttackType.CRUSH)
					.weaponRule(WeaponRule.DEMONBANE).targetTrait(TargetTrait.DEMON)
					.targetTrait(TargetTrait.SCYTHE_THREE_HIT).build()),
			"cerberus");

		registerBoss(profile("duke-sucellus-boss", "Duke Sucellus",
				"Slash weapons are preferred; Duke's resistance reduces, but does not remove, Demonbane value.",
				"Special-attack choices are utility switches rather than automatic main weapons.",
				GearStrategy.builder().name("Duke Sucellus — Slash").location("Duke Sucellus' chamber")
					.rationale("Ranks Slash DPS with Duke's reduced Demonbane multiplier and the Wiki's Oathplate-over-Torva armour order.")
					.combatStyle(CombatStyle.MELEE).attackType(AttackType.SLASH)
					.weaponRule(WeaponRule.DEMONBANE).targetTrait(TargetTrait.DEMON)
					.targetTrait(TargetTrait.SCYTHE_THREE_HIT)
					.preferredItem("oathplate").build()),
			"duke sucellus");

		registerBoss(profile("sarachnis-boss", "Sarachnis",
				"Fast Crush weapons exploit Sarachnis' primary defence weakness.",
				"Magic defence and web-handling choices remain encounter-dependent.",
				GearStrategy.builder().name("Sarachnis - Crush").location("Forthos Dungeon")
					.rationale("Ranks fast Crush DPS and all three Scythe hits rather than generic melee stats.")
					.combatStyle(CombatStyle.MELEE).attackType(AttackType.CRUSH)
					.magicDefenceWeight(0.28).targetTrait(TargetTrait.SCYTHE_THREE_HIT).build()),
			"sarachnis");

		registerBoss(profile("vardorvis-boss", "Vardorvis",
				"Slash is substantially stronger than Crush or Stab against Vardorvis.",
				"Defence-draining special attacks do not work; use damage or sustain switches.",
				GearStrategy.builder().name("Vardorvis - Slash").location("The Stranglewood")
					.rationale("Ranks owned Slash main weapons, the Scythe's two-hit target value, and the Wiki's Oathplate-over-Torva armour order.")
					.combatStyle(CombatStyle.MELEE).attackType(AttackType.SLASH)
					.targetTrait(TargetTrait.SCYTHE_TWO_HIT)
					.preferredItem("oathplate").build()),
			"vardorvis");

		registerBoss(profile("skotizo-boss", "Skotizo",
				"Demonbane slash is preferred, and the Wiki ranks Oathplate above Torva for the offensive melee setup.",
				"Protect from Magic with offensive melee gear, or use a magic-defence setup with Protect from Melee.",
				GearStrategy.builder().name("Skotizo - Demonbane slash").location("Catacombs of Kourend")
					.rationale("Ranks Emberlight/Arclight with the Wiki's Oathplate-over-Torva armour order.")
					.combatStyle(CombatStyle.MELEE).attackType(AttackType.SLASH)
					.weaponRule(WeaponRule.DEMONBANE).targetTrait(TargetTrait.DEMON)
					.preferredItem("oathplate").build()),
			"skotizo");

		registerBoss(profile("abyssal-sire-boss", "Abyssal Sire",
				"Demonbane melee is preferred for the main damage phases.",
				"Respiratory-system and phase switches still require manual encounter planning.",
				GearStrategy.builder().name("Abyssal Sire - Demonbane").location("Abyssal Nexus")
					.rationale("Ranks phase-two Demonbane and three-hit Scythe main-hand damage.")
					.combatStyle(CombatStyle.MELEE).attackType(AttackType.SLASH)
					.weaponRule(WeaponRule.DEMONBANE).targetTrait(TargetTrait.DEMON)
					.targetTrait(TargetTrait.SCYTHE_THREE_HIT).build()),
			"the abyssal sire", "abyssal sire");

		registerBoss(profile("kalphite-queen-boss", "Kalphite Queen",
				"Kalphite weapon effects and Crush-capable melee are ranked for the first phase.",
				"The second phase requires a Ranged or Magic switch that is not a replacement main weapon.",
				GearStrategy.builder().name("Kalphite Queen — Keris / Crush").location("Kalphite Queen lair")
					.rationale("Ranks valid Crush weapons while retaining Keris-family effects.")
					.combatStyle(CombatStyle.MELEE).attackType(AttackType.CRUSH)
					.weaponRule(WeaponRule.KALPHITE).targetTrait(TargetTrait.KALPHITE)
					.targetTrait(TargetTrait.SCYTHE_THREE_HIT).build()),
			"the kalphite queen", "kalphite queen");

		registerBoss(profile("vetion-boss", "Vet'ion",
				"Crush weapons are preferred and charged Wilderness weapons retain their Wilderness bonus.",
				"Use Wilderness-appropriate risk and escape planning.",
				GearStrategy.builder().name("Vet'ion — Wilderness Crush").location("Wilderness")
					.rationale("Ranks Crush DPS with charged Wilderness-weapon effects.")
					.combatStyle(CombatStyle.MELEE).attackType(AttackType.CRUSH)
					.targetTrait(TargetTrait.WILDERNESS).targetTrait(TargetTrait.UNDEAD).build()),
			"vet'ion", "vetion");

		registerBoss(profile("thermonuclear-smoke-devil-boss", "Thermonuclear Smoke Devil",
				"The melee method follows the current Wiki equipment table instead of generic raw-stat ordering.",
				"A Slayer helmet or facemask is mandatory in the smoke; Redemption and Thralls are optional method settings.",
				GearStrategy.builder().name("Thermy - Melee / Redemption").location("Smoke Devil Dungeon")
					.rationale("Wiki weapon order: Scythe, Soulreaper axe, Fang/Blade, then Noxious halberd/Inquisitor's mace.")
					.combatStyle(CombatStyle.MELEE).attackType(AttackType.BALANCED)
					.rankedWeapon("scythe of vitur")
					.rankedWeapon("soulreaper axe")
					.rankedWeapon("osmumten's fang")
					.rankedWeapon("blade of saeldor")
					.rankedWeapon("noxious halberd")
					.rankedWeapon("inquisitor's mace")
					.rankedWeapon("abyssal tentacle")
					.rankedWeapon("ghrazi rapier")
					.preferredItem("amulet of rancour")
					.preferredItem("bellator ring").build()),
			"the thermonuclear smoke devil", "thermonuclear smoke devil");

		SlayerTaskProfile meleeBoss = profile("melee-boss", "Melee boss",
			"Boss-task fallback using offensive melee gear.",
			"Verify encounter-specific switches and supplies.",
			melee("Boss melee", "Boss lair", AttackType.BALANCED, "General melee shortlist"));
		register(meleeBoss,
			"the giant mole", "the grotesque guardians",
			"the maggot king");

		SlayerTaskProfile rangedBoss = profile("ranged-boss", "Ranged boss",
			"Boss-task fallback using offensive Ranged gear.",
			"Verify encounter-specific switches and supplies.",
			ranged("Boss Ranged", "Boss lair", "General ranged shortlist"));
		register(rangedBoss,
			"the alchemical hydra", "callisto", "the chaos elemental",
			"the chaos fanatic", "general graardor", "kree'arra",
			"the leviathan", "the phantom muspah", "commander zilyana",
			"venenatis", "zulrah", "dagannoth kings",
			"crazy archaeologists", "deranged archaeologist");

		SlayerTaskProfile magicBoss = profile("magic-boss", "Magic boss",
			"Boss-task fallback using offensive Magic gear.",
			"Verify encounter-specific switches and supplies.",
			magic("Boss Magic", "Boss lair", "General magic shortlist"));
		register(magicBoss,
			"barrows brothers", "scorpia", "the whisperer");

		register(profile("demon-boss", "Demon boss",
				"Demonbane weapons receive explicit priority.",
				"Verify encounter-specific mechanics.",
				demonMelee("Demonbane boss melee", "Boss lair", "Demonbane shortlist")),
			"k'ril tsutsaroth", "kril tsutsaroth");

		register(profile("dragon-boss", "Dragon boss",
				"Dragonbane Ranged is preferred, followed by stab melee.",
				"Use encounter-appropriate dragonfire protection.",
				dragonRanged("Dragonbane Ranged", "Boss lair"),
				dragonMelee("Dragonbane melee", "Boss lair")),
			"the king black dragon", "king black dragon", "vorkath");
	}

	private static SlayerTaskProfile generic(String taskName)
	{
		String display = taskName == null || taskName.trim().isEmpty() ? "Slayer task" : taskName.trim();
		String key = normalize(display).replaceAll("[^a-z0-9]+", "-");
		return profile(key, display,
			"General owned-gear profile. Specialized task safety rules and supplies are still enforced where known.",
			"Verify location-specific mechanics before leaving the bank.",
			melee("General Slayer melee", "Task-dependent", AttackType.BALANCED,
				"Safe fallback that ranks your strongest owned melee setup."));
	}

	private static String normalize(String value)
	{
		String normalized = value.trim().toLowerCase(Locale.ENGLISH);
		return normalized.startsWith("the ") ? normalized.substring(4) : normalized;
	}
}
