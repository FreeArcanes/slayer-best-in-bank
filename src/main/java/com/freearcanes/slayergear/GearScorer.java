package com.freearcanes.slayergear;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.client.game.ItemEquipmentStats;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStats;

/** Scores owned equipment and builds coherent whole-loadout tiers. */
class GearScorer
{
	private static final Set<EquipmentInventorySlot> SUPPORTED_SLOTS = Set.of(
		EquipmentInventorySlot.HEAD, EquipmentInventorySlot.CAPE, EquipmentInventorySlot.AMULET,
		EquipmentInventorySlot.WEAPON, EquipmentInventorySlot.BODY, EquipmentInventorySlot.SHIELD,
		EquipmentInventorySlot.LEGS, EquipmentInventorySlot.GLOVES, EquipmentInventorySlot.BOOTS,
		EquipmentInventorySlot.RING, EquipmentInventorySlot.AMMO);

	/*
	 * Monster-family passives multiply the player's effective attack/max-hit
	 * rolls, not merely the visible bonuses printed on the weapon itself.
	 * These shared baselines represent the offensive value supplied by levels,
	 * the rest of the worn setup, ammo/spell base damage, prayers and boosts.
	 */
	private static final double WEAPON_SHARED_DAMAGE_BASE = 500.0;
	private static final double WEAPON_SHARED_ACCURACY_BASE = 100.0;

	private final ItemManager itemManager;
	private final SmartSupplyAdvisor supplyAdvisor;

	@Inject
	GearScorer(ItemManager itemManager, SmartSupplyAdvisor supplyAdvisor)
	{
		this.itemManager = itemManager;
		this.supplyAdvisor = supplyAdvisor;
	}

	GearRecommendations score(String taskName, int taskAmount, SlayerTaskProfile profile,
		Item[] gearPool, Item[] bankItems, Item[] packedItems, int alternativesPerSlot,
		int magicLevel, int rangedLevel, boolean kourendEliteComplete, boolean ancientSpellbookActive, String preferredStrategy, GearPriority gearPriority, String pinnedItems, String excludedItems, boolean lowRiskMode, int riskCapGp)
	{
		Set<Integer> bankCanonical = canonicalIds(bankItems);
		Set<Integer> packedCanonical = canonicalIds(packedItems);
		List<BankEquipment> equipment = collectEquipment(gearPool, bankCanonical, packedCanonical);
		Set<String> ownedNames = collectOwnedNames(equipment);
		List<GearStrategy> eligible = eligibleStrategies(profile, ownedNames, magicLevel, rangedLevel);
		GearStrategy selected = selectStrategy(profile, eligible, preferredStrategy);
		List<GearStrategy> alternatives = new ArrayList<>(eligible);
		alternatives.remove(selected);

		Set<String> pinned = parsePreferenceTokens(pinnedItems);
		Set<String> excluded = parsePreferenceTokens(excludedItems);
		List<GearRequirement> requirements = TaskSafetyRules.gearRequirements(profile.getKey(), selected, kourendEliteComplete);
		Map<EquipmentInventorySlot, List<BankEquipment>> candidates = buildCandidates(
			equipment, selected, requirements, gearPriority, pinned, excluded, lowRiskMode, riskCapGp);

		int tiersWanted = Math.max(1, Math.min(3, alternativesPerSlot));
		List<LoadoutTier> loadoutTiers = new ArrayList<>();
		Map<EquipmentInventorySlot, List<GearRecommendation>> bySlot = new EnumMap<>(EquipmentInventorySlot.class);
		for (int rank = 1; rank <= tiersWanted; rank++)
		{
			Map<EquipmentInventorySlot, GearRecommendation> loadout = buildLoadout(rank, candidates, selected, requirements);
			if (loadout.isEmpty()) break;
			loadoutTiers.add(new LoadoutTier(rank, loadout));
			for (Map.Entry<EquipmentInventorySlot, GearRecommendation> entry : loadout.entrySet())
			{
				List<GearRecommendation> slotRecommendations = bySlot.computeIfAbsent(entry.getKey(), ignored -> new ArrayList<>());
				boolean alreadyListed = slotRecommendations.stream()
					.anyMatch(existing -> existing.getCanonicalItemId() == entry.getValue().getCanonicalItemId());
				if (!alreadyListed)
				{
					slotRecommendations.add(entry.getValue());
				}
			}
		}

		Map<EquipmentInventorySlot, GearRecommendation> best = loadoutTiers.isEmpty()
			? Collections.emptyMap() : loadoutTiers.get(0).getItems();
		List<SupplyRecommendation> supplies = supplyAdvisor.recommend(profile, selected, bankItems, packedItems);
		// A protective off-hand already satisfies dragonfire protection. Do not
		// simultaneously tell the player that antifire is still required.
		if (hasDragonfireProtection(best))
		{
			supplies = withoutCategory(supplies, "Antifire");
		}
		ReadinessReport readiness = readiness(best, requirements, supplies, selected, magicLevel, ancientSpellbookActive);

		return GearRecommendations.ready(taskName, taskAmount, profile, selected, alternatives,
			bySlot, loadoutTiers, supplies, readiness, equipment.size());
	}

