package com.akingno.winternightak.item;

import com.akingno.winternightak.WinterNight;
import com.momosoftworks.coldsweat.core.init.ItemInit;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;

/** Preserve fur already stored in earlier test worlds. */
@Mod.EventBusSubscriber(modid = WinterNight.MOD_ID)
public final class LegacyItemMappings {
    @SubscribeEvent
    public static void remapFur(MissingMappingsEvent event) {
        event.getMappings(ForgeRegistries.Keys.ITEMS, WinterNight.MOD_ID).forEach(mapping -> {
            if (mapping.getKey().getPath().equals("fur")) {
                mapping.remap(ItemInit.GOAT_FUR.get());
            }
        });
    }
}
