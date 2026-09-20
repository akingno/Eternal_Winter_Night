package com.akingno.winternightak.client;

import com.akingno.winternightak.WinterNight;
import com.akingno.winternightak.client.particle.FallenLeafParticle;
import com.akingno.winternightak.client.particle.ModParticles;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = WinterNight.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
/** 只在客户端注册粒子工厂和物品外观条件，避免专用服务器加载渲染类。 */
public class ClientModEvents {
    @SubscribeEvent
    public static void registerLeafColors(net.minecraftforge.client.event.RegisterColorHandlersEvent.Block event) {
        // 原版云杉使用固定常绿叶色，不随群系变色；物品也使用同色。
        event.register((state, level, pos, tint) -> net.minecraft.world.level.FoliageColor.getEvergreenColor(),
                com.akingno.winternightak.block.ModBlocks.CHRISTMAS_LEAVES.get());
    }
    @SubscribeEvent
    public static void registerLeafItemColors(net.minecraftforge.client.event.RegisterColorHandlersEvent.Item event) {
        event.register((stack, tint) -> net.minecraft.world.level.FoliageColor.getEvergreenColor(),
                com.akingno.winternightak.block.ModBlocks.CHRISTMAS_LEAVES.get());
    }
    @SubscribeEvent
    public static void registerEntityRenderers(net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(com.akingno.winternightak.entity.ModEntities.STONE_JAVELIN.get(), StoneJavelinRenderer::new);
    }

    @SubscribeEvent
    // spent属性只决定燃尽火把的物品外观，不控制实际燃料或服务器点火行为。
    public static void onClientSetup(net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent event) {
        event.enqueueWork(() -> net.minecraft.client.gui.screens.MenuScreens.register(
                com.akingno.winternightak.crafting.PolarCrafting.MENU.get(), PolarWorkbenchScreen::new));
        // 原版磁石指针算法；未绑定或跨维度时按原版方式旋转。
        event.enqueueWork(() -> net.minecraft.client.renderer.item.ItemProperties.register(
                com.akingno.winternightak.item.ModItems.VILLAGE_COMPASS.get(),
                new net.minecraft.resources.ResourceLocation("angle"),
                new net.minecraft.client.renderer.item.CompassItemPropertyFunction((level, stack, entity) ->
                        stack.hasTag() ? net.minecraft.world.item.CompassItem.getLodestonePosition(stack.getTag()) : null)));
        event.enqueueWork(() -> net.minecraft.client.renderer.item.ItemProperties.register(
                com.akingno.winternightak.block.ModBlocks.POLAR_TORCH.get().asItem(),
                new net.minecraft.resources.ResourceLocation(WinterNight.MOD_ID, "spent"),
                (stack, level, entity, seed) -> {
                    var tag = stack.getTagElement("BlockEntityTag");
                    return tag == null || tag.getInt("FuelTicks") <= 0 ? 1 : 0;
                }));
    }

    @SubscribeEvent
    public static void onRegisterParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.BLIZZARD_SNOW.get(), com.akingno.winternightak.client.particle.BlizzardSnowParticle.Provider::new);
        // 1.20.1 不再使用 Minecraft.getInstance().particles
        // 而是直接使用 event.registerSpriteSet 或者 event.registerSpecial

        // 这里的 registerSpriteSet 适用于需要贴图的粒子（比如落叶）
        // 它会自动把贴图集合传给你的 Factory 构造函数
        event.registerSpriteSet(
                ModParticles.SAKURA_LEAF.get(),
                FallenLeafParticle.Factory::new
        );
    }
}

