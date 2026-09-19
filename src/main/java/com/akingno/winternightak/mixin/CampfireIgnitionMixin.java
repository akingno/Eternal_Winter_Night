package com.akingno.winternightak.mixin;

import com.akingno.winternightak.block.custom.PolarCampfireBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// 拦截原版通用点火判定，只限制本模组营火；打火石交互由方块自身执行带燃料检查的点火。
@Mixin(CampfireBlock.class)
public abstract class CampfireIgnitionMixin {
    @Inject(method = "canLight", at = @At("HEAD"), cancellable = true)
    private static void winterNight$useFuelAwareIgnition(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        // Prevent vanilla item/dispensing fallbacks bypassing our block's fuel check.
        if (state.getBlock() instanceof PolarCampfireBlock) cir.setReturnValue(false);
    }
}