	private List<GearStrategy> eligibleStrategies(SlayerTaskProfile profile, Set<String> names, int magic, int ranged)
	{
		List<GearStrategy> result = new ArrayList<>();
		for (GearStrategy s : profile.getStrategies()) if (isEligible(s, names, magic, ranged)) result.add(s);
		if (result.isEmpty() && !profile.getStrategies().isEmpty()) result.add(profile.getStrategies().get(profile.getStrategies().size() - 1));
		return result;
	}

	private static GearStrategy selectStrategy(SlayerTaskProfile profile, List<GearStrategy> eligible, String preferred)
	{
		if (preferred != null && !preferred.trim().isEmpty())
		{
			String want = NameMatcher.normalize(preferred);
			for (GearStrategy s : eligible) if (NameMatcher.normalize(s.getName()).equals(want)) return s;
		}
		return eligible.isEmpty() ? profile.getStrategies().get(0) : eligible.get(0);
	}

	private Map<EquipmentInventorySlot, List<BankEquipment>> buildCandidates(List<BankEquipment> equipment,
		GearStrategy strategy, List<GearRequirement> requirements, GearPriority gearPriority, Set<String> pinned, Set<String> excluded,
		boolean lowRiskMode, int riskCapGp)
	{
		Map<EquipmentInventorySlot, List<BankEquipment>> result = new EnumMap<>(EquipmentInventorySlot.class);
		boolean shieldRequired = requiresMandatoryOffhand(requirements);
		for (BankEquipment item : equipment)
		{
			boolean explicitlyPinned = matchesAnyPreference(item.name, pinned);
			if (matchesAnyPreference(item.name, excluded) || !allowed(item, strategy)) continue;
			if (lowRiskMode && !explicitlyPinned && riskCapGp > 0)
			{
				int price = itemManager.getItemPrice(item.itemId);
				if (price > riskCapGp) continue;
			}
			// Safety beats raw DPS: a mandatory off-hand makes every 2H weapon an
			// invalid candidate, otherwise the protection pass could create an
			// impossible weapon + shield loadout.
			if (shieldRequired && item.slot == EquipmentInventorySlot.WEAPON && item.stats.isTwoHanded()) continue;
			boolean blocked = false;
			for (GearRequirement req : requirements)
			{
				if (req.restricts(item.slot) && !req.matchesForSlot(item.slot, item.name)) { blocked = true; break; }
			}
			if (blocked) continue;
			item.score = scoreStats(strategy, item.name, item.slot, item.stats, gearPriority);
			if (explicitlyPinned) item.score += 5_000;
			result.computeIfAbsent(item.slot, ignored -> new ArrayList<>()).add(item);
		}
		for (List<BankEquipment> values : result.values())
			values.sort(Comparator.comparingDouble((BankEquipment i) -> i.score).reversed().thenComparing(i -> i.name));
		return result;
	}

