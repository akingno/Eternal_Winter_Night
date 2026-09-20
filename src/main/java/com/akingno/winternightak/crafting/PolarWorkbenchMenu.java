package com.akingno.winternightak.crafting;

import com.akingno.winternightak.block.ModBlocks;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

/** 原版3×3布局、ResultSlot扣料/容器返还、关闭返还材料；专属网格限制高级配方。 */
public final class PolarWorkbenchMenu extends AbstractContainerMenu {
    private final PolarCrafting.Grid grid = new PolarCrafting.Grid(this);
    private final ResultContainer result = new ResultContainer();
    private final ContainerLevelAccess access;
    private final Player player;
    public PolarWorkbenchMenu(int id, Inventory inventory) { this(id,inventory,ContainerLevelAccess.NULL); }
    public PolarWorkbenchMenu(int id, Inventory inventory, ContainerLevelAccess access) {
        super(PolarCrafting.MENU.get(),id);
        this.access=access;
        this.player=inventory.player;
        // 槽0为结果，1..9为材料，10..36为背包，37..45为快捷栏；坐标沿用原版合成界面。
        addSlot(new ResultSlot(player,grid,result,0,124,35));
        for(int y=0;y<3;y++) for(int x=0;x<3;x++) addSlot(new Slot(grid,x+y*3,30+x*18,17+y*18));
        for(int y=0;y<3;y++) for(int x=0;x<9;x++) addSlot(new Slot(inventory,x+y*9+9,8+x*18,84+y*18));
        for(int x=0;x<9;x++) addSlot(new Slot(inventory,x,8+x*18,142));
    }
    @Override public void slotsChanged(Container container) {
        access.execute((level,pos)->{
            if (!(player instanceof ServerPlayer serverPlayer)) return;
            ItemStack output=ItemStack.EMPTY;
            var recipe=level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING,grid,level);
            if(recipe.isPresent() && result.setRecipeUsed(level,serverPlayer,recipe.get()))
                output=recipe.get().assemble(grid,level.registryAccess());
            result.setItem(0,output);
            setRemoteSlot(0,output);
            serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(containerId,incrementStateId(),0,output));
        });
    }
    @Override public boolean stillValid(Player player) { return stillValid(access,player,ModBlocks.POLAR_WORKBENCH.get()); }
    @Override public void removed(Player player) {
        super.removed(player);
        access.execute((level,pos)->clearContainer(player,grid));
    }
    @Override public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != result && super.canTakeItemForPickAll(stack,slot);
    }
    @Override public ItemStack quickMoveStack(Player player,int index) {
        Slot slot=slots.get(index);
        if(!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack=slot.getItem(), original=stack.copy();
        if(index==0) {
            access.execute((level,pos)->stack.getItem().onCraftedBy(stack,level,player));
            if(!moveItemStackTo(stack,10,46,true)) return ItemStack.EMPTY;
            slot.onQuickCraft(stack,original);
        } else if(index<10) {
            if(!moveItemStackTo(stack,10,46,false)) return ItemStack.EMPTY;
        } else if(!moveItemStackTo(stack,1,10,false)) {
            if(index<37 ? !moveItemStackTo(stack,37,46,false) : !moveItemStackTo(stack,10,37,false)) return ItemStack.EMPTY;
        }
        if(stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        if(stack.getCount()==original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player,stack);
        if(index==0) player.drop(stack,false);
        return original;
    }
}
