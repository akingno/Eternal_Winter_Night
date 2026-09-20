package com.akingno.winternightak.block.entity;

import com.akingno.winternightak.WinterNight;
import com.akingno.winternightak.block.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** 将三种设备的方块实体类型与对应方块绑定；读档、tick分发和渲染都依靠此注册。 */
public final class ModBlockEntities {
    // 方块实体类型决定NBT读档、服务端tick和客户端渲染绑定；新增设备时必须同时登记对应方块。
    public static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, WinterNight.MOD_ID);
    public static final RegistryObject<BlockEntityType<PolarTorchBlockEntity>> POLAR_TORCH = TYPES.register("polar_torch",
            () -> BlockEntityType.Builder.of(PolarTorchBlockEntity::new, ModBlocks.POLAR_TORCH.get(), ModBlocks.POLAR_WALL_TORCH.get()).build(null));
    public static final RegistryObject<BlockEntityType<PolarCampfireBlockEntity>> CAMPFIRE = TYPES.register("campfire",
            () -> BlockEntityType.Builder.of(PolarCampfireBlockEntity::new, ModBlocks.CAMPFIRE.get()).build(null));
    public static final RegistryObject<BlockEntityType<PrimitiveLampBlockEntity>> PRIMITIVE_LAMP = TYPES.register("primitive_lamp",
            () -> BlockEntityType.Builder.of(PrimitiveLampBlockEntity::new, ModBlocks.PRIMITIVE_LAMP.get()).build(null));
}