	static boolean requiresMandatoryOffhand(List<GearRequirement> requirements)
	{
		return requirements != null && requirements.stream()
			.anyMatch(requirement -> requirement.restricts(EquipmentInventorySlot.SHIELD));
	}

	private Map<EquipmentInventorySlot, GearRecommendation> buildLoadout(int rank,
		Map<EquipmentInventorySlot, List<BankEquipment>> candidates, GearStrategy strategy, List<GearRequirement> requirements)
	{
		EnumMap<EquipmentInventorySlot, GearRecommendation> selected = new EnumMap<>(EquipmentInventorySlot.class);
		for (EquipmentInventorySlot slot : SUPPORTED_SLOTS)
		{
			List<BankEquipment> list = candidates.getOrDefault(slot, Collections.emptyList());
			if (rank <= list.size())
			{
				selected.put(slot, recommendation(list.get(rank - 1), rank, strategy));
			}
		}

		// Whole-loadout compatibility: a 2H weapon consumes the off-hand.
		GearRecommendation weapon = selected.get(EquipmentInventorySlot.WEAPON);
		if (weapon != null && weapon.isTwoHanded()) selected.remove(EquipmentInventorySlot.SHIELD);

		// Match ammo to weapon type; self-ammo weapons intentionally omit the ammo slot.
		if (weapon != null && strategy.getCombatStyle() == CombatStyle.RANGED)
		{
			String w = NameMatcher.normalize(weapon.getItemName());
			if (usesNoAmmoSlot(w)) selected.remove(EquipmentInventorySlot.AMMO);
			else
			{
				List<BankEquipment> ammo = candidates.getOrDefault(EquipmentInventorySlot.AMMO, Collections.emptyList());
				BankEquipment compatible = nthCompatibleAmmo(ammo, w, rank);
				if (compatible == null) selected.remove(EquipmentInventorySlot.AMMO);
				else selected.put(EquipmentInventorySlot.AMMO, recommendation(compatible, rank, strategy));
			}
		}

		// Force mandatory protection into the cohesive loadout when it is owned.
		for (GearRequirement req : requirements)
		{
			if (req.isSatisfied(selected)) continue;
			for (GearRequirement.Option option : req.getOptions())
			{
				List<BankEquipment> list = candidates.getOrDefault(option.getSlot(), Collections.emptyList());
				for (BankEquipment item : list)
				{
					if (option.matches(item.name))
					{
						selected.put(option.getSlot(), recommendation(item, rank, strategy));
						break;
					}
				}
				if (req.isSatisfied(selected)) break;
			}
		}
		return selected;
	}

	private GearRecommendation recommendation(BankEquipment i, int rank, GearStrategy strategy)
	{
		return GearRecommendation.builder().itemId(i.itemId).canonicalItemId(i.canonicalItemId)
			.itemName(i.name).slot(i.slot).score(i.score).rank(rank).twoHanded(i.stats.isTwoHanded())
			.reason(explain(strategy, i.name, i.slot, i.stats)).packed(i.packed).banked(i.banked).build();
	}

