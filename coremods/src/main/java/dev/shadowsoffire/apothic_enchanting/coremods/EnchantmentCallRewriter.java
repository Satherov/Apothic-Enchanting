package dev.shadowsoffire.apothic_enchanting.coremods;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Shared bytecode rewrite used by the two "ench info" redirectors. Replaces every
 * {@code INVOKEVIRTUAL Enchantment#getMaxLevel()I} with an
 * {@code INVOKESTATIC EnchHooks#<hookName>(Enchantment)I}, and also rewrites any
 * {@code INVOKEDYNAMIC} bootstrap handle that references {@code Enchantment#getMaxLevel}
 * (lambdas / method references that wrap the same method).
 */
final class EnchantmentCallRewriter {

    static final String HOOK_OWNER = "dev/shadowsoffire/apothic_enchanting/asm/EnchHooks";
    static final String HOOK_DESCRIPTOR = "(Lnet/minecraft/world/item/enchantment/Enchantment;)I";
    static final String TARGET_OWNER = "net/minecraft/world/item/enchantment/Enchantment";
    static final String TARGET_METHOD_NAME = "getMaxLevel";

    private EnchantmentCallRewriter() {}

    /**
     * Rewrites all matching calls in {@code classNode}. Returns the number of rewrites performed
     * so the caller can short-circuit if none were made (lets the framework skip the class rewrite
     * for classes that happen to be listed as targets but don't actually call the target method).
     */
    static int rewriteGetMaxLevelCalls(ClassNode classNode, String hookName) {
        int count = 0;
        for (MethodNode method : classNode.methods) {
            var instructions = method.instructions;
            for (int i = 0; i < instructions.size(); i++) {
                AbstractInsnNode insn = instructions.get(i);
                if (insn.getOpcode() == Opcodes.INVOKEVIRTUAL && insn instanceof MethodInsnNode m) {
                    if (TARGET_OWNER.equals(m.owner) && TARGET_METHOD_NAME.equals(m.name)) {
                        instructions.set(m, new MethodInsnNode(Opcodes.INVOKESTATIC, HOOK_OWNER, hookName, HOOK_DESCRIPTOR, false));
                        count++;
                    }
                }
                else if (insn instanceof InvokeDynamicInsnNode indy) {
                    Object[] bsmArgs = indy.bsmArgs;
                    for (int k = 0; k < bsmArgs.length; k++) {
                        if (bsmArgs[k] instanceof Handle handle
                            && TARGET_OWNER.equals(handle.getOwner())
                            && TARGET_METHOD_NAME.equals(handle.getName())) {
                            bsmArgs[k] = new Handle(Opcodes.H_INVOKESTATIC, HOOK_OWNER, hookName, HOOK_DESCRIPTOR, false);
                            count++;
                        }
                    }
                }
            }
        }
        return count;
    }
}
