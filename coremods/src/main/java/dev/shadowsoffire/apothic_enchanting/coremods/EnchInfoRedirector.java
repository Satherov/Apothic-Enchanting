package dev.shadowsoffire.apothic_enchanting.coremods;

import java.util.Set;

import net.neoforged.neoforgespi.transformation.ProcessorName;
import net.neoforged.neoforgespi.transformation.SimpleClassProcessor;

/**
 * Replaces every virtual or bootstrap-handle call to {@code Enchantment#getMaxLevel()} in a
 * small set of classes with an {@code INVOKESTATIC} to
 * {@code dev.shadowsoffire.apothic_enchanting.asm.EnchHooks#getMaxLevel(Enchantment)}.
 *
 * <p>Ported from the pre-26.1 JS coremod {@code coremods/ench/ench_info_redirector.js}. The list
 * of target classes is the same — it includes vanilla classes that read the enchantment's max level
 * for display / interaction, plus third-party integrations (Quark, JEI, Goblin Traders, the
 * enchantment-level-cap-indicator mod) that Apothic wants to retroactively level-cap.
 */
public class EnchInfoRedirector extends SimpleClassProcessor {

    private static final String HOOK_NAME = "getMaxLevel";

    @Override
    public ProcessorName name() {
        return new ProcessorName("apothic_enchanting", "ench_info_redirector");
    }

    @Override
    public Set<Target> targets() {
        return Set.of(
            // Third-party integrations — left as-is; these are best-effort mod compat and do not
            // affect Apothic Enchanting's own correctness if the target mod isn't present.
            new Target("mezz.jei.library.plugins.vanilla.grindstone.GrindstoneRecipeMaker"),
            new Target("mezz.jei.library.plugins.vanilla.anvil.AnvilRecipeMaker$EnchantmentData"),
            new Target("org.violetmoon.quark.content.tools.module.AncientTomesModule"),
            new Target("org.violetmoon.quark.content.tools.item.AncientTomeItem"),
            new Target("com.mrcrayfish.goblintraders.Hooks"),
            new Target("com.natamus.enchantmentlevelcapindicator_common_neoforge.mixin.EnchantmentScreenMixin"),
            new Target("com.natamus.enchantmentlevelcapindicator_common_neoforge.mixin.ItemStackMixin"),

            // Vanilla — every surviving call site of Enchantment#getMaxLevel() outside the loot
            // system (which lives in EnchInfoLootRedirector). Verified against the 26.1 source.
            // Removed vs 1.21.1: EnchantedBookItem (class gone), VillagerTrades$EnchantBookForEmeralds
            // (villager trades are data-driven loot tables now, the inner class no longer exists).
            new Target("net.minecraft.server.commands.EnchantCommand"),
            new Target("net.minecraft.world.inventory.AnvilMenu"),
            new Target("net.minecraft.world.item.CreativeModeTabs"),
            new Target("net.minecraft.world.item.enchantment.Enchantment"));
    }

    @Override
    public void transform(org.objectweb.asm.tree.ClassNode input, net.neoforged.neoforgespi.transformation.SimpleTransformationContext context) {
        EnchantmentCallRewriter.rewriteGetMaxLevelCalls(input, HOOK_NAME);
    }
}
