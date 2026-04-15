package dev.shadowsoffire.apothic_enchanting.table;

import javax.annotation.Nullable;

import dev.shadowsoffire.apothic_enchanting.api.EnchantmentStatBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class ApothEnchantingTableBlock extends EnchantingTableBlock {

    public ApothEnchantingTableBlock(Block.Properties props) {
        super(props);
    }

    @Override
    @Nullable
    public MenuProvider getMenuProvider(BlockState state, Level world, BlockPos pos) {
        BlockEntity tileentity = world.getBlockEntity(pos);
        if (tileentity instanceof EnchantingTableBlockEntity) {
            Component itextcomponent = ((Nameable) tileentity).getDisplayName();
            return new SimpleMenuProvider((id, inventory, player) -> new ApothEnchantmentMenu(id, inventory, ContainerLevelAccess.create(world, pos), tileentity.getData(EnchantmentTableItemHandler.TYPE)), itextcomponent);
        }
        else {
            return null;
        }
    }

    /**
     * Normally we'd do this in {@link BlockEntity#preRemoveSideEffects(BlockPos, BlockState)}, but since
     * we don't own the block entity and instead use an attachment, we can't.
     */
    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        if (level.getBlockEntity(pos) instanceof EnchantingTableBlockEntity be) {
            EnchantmentTableItemHandler handler = be.getData(EnchantmentTableItemHandler.TYPE);
            ItemResource fuel = handler.getResource(0);
            int amount = handler.getAmountAsInt(0);
            if (!fuel.isEmpty() && amount > 0) {
                Block.popResource(level, pos, fuel.toStack(amount));
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
        for (BlockPos offset : BOOKSHELF_OFFSETS) {
            BlockState shelfState = level.getBlockState(pos.offset(offset));
            ((EnchantmentStatBlock) shelfState.getBlock()).spawnTableParticle(shelfState, level, rand, pos, offset);
        }
    }

    public static ResourceHandler<ItemResource> getItemHandler(EnchantingTableBlockEntity be, Direction dir) {
        return be.getData(EnchantmentTableItemHandler.TYPE);
    }

}
