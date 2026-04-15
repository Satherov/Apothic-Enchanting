package dev.shadowsoffire.apothic_enchanting.objects;

import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * BlockItem for the enchantment library blocks that surfaces capacity + stored point count in
 * the item tooltip.
 */
public class LibraryBlockItem extends BlockItem {

    private final int maxLevel;

    public LibraryBlockItem(Block block, Item.Properties properties, int maxLevel) {
        super(block, properties);
        this.maxLevel = maxLevel;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("tooltip.enchlib.capacity", Component.translatable("enchantment.level." + this.maxLevel)).withStyle(ChatFormatting.GOLD));
        TypedEntityData<BlockEntityType<?>> data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (data != null && data.contains("points")) {
            int points = data.copyTagWithoutId().getCompoundOrEmpty("points").size();
            if (points > 0) {
                tooltip.accept(Component.translatable("tooltip.enchlib.item", points).withStyle(ChatFormatting.GRAY));
            }
        }
    }
}
