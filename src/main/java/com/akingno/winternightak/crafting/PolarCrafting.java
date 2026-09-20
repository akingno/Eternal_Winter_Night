package com.akingno.winternightak.crafting;

import com.akingno.winternightak.WinterNight;
import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.*;

/** 沿用原版有序配方序列化；仅在极地工作台的专属合成网格中匹配。 */
public final class PolarCrafting {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, WinterNight.MOD_ID);
    public static final RegistryObject<MenuType<PolarWorkbenchMenu>> MENU = MENUS.register("polar_workbench",
            () -> new MenuType<>(PolarWorkbenchMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, WinterNight.MOD_ID);
    public static final RegistryObject<RecipeSerializer<ShapedRecipe>> SERIALIZER = SERIALIZERS.register("polar_shaped", Serializer::new);

    public static final class Grid extends TransientCraftingContainer {
        public Grid(AbstractContainerMenu menu) { super(menu,3,3); }
    }
    private static final class PolarRecipe extends ShapedRecipe {
        PolarRecipe(ShapedRecipe base) {
            super(base.getId(),base.getGroup(),base.category(),base.getWidth(),base.getHeight(),
                    base.getIngredients(),base.getResultItem(RegistryAccess.EMPTY),false);
        }
        @Override public boolean matches(CraftingContainer grid, Level level) {
            return grid instanceof Grid && super.matches(grid,level);
        }
        @Override public RecipeSerializer<?> getSerializer() { return SERIALIZER.get(); }
        // 不进入普通工作台配方书自动填充；手动3×3放料。
        @Override public boolean isSpecial() { return true; }
    }
    public static final class Serializer extends ShapedRecipe.Serializer {
        @Override public ShapedRecipe fromJson(ResourceLocation id, JsonObject json) {
            return new PolarRecipe(super.fromJson(id,json));
        }
        @Override public ShapedRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            return new PolarRecipe(super.fromNetwork(id,buffer));
        }
    }
}
