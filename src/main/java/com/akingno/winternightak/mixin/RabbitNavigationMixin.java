package com.akingno.winternightak.mixin;

import com.akingno.winternightak.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PathNavigation.class)
public abstract class RabbitNavigationMixin {
    @Shadow @Final protected Mob mob;

    @Inject(method = "isStableDestination", at = @At("HEAD"), cancellable = true)
    private void winterNight$acceptHardIce(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!(mob instanceof Rabbit)) return;
        var level = mob.level();
        var ground = level.getBlockState(pos.below());
        if (ground.is(Blocks.SNOW)) ground = level.getBlockState(pos.below(2));
        // Hard ice has full collision but noOcclusion, so vanilla's isSolidRender rejects it.
        // This only accepts a destination; normal collision/path checks still apply.
        if (ground.is(ModBlocks.HARD_ICE.get()) || ground.is(ModBlocks.FROZEN_SOIL.get()))
            cir.setReturnValue(true);
    }
}
