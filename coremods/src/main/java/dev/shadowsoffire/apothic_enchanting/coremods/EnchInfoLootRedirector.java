package dev.shadowsoffire.apothic_enchanting.coremods;

import java.util.Set;

import net.neoforged.neoforgespi.transformation.ProcessorName;
import net.neoforged.neoforgespi.transformation.SimpleClassProcessor;

/**
 * Replaces every virtual or bootstrap-handle call to {@code Enchantment#getMaxLevel()} in the
 * loot-generation codepath with an {@code INVOKESTATIC} to
 * {@code dev.shadowsoffire.apothic_enchanting.asm.EnchHooks#getMaxLootLevel(Enchantment)}.
 *
 * <p>Ported from the pre-26.1 JS coremod {@code coremods/ench/ench_info_loot_redirector.js}. The
 * loot path uses a separate static hook so Apothic can return a different cap for looted
 * enchantments than for display / anvil contexts (see {@code EnchantmentInfo#getMaxLootLevel}).
 */
public class EnchInfoLootRedirector extends SimpleClassProcessor {

    private static final String HOOK_NAME = "getMaxLootLevel";

    @Override
    public ProcessorName name() {
        return new ProcessorName("apothic_enchanting", "ench_info_loot_redirector");
    }

    @Override
    public Set<Target> targets() {
        // Removed vs 1.21.1: VillagerTrades$EnchantBookForEmeralds — the inner class is gone in 26.1.
        // Villager trades are now data-driven loot tables that dispatch through EnchantRandomlyFunction,
        // which is already in this list, so the effective coverage is preserved.
        return Set.of(
            new Target("net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction"),
            new Target("net.minecraft.world.item.enchantment.EnchantmentHelper"),
            new Target("net.minecraft.world.item.enchantment.providers.SingleEnchantment"));
    }

    @Override
    public void transform(org.objectweb.asm.tree.ClassNode input, net.neoforged.neoforgespi.transformation.SimpleTransformationContext context) {
        EnchantmentCallRewriter.rewriteGetMaxLevelCalls(input, HOOK_NAME);
    }
}
