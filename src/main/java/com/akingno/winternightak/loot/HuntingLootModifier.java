package com.akingno.winternightak.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/** 先删JSON列出的原版掉落，再抽取追加掉落表；没有列出的兔脚、墨囊等不会被误删。 */
public class HuntingLootModifier extends LootModifier {
    public static final Codec<HuntingLootModifier> CODEC = RecordCodecBuilder.create(instance ->
            codecStart(instance).and(instance.group(
                    ResourceLocation.CODEC.fieldOf("table").forGetter(modifier -> modifier.table),
                    ForgeRegistries.ITEMS.getCodec().listOf().fieldOf("remove_items")
                            .forGetter(modifier -> modifier.removeItems)
            )).apply(instance, HuntingLootModifier::new));

    private final ResourceLocation table;
    private final List<Item> removeItems;

    public HuntingLootModifier(LootItemCondition[] conditions, ResourceLocation table, List<Item> removeItems) {
        super(conditions);
        this.table = table;
        this.removeItems = List.copyOf(removeItems);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        generatedLoot.removeIf(stack -> removeItems.contains(stack.getItem()));
        // 使用Raw入口抽追加表，避免同一个上下文再次触发全局修饰器造成递归。
        context.getLevel().getServer().getLootData().getLootTable(table)
                .getRandomItemsRaw(context, generatedLoot::add);
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
