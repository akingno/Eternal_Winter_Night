package com.akingno.winternightak.mixin;

import com.akingno.winternightak.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// 修正兔子对极地地面的寻路评分：0为中性可用，不再因为缺少草地而排斥目的地。
@Mixin(Animal.class)
public abstract class PolarAnimalGroundMixin {
    @Inject(method = "getWalkTargetValue", at = @At("HEAD"), cancellable = true)
    private void winterNight$acceptPolarGround(BlockPos pos, LevelReader level,
                                                CallbackInfoReturnable<Float> cir) {
        if (!((Object) this instanceof Rabbit)) return;
        var ground = level.getBlockState(pos.below());
        // 薄雪下再看一层地面，因此below(2)是跳过雪层，不是额外增加寻路高度。
        if (ground.is(Blocks.SNOW)) ground = level.getBlockState(pos.below(2));
        if (ground.is(ModBlocks.HARD_ICE.get()) || ground.is(ModBlocks.FROZEN_SOIL.get()))
            cir.setReturnValue(0.0F);
    }
}
