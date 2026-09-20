package com.akingno.winternightak.block.custom;
import com.akingno.winternightak.crafting.PolarWorkbenchMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** 没有库存方块实体；合成材料只属于本次打开的菜单，关闭时返还。 */
public final class PolarWorkbenchBlock extends Block {
    public PolarWorkbenchBlock(Properties properties) { super(properties); }
    @Override public InteractionResult use(BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit) {
        if(!level.isClientSide) player.openMenu(new SimpleMenuProvider(
                (id,inventory,p)->new PolarWorkbenchMenu(id,inventory,ContainerLevelAccess.create(level,pos)),
                Component.translatable("block.winternightak.polar_workbench")));
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
