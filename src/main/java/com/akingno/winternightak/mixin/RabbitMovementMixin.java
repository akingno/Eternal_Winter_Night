package com.akingno.winternightak.mixin;

import net.minecraft.world.entity.animal.Rabbit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Rabbit.class)
public abstract class RabbitMovementMixin {
    // Apply to both panic and avoidance goals, which otherwise restore vanilla flee speed.
    // 原版2.2是逃跑/避敌目标使用的速度倍率；同时修改两者，防止另一目标恢复过快速度。
    @ModifyConstant(method = "registerGoals", constant = @Constant(doubleValue = 2.2D))
    private double winterNight$moderatePanicSpeed(double vanillaSpeed) {
        // 0.5调高兔子逃跑更快，调低更慢；当前作用于原版兔子，不只限极地维度。
        return 0.5D;
    }
}