	private ReadinessReport readiness(Map<EquipmentInventorySlot, GearRecommendation> selected,
		List<GearRequirement> requirements, List<SupplyRecommendation> supplies, GearStrategy strategy, int magicLevel, boolean ancientSpellbookActive)
	{
		List<String> missing = new ArrayList<>();
		boolean protection = true;
		for (GearRequirement req : requirements)
		{
			if (!req.isSatisfied(selected)) { protection = false; missing.add(req.getLabel()); }
		}
		GearRecommendation weapon = selected.get(EquipmentInventorySlot.WEAPON);
		boolean ammoReady = weapon != null;
		if (weapon == null)
		{
			missing.add("Compatible " + strategy.getCombatStyle().name().toLowerCase(Locale.ENGLISH) + " weapon");
		}
		if (weapon != null && strategy.getCombatStyle() == CombatStyle.RANGED && !usesNoAmmoSlot(NameMatcher.normalize(weapon.getItemName())))
		{
			ammoReady = selected.containsKey(EquipmentInventorySlot.AMMO);
			if (!ammoReady) missing.add("Compatible ammunition");
		}
		int packedGear = 0;
		for (GearRecommendation r : selected.values()) if (r.isPacked()) packedGear++;
		int gearTotal = selected.size();
		int suppliesPacked = 0, suppliesTotal = 0;
		boolean dragonfireShieldReady = hasDragonfireProtection(selected);
		for (SupplyRecommendation s : supplies)
		{
			// Cannon components are ground equipment and are rendered inside the Tier 1
			// loadout. Count the four required parts as gear readiness; cannonballs
			// remain a trip supply. This keeps the readiness strip consistent with
			// what the player sees in the loadout section.
			if ("Cannon setup".equals(s.getCategory()))
			{
				gearTotal++;
				if (s.getStatus().isPacked()) packedGear++;
			}
			else
			{
				suppliesTotal++;
				if (s.getStatus().isPacked()) suppliesPacked++;
			}

			boolean requiredHere = s.isRequired()
				&& !("Antifire".equals(s.getCategory()) && dragonfireShieldReady);
			if (requiredHere && !s.getStatus().isPacked())
			{
				missing.add(s.getStatus().isBanked()
					? "Pack " + s.getItemName()
					: s.getCategory() + ": " + s.getItemName());
			}
		}
		String spell = "Not required";
		if (strategy != null && strategy.isAncientAoe())
		{
			String highest = highestAncientAoe(magicLevel);
			if (magicLevel < 62)
			{
				spell = highest;
				missing.add("Ancient AoE spell level (62+ Magic)");
			}
			else if (!ancientSpellbookActive)
			{
				spell = highest + " • Ancient spellbook inactive";
				missing.add("Switch to the Ancient Magicks spellbook");
			}
			else
			{
				spell = highest + " • spellbook ready";
			}
		}
		return new ReadinessReport(packedGear, gearTotal, protection, ammoReady, spell,
			suppliesPacked, suppliesTotal, missing);
	}

	static List<SupplyRecommendation> withoutCategory(List<SupplyRecommendation> supplies, String category)
	{
		if (supplies == null || supplies.isEmpty()) return Collections.emptyList();
		List<SupplyRecommendation> filtered = new ArrayList<>();
		for (SupplyRecommendation supply : supplies)
		{
			if (!category.equals(supply.getCategory())) filtered.add(supply);
		}
		return filtered;
	}

	private static boolean hasDragonfireProtection(Map<EquipmentInventorySlot, GearRecommendation> selected)
	{
		GearRecommendation shield = selected.get(EquipmentInventorySlot.SHIELD);
		if (shield == null) return false;
		String name = NameMatcher.normalize(shield.getItemName());
		return name.contains("anti dragon shield")
			|| name.contains("dragonfire shield")
			|| name.contains("dragonfire ward");
	}

	static String highestAncientAoe(int level)
	{
		if (level >= 94) return "Ice Barrage";
		if (level >= 92) return "Blood Barrage";
		if (level >= 88) return "Shadow Barrage";
		if (level >= 86) return "Smoke Barrage";
		if (level >= 70) return "Ice Burst";
		if (level >= 68) return "Blood Burst";
		if (level >= 64) return "Shadow Burst";
		if (level >= 62) return "Smoke Burst";
		return "Ancient AoE unavailable";
	}

	static double scoreStats(GearStrategy strategy, String itemName, EquipmentInventorySlot slot, ItemEquipmentStats stats)
	{
		return scoreStats(strategy, itemName, slot, stats, GearPriority.BALANCED);
	}

