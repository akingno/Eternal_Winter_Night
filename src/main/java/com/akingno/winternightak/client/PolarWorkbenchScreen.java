package com.akingno.winternightak.client;
import com.akingno.winternightak.crafting.PolarWorkbenchMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** 使用复制到本模组的原版工作台背景；布局与服务端槽位坐标一致。 */
public final class PolarWorkbenchScreen extends AbstractContainerScreen<PolarWorkbenchMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("winternightak","textures/gui/polar_workbench.png");
    public PolarWorkbenchScreen(PolarWorkbenchMenu menu,Inventory inventory,Component title) { super(menu,inventory,title); }
    @Override protected void renderBg(GuiGraphics graphics,float partial,int x,int y) {
        graphics.blit(TEXTURE,leftPos,topPos,0,0,imageWidth,imageHeight);
    }
    @Override public void render(GuiGraphics graphics,int x,int y,float partial) {
        renderBackground(graphics);
        super.render(graphics,x,y,partial);
        renderTooltip(graphics,x,y);
    }
}
