package com.akingno.winternightak.block;

import com.akingno.winternightak.WinterNight;
import com.akingno.winternightak.block.custom.*;
import com.akingno.winternightak.client.particle.ModParticles;
import com.akingno.winternightak.item.ModItems;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, WinterNight.MOD_ID);

    public static final RegistryObject<Block> PERMAFROST = registerBlock("permafrost",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.BEDROCK)
                    .mapColor(MapColor.DIRT).sound(SoundType.GRAVEL).noLootTable()));
    public static final RegistryObject<Block> FROZEN_SOIL = registerBlock("frozen_soil",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.DIRT).strength(1.5F)));
    public static final RegistryObject<Block> HARD_ICE = registerBlock("hard_ice",
            () -> new HardIceBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .mapColor(MapColor.ICE).sound(SoundType.GLASS).noOcclusion()));
    public static final RegistryObject<Block> ICE_SLAB = registerBlock("ice_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.of().mapColor(MapColor.ICE)
                    .strength(2.0F, 6.0F).sound(SoundType.GLASS).noOcclusion()));


    //灰泥
    public static final RegistryObject<Block> PLASTER = registerBlock("plaster",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .strength(2.0F, 6.0F)));
    public static final RegistryObject<Block> PLASTER_STAIRS = registerBlock("plaster_stairs",
            () -> new StairBlock(()->ModBlocks.PLASTER.get().defaultBlockState(),
                    BlockBehaviour.Properties.copy(ModBlocks.PLASTER.get())));
    public static final RegistryObject<Block> PLASTER_SLAB = registerBlock("plaster_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.copy(ModBlocks.PLASTER.get())));

    //瓦
    public static final RegistryObject<Block> KAWARA = registerBlock("kawara",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .strength(2.0F, 6.0F)));
    public static final RegistryObject<Block> KAWARA_STAIRS = registerBlock("kawara_stairs",
            () -> new StairBlock(()->ModBlocks.KAWARA.get().defaultBlockState(),
                    BlockBehaviour.Properties.copy(ModBlocks.KAWARA.get())));
    public static final RegistryObject<Block> KAWARA_SLAB = registerBlock("kawara_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.copy(ModBlocks.KAWARA.get())));
    
    //菱墙
    public static final RegistryObject<Block> NAMAKO = registerBlock("namako",
            () -> new Block(BlockBehaviour.Properties.copy(ModBlocks.KAWARA.get())));
    public static final RegistryObject<Block> NAMAKO_STAIRS = registerBlock("namako_stairs",
            () -> new StairBlock(()->ModBlocks.NAMAKO.get().defaultBlockState(),
                    BlockBehaviour.Properties.copy(ModBlocks.NAMAKO.get())));
    public static final RegistryObject<Block> NAMAKO_SLAB = registerBlock("namako_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.copy(ModBlocks.KAWARA.get())));



    //樱花木板
    public static final RegistryObject<Block> SAKURA_PLANKS = registerBlock("sakura_planks",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS)));
    public static final RegistryObject<Block> SAKURA_STAIRS = registerBlock("sakura_stairs",
            () -> new StairBlock(()->ModBlocks.NAMAKO.get().defaultBlockState(),
                    BlockBehaviour.Properties.copy(ModBlocks.SAKURA_PLANKS.get())));
    public static final RegistryObject<Block> SAKURA_SLAB = registerBlock("sakura_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.copy(ModBlocks.SAKURA_PLANKS.get())));

    //樱原木
    public static final RegistryObject<RotatedPillarBlock> SAKURA_LOG = registerBlock("sakura_log",
            () -> copyLog(MapColor.WOOD, MapColor.PODZOL));
    public static final RegistryObject<Block> SAKURA_LOG_STAIRS = registerBlock("sakura_log_stairs",
            () -> new StairBlock(()->ModBlocks.SAKURA_LOG.get().defaultBlockState(),
                    BlockBehaviour.Properties.copy(Blocks.OAK_STAIRS)));
    public static final RegistryObject<Block> SAKURA_LOG_SLAB = registerBlock("sakura_log_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.OAK_SLAB)));


    // 疂
    public static final RegistryObject<Block> TATAMI = registerBlock("tatami",
            () -> new TatamiBlock(BlockBehaviour.Properties.copy(Blocks.HAY_BLOCK)));
    // 无缘畳
    public static final RegistryObject<Block> TATAMI_NB = registerBlock("tatami_nb",
            () -> new TatamiBlock(BlockBehaviour.Properties.copy(Blocks.HAY_BLOCK)));

    // 樱花叶
    public static final RegistryObject<Block> SAKURA_LEAVES = registerBlock("sakura_leaves",
            () -> new SakuraLeavesBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LEAVES).strength(0.2f)
                    .randomTicks().sound(SoundType.GRASS).noOcclusion().lightLevel((x)-> 10), ModParticles.SAKURA_LEAF));

    // 樱花树苗
    public static final RegistryObject<Block> SAKURA_SAPLING = registerBlock("sakura_sapling",
            () -> new SaplingBlock(new SakuraTreeGrower(),
                    BlockBehaviour.Properties.copy(Blocks.OAK_SAPLING)));

    // 玻璃门
    public static final RegistryObject<Block> GLASS_DOOR = registerBlock("glass_door",
            () -> new DoorBlock(BlockBehaviour.Properties.copy(Blocks.OAK_DOOR)
                    .strength(3.0F).sound(SoundType.WOOD).noOcclusion(), BlockSetType.OAK));

    // 隔扇
    public static final RegistryObject<Block> HUSUMA = registerBlock("husuma",
            () -> new DoorBlock(BlockBehaviour.Properties.copy(Blocks.BAMBOO)
                    .strength(2.0F).sound(SoundType.BAMBOO).noOcclusion(), BlockSetType.OAK));

    // 青行灯
    public static final RegistryObject<Block> ANDON = registerBlock("andon",
            () -> new AndonBlock(BlockBehaviour.Properties.copy(Blocks.BAMBOO).strength(1.0F).sound(SoundType.BAMBOO)
                    .noOcclusion().noCollission().lightLevel((x)->15)));

    // VINI书
    public static final RegistryObject<Block> VINI_BOOK = registerBlock("vini_book",
            () -> new BookBlock(BlockBehaviour.Properties.copy(Blocks.BOOKSHELF).strength(1.0F).sound(SoundType.SNOW)
                    .noOcclusion().lightLevel((x)->15)));

    //水车
    /*public static final RegistryObject<Block> WATER_WHEEL = registerBlock("waterwheel",
            () -> new WaterWheelBlock(BlockBehaviour.Properties.copy(Material.WOOD).strength(2.0F).sound(SoundType.WOOD)
                    .notSolid().doesNotBlockMovement()));*/

    //futon
    public static final RegistryObject<Block> FUTON = registerBlock("futon",
            () -> new FutonBlock(DyeColor.BLUE,BlockBehaviour.Properties.copy(Blocks.BLUE_WOOL)
                    .strength(0.1F).sound(SoundType.WOOL).noOcclusion()));





    private static RotatedPillarBlock copyLog(MapColor top, MapColor bark) {
        return new RotatedPillarBlock(BlockBehaviour.Properties
                .copy(Blocks.OAK_LOG).mapColor((state) -> { return state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? top : bark; })
                .strength(2.0F).sound(SoundType.WOOD));
    }
    
    
    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