	static double scoreStats(GearStrategy strategy, String itemName, EquipmentInventorySlot slot,
		ItemEquipmentStats stats, GearPriority gearPriority)
	{
		double damage;
		double accuracy;
		boolean prayerFirst = gearPriority == GearPriority.PRAYER_FIRST;

		// Prayer First changes sustain gear, not the combat-optimal weapon.
		double prayerWeight = prayerFirst && slot != EquipmentInventorySlot.WEAPON
			? 200.0
			: strategy.getPrayerWeight();

		double utility = stats.getPrayer() * prayerWeight
			+ stats.getDmagic() * strategy.getMagicDefenceWeight();

		switch (strategy.getCombatStyle())
		{
			case MAGIC:
				damage = stats.getMdmg() * 25.0;
				accuracy = stats.getAmagic() * .28;
				break;
			case RANGED:
				damage = stats.getRstr() * 5.0;
				accuracy = stats.getArange() * .32;
				break;
			default:
				damage = stats.getStr() * 5.0;
				accuracy = attackBonus(strategy.getAttackType(), stats) * .34;
				break;
		}

		if (slot == EquipmentInventorySlot.WEAPON)
		{
			double damageMultiplier = WeaponCombatRules.damageMultiplier(strategy, itemName);
			double accuracyMultiplier = WeaponCombatRules.accuracyMultiplier(strategy, itemName);

			/*
			 * Apply monster-specific passives to a proxy for the whole attack.
			 * This fixes cases such as Emberlight vs Abyssal whip on demons:
			 * Emberlight's +70% effect scales the wielder's attack/max hit in game,
			 * not only Emberlight's own small +Strength bonus.
			 */
			damage = (WEAPON_SHARED_DAMAGE_BASE + damage) * damageMultiplier;
			accuracy = (WEAPON_SHARED_ACCURACY_BASE + accuracy) * accuracyMultiplier;

			// Rat-bone weapons add flat max hit instead of a multiplier.
			damage += WeaponCombatRules.flatDamageScore(strategy, itemName);

			// Attack speed scales the whole attack contribution.
			if (stats.getAspeed() > 0)
			{
				double speedScale = 4.0 / stats.getAspeed();
				damage *= speedScale;
				accuracy *= speedScale;
			}
		}
		else if (prayerFirst)
		{
			// Prayer dominates non-weapon sustain gear; offence remains a tie-breaker.
			damage *= 0.25;
			accuracy *= 0.10;
		}

		double score = damage + accuracy + utility;
		String n = NameMatcher.normalize(itemName);

		if (slot == EquipmentInventorySlot.HEAD && (n.contains("slayer helm") || n.startsWith("black mask"))
			&& (strategy.getCombatStyle() == CombatStyle.MELEE || n.contains("(i)") || n.contains("imbued")))
		{
			score += 1200;
		}

		if (slot == EquipmentInventorySlot.AMMO && WeaponCombatRules.isFieryPearlAmmo(strategy, itemName))
		{
			score += 35;
		}

		// Curated names are tie-breakers; required weapons are enforced elsewhere.
		for (int x = 0; x < strategy.getPreferredItems().size(); x++)
		{
			if (n.contains(NameMatcher.normalize(strategy.getPreferredItems().get(x))))
			{
				score += Math.max(40, 160 - x * 20);
				break;
			}
		}

		return score;
	}

	private List<BankEquipment> collectEquipment(Item[] items, Set<Integer> bank, Set<Integer> packed)
	{
		Map<Integer, BankEquipment> dedup = new HashMap<>();
		if (items == null) return new ArrayList<>();
		for (Item item : items)
		{
			if (item == null || item.getId() <= 0 || item.getQuantity() <= 0) continue;
			ItemComposition comp = itemManager.getItemComposition(item.getId());
			if (comp == null || comp.getPlaceholderTemplateId() != -1) continue;
			int canonical = itemManager.canonicalize(item.getId());
			ItemStats stat = itemManager.getItemStats(item.getId());
			if (stat == null) stat = itemManager.getItemStats(canonical);
			if (stat == null || !stat.isEquipable() || stat.getEquipment() == null) continue;
			EquipmentInventorySlot slot = slotFor(stat.getEquipment().getSlot());
			if (slot == null || !SUPPORTED_SLOTS.contains(slot)) continue;
			String name = comp.getName();
			if (name == null || name.trim().isEmpty() || "null".equalsIgnoreCase(name)) continue;
			BankEquipment candidate = new BankEquipment(item.getId(), canonical, name, slot, stat.getEquipment(), bank.contains(canonical), packed.contains(canonical));
			BankEquipment existing = dedup.get(canonical);
			if (existing == null || (!existing.packed && candidate.packed)) dedup.put(canonical, candidate);
		}
		return new ArrayList<>(dedup.values());
	}

