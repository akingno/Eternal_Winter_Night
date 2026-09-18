package com.akingno.winternightak.mixin;

import net.minecraft.world.entity.animal.Rabbit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Rabbit.class)
public abstract class RabbitMovementMixin {
    @ModifyConstant(method = "registerGoals", constant = @Constant(doubleValue = 2.2D, ordinal = 0))
    private double winterNight$moderatePanicSpeed(double vanillaSpeed) {
        return 1.3D;
    }
}
