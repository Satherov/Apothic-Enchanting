package dev.shadowsoffire.apothic_enchanting;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apothic_enchanting.PowerFunction.DefaultMaxPowerFunction;
import dev.shadowsoffire.apothic_enchanting.PowerFunction.DefaultMinPowerFunction;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * EnchantmentInfo retains all configurable per-enchantment data — max level, max loot level, optional hard cap, and
 * the min/max enchanting power functions. Instances are loaded from the {@code apothic_enchanting:enchantment_info}
 * datamap; absent fields fall back to runtime defaults at lookup time.
 * <p>
 * The {@code maxLevel} / {@code maxLootLevel} fields are {@code Optional}: an empty value means "use the scaled
 * default" (via {@link ApothicEnchanting#getDefaultMaxLevel}) and "use the vanilla max" respectively. This lets
 * datapacks override one field without the other.
 */
public record EnchantmentInfo(Optional<Integer> maxLevel, Optional<Integer> maxLootLevel, int levelCap, PowerFunction maxPower, PowerFunction minPower) {

    public static final EnchantmentInfo EMPTY = new EnchantmentInfo(Optional.empty(), Optional.empty(), -1, DefaultMaxPowerFunction.INSTANCE, DefaultMinPowerFunction.INSTANCE);

    public static final MapCodec<EnchantmentInfo> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
        Codec.intRange(1, 127).optionalFieldOf("max_level").forGetter(EnchantmentInfo::maxLevel),
        Codec.intRange(1, 127).optionalFieldOf("max_loot_level").forGetter(EnchantmentInfo::maxLootLevel),
        Codec.intRange(-1, 127).optionalFieldOf("level_cap", -1).forGetter(EnchantmentInfo::levelCap),
        PowerFunction.CODEC.codec().optionalFieldOf("max_power", DefaultMaxPowerFunction.INSTANCE).forGetter(EnchantmentInfo::maxPower),
        PowerFunction.CODEC.codec().optionalFieldOf("min_power", DefaultMinPowerFunction.INSTANCE).forGetter(EnchantmentInfo::minPower))
        .apply(inst, EnchantmentInfo::new));

    /**
     * Returns the max level of the enchantment, clamped by any active {@link ApothicEnchanting#ENCH_HARD_CAPS IMC hard cap}.
     * Falls back to {@link ApothicEnchanting#getDefaultMaxLevel} (cached) if no override is set.
     */
    public int getMaxLevel(Holder<Enchantment> ench) {
        int v = this.maxLevel.orElseGet(() -> ApothicEnchanting.getDefaultMaxLevel(ench));
        return Math.min(ApothicEnchanting.ENCH_HARD_CAPS.getOrDefault(ench.getKey(), 127), v);
    }

    /**
     * Returns the max loot level of the enchantment, clamped by any active hard cap. Loot level is used by loot
     * table generation and villager trades. Falls back to the enchantment's vanilla max level if no override is set.
     */
    public int getMaxLootLevel(Holder<Enchantment> ench) {
        int v = this.maxLootLevel.orElseGet(() -> ench.value().getMaxLevel());
        return Math.min(ApothicEnchanting.ENCH_HARD_CAPS.getOrDefault(ench.getKey(), 127), v);
    }

    /**
     * Minimum enchanting power required to receive {@code level} of this enchantment in an enchanting table.
     */
    public int getMinPower(int level, Holder<Enchantment> ench) {
        return this.minPower.getPower(level, ench);
    }

    /**
     * Maximum enchanting power required to receive {@code level} of this enchantment in an enchanting table.
     * By default returns 200 for all levels.
     */
    public int getMaxPower(int level, Holder<Enchantment> ench) {
        return this.maxPower.getPower(level, ench);
    }

    /**
     * Fallback used when no datamap entry exists for an enchantment. Returns the shared {@link #EMPTY} instance —
     * actual default values are computed lazily by the getters above, allowing the expensive
     * {@link ApothicEnchanting#getDefaultMaxLevel} call to be memoized in one place.
     */
    public static EnchantmentInfo fallback(Holder<Enchantment> ench) {
        return EMPTY;
    }

}