	private Set<Integer> canonicalIds(Item[] items)
	{
		Set<Integer> result = new HashSet<>(); if (items == null) return result;
		for (Item i : items) if (i != null && i.getId() > 0 && i.getQuantity() > 0) result.add(itemManager.canonicalize(i.getId()));
		return result;
	}

	private static Set<String> collectOwnedNames(List<BankEquipment> items) { Set<String> r=new HashSet<>(); for(BankEquipment i:items) r.add(NameMatcher.normalize(i.name)); return r; }
	private static Set<String> parsePreferenceTokens(String s) { Set<String> r=new HashSet<>(); if(s!=null) for(String t:s.split(",")) if(!t.trim().isEmpty()) r.add(NameMatcher.normalize(t)); return r; }
	private static boolean matchesAnyPreference(String name, Set<String> tokens) { String n=NameMatcher.normalize(name); for(String t:tokens) if(n.contains(t)) return true; return false; }

	private static boolean isEligible(GearStrategy s, Set<String> names, int magic, int ranged)
	{
		if (s.getCombatStyle()==CombatStyle.MAGIC && magic<s.getMinimumMagic()) return false;
		if (s.getCombatStyle()==CombatStyle.RANGED && ranged<s.getMinimumRanged()) return false;
		return s.getRequiredWeapon()==null || names.stream().anyMatch(n -> NameMatcher.matchesAnyToken(n, s.getRequiredWeapon()));
	}

	static boolean allowed(BankEquipment item, GearStrategy strategy)
	{
		if (item.slot != EquipmentInventorySlot.WEAPON) return true;
		String n = NameMatcher.normalize(item.name);
		if (!WeaponCombatRules.usableOnTarget(strategy, n)) return false;
		if (!matchesCombatStyle(strategy.getCombatStyle(), n, item.stats)) return false;

		// A strategy that names a required weapon (for example Venator bow) is a
		// real loadout constraint, not merely an eligibility check.
		if (strategy.getRequiredWeapon() != null
			&& !NameMatcher.matchesAnyToken(n, strategy.getRequiredWeapon())) return false;

		if (strategy.getCombatStyle() == CombatStyle.MELEE
			&& !WeaponCombatRules.supportsAttackType(n, strategy.getAttackType())
			&& !WeaponCombatRules.hasTargetSpecificEffect(strategy, n)) return false;

		if (strategy.getWeaponRule() == WeaponRule.LEAF_BLADED)
		{
			if (strategy.getCombatStyle() == CombatStyle.MELEE && !n.contains("leaf-bladed")) return false;
			if (strategy.getCombatStyle() == CombatStyle.MAGIC && !n.contains("slayer's staff")) return false;
		}

		if (strategy.getWeaponRule() == WeaponRule.VAMPYRE)
		{
			return n.contains("sunspear") || n.contains("hallowed flail") || n.contains("blisterwood")
				|| n.contains("ivandis") || n.contains("rod of ivandis") || n.contains("silverlight")
				|| n.contains("darklight") || n.contains("arclight") || n.contains("emberlight");
		}
		return true;
	}

