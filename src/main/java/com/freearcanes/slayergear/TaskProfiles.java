package com.freearcanes.slayergear;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

final class TaskProfiles
{
	private static final Map<String, SlayerTaskProfile> PROFILES = new LinkedHashMap<>();

	static
	{
		register(profile("aberrant-spectres", "Aberrant spectres",
				"Cannon-assisted melee is the practical default; magic defence reduces their spell damage.",
				"A Slayer helmet or nose peg is required.",
				melee("Cannon + melee", "Slayer Tower / Stronghold cave", AttackType.SLASH,
					"Fastest owned melee setup while a cannon supplies extra hits.", "slayer helm", "nose peg")),
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
				"Open with slash to sever the lure, then use stab against the reduced defence.",
				"They attack with Magic, so magic defence and Protect from Magic are useful.",
				melee("Slash switch + stab", "Ynysdail Cavern", AttackType.STAB,
					"Ranks stab DPS, with slash-capable weapons retained as alternatives.",
					"fang", "rapier", "hasta", "spear", "scimitar")),
			"aquanites", "aquanite");

		register(profile("araxytes", "Araxytes",
				"Cannon and Venator bow is the current high-XP normal-task method.",
				"Bring venom protection.",
				venator("Cannon + Venator", "Morytania Spider Nest", "Top multi-target XP method."),
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
				"Protect from Melee prevents damage; otherwise favor magic defence.",
				venator("Cannon + Venator", "Meiyerditch Laboratory / Catacombs",
					"Current top multi-target XP method when a Venator bow is owned."),
				meleeMagicDef("Cannon + melee", "Meiyerditch Laboratory", AttackType.SLASH,
					"Strong fallback using melee damage and magic-defence armour")),
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
				melee("Cannon + melee", "Lighthouse", AttackType.SLASH,
					"High-throughput fallback for normal Dagannoths")),
			"dagannoth", "dagannoths");

		register(profile("dark-beasts", "Dark beasts",
				"A short, straightforward melee task.",
				"Protect from Melee reduces supply use.",
				melee("Melee", "Mourner Tunnels / Iorwerth Dungeon", AttackType.SLASH,
					"Ranks melee damage first")),
			"dark beasts", "dark beast");

		register(profile("drakes", "Drakes",
				"Dragonbane stab gear is prioritized for this draconic task.",
				"Boots of stone or a heat-protecting upgrade may be required in Karuulm.",
				dragonMelee("Dragonbane stab", "Karuulm Slayer Dungeon"),
				ranged("Ranged fallback", "Karuulm Slayer Dungeon", "Safer-distance alternative")),
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
				"One-handed dragonbane combat with an ancient-wyvern-breath shield.",
				"An elemental, mind, dragonfire, or ancient wyvern shield is required.",
				GearStrategy.builder()
					.name("Protected dragonbane Ranged")
					.location("Fossil Island Wyvern Cave")
					.rationale("Ranks ranged dragonbane weapons while enforcing wyvern protection.")
					.combatStyle(CombatStyle.RANGED)
					.requiredOffhand("elemental shield|mind shield|dragonfire shield|ancient wyvern shield")
					.preferredItem("dragon hunter crossbow")
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
				"Crush melee is prioritized.",
				"Keep a rock hammer or rock thrownhammer available for finishing blows.",
				melee("Crush melee", "Slayer Tower", AttackType.CRUSH,
					"Targets their crush weakness", "rock hammer", "rock thrownhammer")),
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
				melee("Heavy melee + cannon", "The Great Conch", AttackType.CRUSH,
					"Balances melee damage with heavy armour", "tortugan shield", "dragonfire shield")),
			"gryphons", "gryphon", "the shellbane gryphon");

		register(profile("hellhounds", "Hellhounds",
				"Venator in the Catacombs is prioritized for normal-task XP; melee follows.",
				"Protect from Melee removes ordinary Hellhound damage.",
				venator("Catacombs Venator", "Catacombs of Kourend",
					"Multi-target, low-effort Slayer XP."),
				melee("Melee / Cerberus", "Catacombs / Cerberus", AttackType.CRUSH,
					"General fallback and boss-capable melee setup")),
			"hellhounds", "hellhound", "cerberus");

		register(profile("kalphites", "Kalphites",
				"Cannon-assisted melee is the fast normal-task method.",
				"Bring poison protection for stronger variants.",
				melee("Cannon + Keris melee", "Kalphite Slayer Cave", AttackType.STAB,
					"Keris weapons receive task-specific priority", "keris")),
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
				ranged("Cannon + Ranged", "Lizardman Canyon / Temple",
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
				melee("Melee fallback", "Slayer Tower / Catacombs", AttackType.SLASH,
					"Profitable single-target fallback")),
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
				"Barraging is the highest-XP Slayer method, with melee retained as a safe fallback.",
				"A Slayer helmet or facemask is required.",
				ancients("Barrage", "Smoke Devil Dungeon",
					"Magic damage and prayer are prioritized over excess accuracy."),
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
				"Use Protect from Magic and solid melee defence.",
				meleeMagicDef("Cannon + melee", "Lunar Isle", AttackType.SLASH,
					"Balances melee damage with magic defence")),
			"suqahs", "suqah");

		register(profile("trolls", "Trolls",
				"Cannon-assisted melee at dense spawns is prioritized.",
				"Use protection prayers against high-damage ice trolls.",
				melee("Cannon + melee", "Jatizso / Mount Quidamortem", AttackType.SLASH,
					"Fast normal-task setup")),
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
				ranged("Fight Caves Ranged", "Fight Caves", "Ranks ranged damage and prayer gear")),
			"tztok-jad", "jad");

		register(profile("zuk", "TzKal-Zuk",
				"Ranged is the primary Inferno setup.",
				"Treat the result as a shortlist; Inferno loadouts are highly account-specific.",
				ranged("Inferno Ranged", "The Inferno", "Ranks ranged damage and prayer gear")),
			"tzkal-zuk", "zuk");

		register(profile("vampyres", "Vampyres",
				"Only valid vampyre weapons are considered.",
				"Higher-tier Vyrewatch require an Ivandis or blisterwood weapon.",
				GearStrategy.builder()
					.name("Vampyre melee")
					.location("Darkmeyer / Meiyerditch")
					.rationale("Prioritizes blisterwood, then Ivandis and other valid silver weapons.")
					.combatStyle(CombatStyle.MELEE)
					.attackType(AttackType.CRUSH)
					.weaponRule(WeaponRule.VAMPYRE)
					.preferredItem("blisterwood flail")
					.preferredItem("ivandis flail")
					.preferredItem("silverlight")
					.build()),
			"vampyres", "vampyre", "vyrewatch");

		register(profile("warped-creatures", "Warped creatures",
				"Cannon-assisted melee is the efficient unlocked-task route.",
				"They use melee and ranged in multicombat; use protection and sustain.",
				melee("Cannon + melee", "Poison Waste Dungeon", AttackType.SLASH,
					"Ranks melee damage for the cannon-supported method")),
			"warped creatures", "warped creature");

		register(profile("waterfiends", "Waterfiends",
				"Crush melee targets their main weakness.",
				"They attack with two styles; defensive balance may help.",
				melee("Crush melee", "Ancient Cavern", AttackType.CRUSH,
					"Crush accuracy receives priority")),
			"waterfiends", "waterfiend");

		register(profile("wyrms", "Wyrms",
				"Dragonbane stab or Ranged is prioritized.",
				"Boots of stone or a heat-protecting upgrade may be required in Karuulm.",
				dragonMelee("Dragonbane stab", "Karuulm Slayer Dungeon"),
				dragonRanged("Dragonbane Ranged", "Karuulm Slayer Dungeon")),
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
				"Use protection prayers as needed while stacking.",
				ancients("Catacombs burst / barrage", "Catacombs of Kourend",
					"Uses the highest Ancient AoE spell your Magic level supports."),
				melee("Melee fallback", "Fremennik Slayer Dungeon / Catacombs", AttackType.SLASH,
					"Single-target fallback when Ancient AoE is unavailable.")),
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
					.weaponRule(WeaponRule.LEAF_BLADED).preferredItem("leaf-bladed battleaxe")
					.preferredItem("leaf-bladed sword").preferredItem("leaf-bladed spear").build()),
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
		if (taskName == null)
		{
			return Optional.empty();
		}
		SlayerTaskProfile exact = PROFILES.get(normalize(taskName));
		if (exact != null)
		{
			return Optional.of(exact);
		}
		return Optional.of(generic(taskName));
	}

	static int profileCount()
	{
		return (int) PROFILES.values().stream().distinct().count();
	}

	private static void register(
		SlayerTaskProfile profile, String... taskNames)
	{
		for (String taskName : taskNames)
		{
			String key = normalize(taskName);
			// First registration wins: a later broad boss alias must never overwrite
			// a task-specific curated profile.
			PROFILES.putIfAbsent(key, profile);
		}
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
		String rationale)
	{
		return GearStrategy.builder()
			.name(name)
			.location(location)
			.rationale(rationale)
			.combatStyle(CombatStyle.MELEE)
			.attackType(attackType)
			.magicDefenceWeight(0.28)
			.build();
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
			.preferredItem("emberlight")
			.preferredItem("arclight")
			.preferredItem("darklight")
			.preferredItem("silverlight")
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
			.preferredItem("dragon hunter lance")
			.preferredItem("zamorakian hasta")
			.build();
	}

	private static GearStrategy dragonRanged(String name, String location)
	{
		return GearStrategy.builder()
			.name(name)
			.location(location)
			.rationale("Prioritizes owned ranged dragonbane weapons.")
			.combatStyle(CombatStyle.RANGED)
			.preferredItem("dragon hunter crossbow")
			.preferredItem("twisted bow")
			.build();
	}

	private static void registerBossAliases()
	{
		SlayerTaskProfile meleeBoss = profile("melee-boss", "Melee boss",
			"Boss-task fallback using offensive melee gear.",
			"Verify encounter-specific switches and supplies.",
			melee("Boss melee", "Boss lair", AttackType.BALANCED, "General melee shortlist"));
		register(meleeBoss,
			"the abyssal sire", "abyssal sire", "araxxor", "cerberus",
			"duke sucellus", "the giant mole", "the grotesque guardians",
			"the kalphite queen", "sarachnis", "vardorvis", "vet'ion",
			"the thermonuclear smoke devil", "the maggot king");

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
			"k'ril tsutsaroth", "kril tsutsaroth", "skotizo");

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
