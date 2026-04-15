package dev.shadowsoffire.apothic_enchanting.coremods;

import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.neoforged.neoforgespi.transformation.ProcessorName;
import net.neoforged.neoforgespi.transformation.SimpleMethodProcessor;
import net.neoforged.neoforgespi.transformation.SimpleTransformationContext;

/**
 * Rewrites the tail of {@code FishingHook#catchingFish(BlockPos)} so that the delay value
 * assigned to {@code timeUntilHooked} is routed through
 * {@code dev.shadowsoffire.apothic_enchanting.asm.EnchHooks#getTicksCaughtDelay(FishingHook)}
 * instead of the vanilla formula. Ported from the pre-26.1 JS coremod
 * {@code coremods/ench/fishing_hook.js}.
 *
 * <p>Pattern: vanilla's method body ends with a {@code PUTFIELD timeUntilHooked} that writes a
 * locally-computed {@code int} onto {@code this}. We find the <em>last</em> {@code PUTFIELD} in
 * the method, pop the pre-computed value off the stack, push {@code this}, then call our hook and
 * let the resulting {@code int} be consumed by the original {@code PUTFIELD}. The vanilla
 * computation is discarded in favour of our hook.
 */
public class FishingHookLureTransformer extends SimpleMethodProcessor {

    private static final String HOOK_OWNER = "dev/shadowsoffire/apothic_enchanting/asm/EnchHooks";
    private static final String HOOK_NAME = "getTicksCaughtDelay";
    private static final String HOOK_DESCRIPTOR = "(Lnet/minecraft/world/entity/projectile/FishingHook;)I";

    @Override
    public ProcessorName name() {
        return new ProcessorName("apothic_enchanting", "fishing_hook_lure");
    }

    @Override
    public Set<Target> targets() {
        return Set.of(new Target(
            "net.minecraft.world.entity.projectile.FishingHook",
            "catchingFish",
            "(Lnet/minecraft/core/BlockPos;)V"));
    }

    @Override
    public void transform(MethodNode input, SimpleTransformationContext context) {
        InsnList instructions = input.instructions;
        AbstractInsnNode lastPutField = null;
        for (int i = instructions.size() - 1; i >= 0; i--) {
            AbstractInsnNode n = instructions.get(i);
            if (n.getOpcode() == Opcodes.PUTFIELD) {
                lastPutField = n;
                break;
            }
        }
        if (lastPutField == null) {
            return;
        }

        InsnList patch = new InsnList();
        patch.add(new InsnNode(Opcodes.POP));                          // discard vanilla's computed delay
        patch.add(new VarInsnNode(Opcodes.ALOAD, 0));                  // push `this` (FishingHook)
        patch.add(new MethodInsnNode(
            Opcodes.INVOKESTATIC,
            HOOK_OWNER,
            HOOK_NAME,
            HOOK_DESCRIPTOR,
            false));
        instructions.insertBefore(lastPutField, patch);
    }
}
