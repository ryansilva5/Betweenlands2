package thebetweenlands.common.item.armor.amphibian;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import thebetweenlands.api.item.IAmphibianArmorAttributeUpgrade;
import thebetweenlands.api.item.IAmphibianArmorUpgrade;
import thebetweenlands.common.capability.circlegem.CircleGemType;
import thebetweenlands.common.item.misc.ItemMisc.EnumItemMisc;
import thebetweenlands.common.lib.ModInfo;
import thebetweenlands.common.registries.ItemRegistry;

public enum AmphibianArmorUpgrades implements IAmphibianArmorUpgrade {
	VISIBILITY(new ResourceLocation(ModInfo.ID, "visibility"), 64, EnumItemMisc.ANADIA_EYE::isItemOf, EntityEquipmentSlot.HEAD),
	BREATHING(new ResourceLocation(ModInfo.ID, "breathing"), 64, EnumItemMisc.ANADIA_GILLS::isItemOf, EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST),
	TOUGHNESS(new ResourceLocation(ModInfo.ID, "toughness"), 64, EnumItemMisc.SLIMY_BONE::isItemOf, AdditiveAttributeUpgrade.TOUGHNESS, EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET),
	THORNS(new ResourceLocation(ModInfo.ID, "thorns"), 64, EnumItemMisc.URCHIN_SPIKE::isItemOf, EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET),
	DECAY_DECREASE(new ResourceLocation(ModInfo.ID, "decay_decrease"), 64, EnumItemMisc.ANADIA_SCALES::isItemOf, EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET),
	MINING_SPEED(new ResourceLocation(ModInfo.ID, "mining_speed"), 64, EnumItemMisc.SNOT::isItemOf, EntityEquipmentSlot.CHEST),
	MOVEMENT_SPEED(new ResourceLocation(ModInfo.ID, "movement_speed"), 64, EnumItemMisc.ANADIA_FINS::isItemOf, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET),
	BUOYANCY(new ResourceLocation(ModInfo.ID, "buoyancy"), 64, EnumItemMisc.ANADIA_SWIM_BLADDER::isItemOf, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS),
	KNOCKBACK_RESISTANCE(new ResourceLocation(ModInfo.ID, "knockback_resistance"), 64, EnumItemMisc.LURKER_SKIN::isItemOf, AdditiveAttributeUpgrade.KNOCKBACK_RESISTANCE, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS),
	AQUA_GEM(new ResourceLocation(ModInfo.ID, "aqua_gem"), 64, s -> s.getItem() == ItemRegistry.AQUA_MIDDLE_GEM, null, CircleGemType.AQUA.getAmphibianArmorOnChangedHandler(), ImmutableSet.of(new ResourceLocation(ModInfo.ID, "green_gem"), new ResourceLocation(ModInfo.ID, "crimson_gem")), EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET),
	GREEN_GEM(new ResourceLocation(ModInfo.ID, "green_gem"), 64, s -> s.getItem() == ItemRegistry.GREEN_MIDDLE_GEM, null, CircleGemType.GREEN.getAmphibianArmorOnChangedHandler(), ImmutableSet.of(new ResourceLocation(ModInfo.ID, "aqua_gem"), new ResourceLocation(ModInfo.ID, "crimson_gem")), EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET),
	CRIMSON_GEM(new ResourceLocation(ModInfo.ID, "crimson_gem"), 64, s -> s.getItem() == ItemRegistry.CRIMSON_MIDDLE_GEM, null, CircleGemType.CRIMSON.getAmphibianArmorOnChangedHandler(), ImmutableSet.of(new ResourceLocation(ModInfo.ID, "aqua_gem"), new ResourceLocation(ModInfo.ID, "green_gem")), EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET);

	private static final Map<ResourceLocation, IAmphibianArmorUpgrade> ID_TO_UPGRADE = new HashMap<>();
	private static final Multimap<EntityEquipmentSlot, IAmphibianArmorUpgrade> TYPE_TO_UPGRADES = MultimapBuilder.enumKeys(EntityEquipmentSlot.class).arrayListValues().build();

