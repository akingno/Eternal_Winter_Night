package com.akingno.winternightak.block.datagen;

import com.akingno.winternightak.WinterNight;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = WinterNight.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        // 在这里添加你的 Provider
        // event.includeServer() 表示这些数据属于服务端数据（WorldGen属于服务端数据）
        generator.addProvider(event.includeServer(), new ModWorldGenProvider(packOutput, lookupProvider));

        // 如果你以后要生成方块模型、语言文件等，也都是加在这里
        // generator.addProvider(event.includeClient(), new ModBlockStateProvider(...));
    }
}
