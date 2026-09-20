package com.akingno.winternightak.item;

import com.akingno.winternightak.WinterNight;
import com.akingno.winternightak.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

// 创造栏仅提供调试入口，不代表这些物品在生存中有合成配方。
public class ModCreativeModTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, WinterNight.MOD_ID);

    public static final RegistryObject<CreativeModeTab> WINTER_NIGHT_TAB = CREATIVE_MODE_TABS.register("winter_night_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.ICE_PICK.get()))
                    .title(Component.translatable("creativetab.winternightak.winter_night_tab"))
                    .displayItems((params, output) -> {
                        output.accept(ModBlocks.PERMAFROST.get());
                        output.accept(ModBlocks.FROZEN_SOIL.get());
                        output.accept(ModBlocks.HARD_ICE.get());
                        output.accept(ModBlocks.ICE_SLAB.get());
                        output.accept(ModBlocks.PRIMITIVE_LAMP.get());
                        output.accept(ModBlocks.CAMPFIRE.get());
                        output.accept(ModBlocks.POLAR_TORCH.get());
                        output.accept(ModItems.FUR_WRAP.get());
                        output.accept(ModItems.VILLAGE_COMPASS.get());
                        output.accept(ModItems.STONE_JAVELIN.get());
                        output.accept(ModItems.METAL_PART.get());
                        output.accept(ModBlocks.CHRISTMAS_TREE_SAPLING.get());
                        output.accept(ModBlocks.CHRISTMAS_LEAVES.get());
                        output.accept(ModBlocks.POLAR_WORKBENCH.get());
                        output.accept(ModBlocks.HEATER.get());
                        output.accept(ModBlocks.SUN_LAMP.get());
                        output.accept(ModBlocks.THAWED_SOIL.get());
                        output.accept(ModBlocks.THAWED_FARMLAND.get());
                        output.accept(ModBlocks.THAWED_PATH.get());
                        output.accept(com.momosoftworks.coldsweat.core.init.ItemInit.GOAT_FUR.get());
                        output.accept(com.momosoftworks.coldsweat.core.init.ItemInit.GOAT_FUR_HELMET.get());
                        output.accept(com.momosoftworks.coldsweat.core.init.ItemInit.GOAT_FUR_CHESTPLATE.get());
                        output.accept(com.momosoftworks.coldsweat.core.init.ItemInit.GOAT_FUR_LEGGINGS.get());
                        output.accept(com.momosoftworks.coldsweat.core.init.ItemInit.GOAT_FUR_BOOTS.get());
                        output.accept(ModItems.ANIMAL_FAT.get());
                        output.accept(Items.LEATHER);
                        output.accept(ModItems.RAW_GAME_MEAT.get());
                        output.accept(ModItems.ICE_BRICK.get());
                        output.accept(ModItems.ICE_PICK.get());
                    }).build());



    public static final RegistryObject<CreativeModeTab> MESHI_TAB = CREATIVE_MODE_TABS.register("meshi_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(Items.BAMBOO))
                    .title(Component.translatable("creativetab.meshi_tab"))
                    .displayItems((params, output) -> {

                        // --- Vanilla helper ---
                        output.accept(Items.BAMBOO);

                        // --- Items / Foods ---
                        output.accept(ModItems.BAMBOO_BUCKET.get());
                        output.accept(ModItems.BAMBOO_SHOOT.get());
                        output.accept(ModItems.TUDURA.get());

                        output.accept(ModItems.MESHI.get());
                        output.accept(ModItems.ICEGLASS.get());

                        output.accept(ModItems.GYUMESHI.get());
                        output.accept(ModItems.KATSUMESHI.get());
                        output.accept(ModItems.KINOKOMESHI.get());
                        output.accept(ModItems.EGGMESHI.get());
                        output.accept(ModItems.OYAKODON.get());
                        output.accept(ModItems.TEKKA.get());
                        output.accept(ModItems.TAKEDON.get());

                        // --- Tools / Weapons ---
                        output.accept(ModItems.KATANA.get());

                        // --- Building blocks: stone/plaster ---
                        output.accept(ModBlocks.PLASTER.get());
                        output.accept(ModBlocks.PLASTER_STAIRS.get());
                        output.accept(ModBlocks.PLASTER_SLAB.get());

                        output.accept(ModBlocks.KAWARA.get());
                        output.accept(ModBlocks.KAWARA_STAIRS.get());
                        output.accept(ModBlocks.KAWARA_SLAB.get());

                        output.accept(ModBlocks.NAMAKO.get());
                        output.accept(ModBlocks.NAMAKO_STAIRS.get());
                        output.accept(ModBlocks.NAMAKO_SLAB.get());

                        // --- Sakura wood set ---
                        output.accept(ModBlocks.SAKURA_LOG.get());
                        output.accept(ModBlocks.SAKURA_LOG_STAIRS.get());
                        output.accept(ModBlocks.SAKURA_LOG_SLAB.get());

                        output.accept(ModBlocks.SAKURA_PLANKS.get());
                        output.accept(ModBlocks.SAKURA_STAIRS.get());
                        output.accept(ModBlocks.SAKURA_SLAB.get());

                        output.accept(ModBlocks.SAKURA_LEAVES.get());
                        output.accept(ModBlocks.SAKURA_SAPLING.get());

                        // --- Japanese-style interior blocks ---
                        output.accept(ModBlocks.TATAMI.get());
                        output.accept(ModBlocks.TATAMI_NB.get());

                        output.accept(ModBlocks.ANDON.get());
                        output.accept(ModBlocks.VINI_BOOK.get());

                        output.accept(ModBlocks.GLASS_DOOR.get());
                        output.accept(ModBlocks.HUSUMA.get());

                        output.accept(ModBlocks.FUTON.get());
                    })
                    .build()
    );


    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
