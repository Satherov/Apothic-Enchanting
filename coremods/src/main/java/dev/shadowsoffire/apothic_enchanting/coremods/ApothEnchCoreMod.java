package dev.shadowsoffire.apothic_enchanting.coremods;

import net.neoforged.neoforgespi.transformation.ClassProcessorProvider;

/**
 * Entry point for Apothic Enchanting's bytecode transformers, discovered via the
 * {@code META-INF/services/net.neoforged.neoforgespi.transformation.ClassProcessorProvider}
 * service file shipped alongside this class. Replaces the pre-26.1 JavaScript coremods.
 *
 * <p>Note: this class lives in the Plugin layer and must not reference any Minecraft or mod code
 * at compile time. The injected bytecode points at {@code dev.shadowsoffire.apothic_enchanting.asm.EnchHooks},
 * which exists in the main mod jar (Game layer) — the reference is resolved at runtime by the target class's
 * classloader, not this one.
 */
public class ApothEnchCoreMod implements ClassProcessorProvider {

    @Override
    public void createProcessors(Context context, Collector collector) {
        collector.add(new EnchInfoRedirector());
        collector.add(new EnchInfoLootRedirector());
        collector.add(new FishingHookLureTransformer());
    }
}
