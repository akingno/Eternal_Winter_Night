package com.akingno.winternightak.mixin;

import net.minecraft.world.entity.animal.Rabbit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Rabbit.class)
public abstract class RabbitMovementMixin {
    // Apply to both panic and avoidance goals, which otherwise restore vanilla flee speed.
    @ModifyConstant(method = "registerGoals", constant = @Constant(doubleValue = 2.2D))
    private double winterNight$moderatePanicSpeed(double vanillaSpeed) {
        return 0.5D;
    }
}