	public static boolean register(IAmphibianArmorUpgrade upgrade) {
		if(ID_TO_UPGRADE.put(upgrade.getId(), upgrade) == null) {
			for(EntityEquipmentSlot armorType : upgrade.getArmorTypes()) {
				TYPE_TO_UPGRADES.put(armorType, upgrade);
			}
			return true;
		}
		return false;
	}

	public static Collection<IAmphibianArmorUpgrade> getUpgrades(EntityEquipmentSlot armorType) {
		return TYPE_TO_UPGRADES.get(armorType);
	}

	@Nullable
	public static IAmphibianArmorUpgrade getUpgrade(EntityEquipmentSlot armorType, ItemStack stack) {
		for(IAmphibianArmorUpgrade upgrade : getUpgrades(armorType)) {
			if(upgrade.matches(armorType, stack)) {
				return upgrade;
			}
		}
		return null;
	}

	@Nullable
	public static IAmphibianArmorUpgrade getUpgrade(ResourceLocation id) {
		return ID_TO_UPGRADE.get(id);
	}

	static {
		for(AmphibianArmorUpgrades upgrade : values()) {
			register(upgrade);
		}
	}

	private final ResourceLocation id;
	private final int maxDamage;
	private final Predicate<ItemStack> matcher;
	private final IAmphibianArmorAttributeUpgrade attributeUpgrade;
	private final Set<EntityEquipmentSlot> armorTypes;
	private final Consumer<ItemStack> onChanged;
	private final Set<ResourceLocation> blacklist;

	private AmphibianArmorUpgrades(ResourceLocation id, int maxDamage, Predicate<ItemStack> matcher, EntityEquipmentSlot... armorTypes) {
		this(id, maxDamage, matcher, null, armorTypes);
	}

	private AmphibianArmorUpgrades(ResourceLocation id, int maxDamage, Predicate<ItemStack> matcher, @Nullable IAmphibianArmorAttributeUpgrade attributeUpgrade, EntityEquipmentSlot... armorTypes) {
		this(id, maxDamage, matcher, attributeUpgrade, null, ImmutableSet.of(), armorTypes);
	}

	private AmphibianArmorUpgrades(ResourceLocation id, int maxDamage, Predicate<ItemStack> matcher, @Nullable IAmphibianArmorAttributeUpgrade attributeUpgrade, Consumer<ItemStack> onChanged, Set<ResourceLocation> blacklist, EntityEquipmentSlot... armorTypes) {
		this.id = id;
		this.maxDamage = maxDamage;
		this.matcher = matcher;
		this.attributeUpgrade = attributeUpgrade;
		this.onChanged = onChanged;
		this.blacklist = blacklist;
		this.armorTypes = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(armorTypes)));
	}

	@Override
	public boolean matches(EntityEquipmentSlot armorType, ItemStack stack) {
		return !stack.isEmpty() && this.armorTypes.contains(armorType) && this.matcher.test(stack);
	}

	@Override
	public Set<EntityEquipmentSlot> getArmorTypes() {
		return this.armorTypes;
	}

	@Override
	public ResourceLocation getId() {
		return this.id;
	}

	@Override
	public void applyAttributeModifiers(EntityEquipmentSlot armorType, ItemStack stack, int count, Multimap<String, AttributeModifier> modifiers) {
		if(this.attributeUpgrade != null) {
			this.attributeUpgrade.applyAttributeModifiers(armorType, stack, count, modifiers);
		}
	}

	@Override
	public void onChanged(EntityEquipmentSlot armorType, ItemStack armor, ItemStack stack) {
		if(this.onChanged != null) {
			this.onChanged.accept(armor);
		}
	}
	
	@Override
	public boolean isBlacklisted(IAmphibianArmorUpgrade other) {
		return this.blacklist.contains(other.getId());
	}

	@Override
	public int getMaxDamage() {
		return this.maxDamage;
	}
}