	private static boolean matchesCombatStyle(CombatStyle style,String n,ItemEquipmentStats s)
	{
		switch(style){case MAGIC:return s.getAmagic()>0||s.getMdmg()>0||has(n,"staff","wand","sceptre","trident","tome");case RANGED:return s.getArange()>0||s.getRstr()>0||has(n,"bow","crossbow","blowpipe","atlatl","chinchompa");default:return s.getStr()>0||s.getAstab()>0||s.getAslash()>0||s.getAcrush()>0;}
	}
	static boolean usesNoAmmoSlot(String weapon)
	{
		return has(weapon, "blowpipe", "crystal bow", "bow of faerdhinen", "chinchompa",
			" dart", "knife", "thrownaxe", "javelin", "toktz-xil-ul", "holy water");
	}

	private static BankEquipment nthCompatibleAmmo(List<BankEquipment> ammo, String weapon, int rank)
	{
		String token = weapon.contains("crossbow") ? "bolt"
			: weapon.contains("atlatl") ? "atlatl dart"
			: weapon.contains("ballista") ? "javelin"
			: weapon.contains("salamander") ? "tar"
			: "arrow";
		List<BankEquipment> compatible = new ArrayList<>();
		for (BankEquipment candidate : ammo)
		{
			if (NameMatcher.normalize(candidate.name).contains(token)) compatible.add(candidate);
		}
		return rank <= 0 || rank > compatible.size() ? null : compatible.get(rank - 1);
	}
	private static boolean has(String v,String...t){for(String x:t)if(v.contains(x))return true;return false;}
	private static int attackBonus(AttackType a,ItemEquipmentStats s){switch(a){case STAB:return s.getAstab();case SLASH:return s.getAslash();case CRUSH:return s.getAcrush();default:return Math.max(s.getAstab(),Math.max(s.getAslash(),s.getAcrush()));}}
	private static EquipmentInventorySlot slotFor(int i){for(EquipmentInventorySlot s:EquipmentInventorySlot.values())if(s.getSlotIdx()==i)return s;return null;}

	private static String explain(GearStrategy strategy,String name,EquipmentInventorySlot slot,ItemEquipmentStats stats)
	{
		List<String> r = new ArrayList<>();
		if (slot == EquipmentInventorySlot.WEAPON)
		{
			String affinity = WeaponCombatRules.affinityReason(strategy, name);
			if (affinity != null) r.add(affinity);
		}
		else if (slot == EquipmentInventorySlot.AMMO && WeaponCombatRules.isFieryPearlAmmo(strategy, name))
		{
			r.add("Fiery-target Sea Curse bonus");
		}
		for(String p:strategy.getPreferredItems())if(NameMatcher.normalize(name).contains(NameMatcher.normalize(p))){r.add("task-method priority");break;}
		switch(strategy.getCombatStyle()){case MAGIC:add(r,stats.getMdmg(),"% magic dmg");add(r,stats.getAmagic(),"magic");break;case RANGED:add(r,stats.getRstr(),"ranged Str");add(r,stats.getArange(),"ranged");break;default:add(r,stats.getStr(),"melee Str");add(r,attackBonus(strategy.getAttackType(),stats),strategy.getAttackType().name().toLowerCase(Locale.ENGLISH));}
		add(r,stats.getPrayer(),"prayer"); if(slot==EquipmentInventorySlot.WEAPON&&stats.getAspeed()>0)r.add(stats.getAspeed()+"-tick speed"); if(r.isEmpty())r.add("best weighted stats available"); return String.join(", ",r);
	}
	private static void add(List<String> r,float v,String label){if(v!=0)r.add((v>0?"+":"")+(v==Math.rint(v)?Integer.toString((int)v):Float.toString(v))+" "+label);}

	private static final class BankEquipment
	{
		final int itemId,canonicalItemId; final String name; final EquipmentInventorySlot slot; final ItemEquipmentStats stats; final boolean banked,packed; double score;
		BankEquipment(int itemId,int canonical,String name,EquipmentInventorySlot slot,ItemEquipmentStats stats,boolean banked,boolean packed){this.itemId=itemId;this.canonicalItemId=canonical;this.name=name;this.slot=slot;this.stats=stats;this.banked=banked;this.packed=packed;}
	}
}