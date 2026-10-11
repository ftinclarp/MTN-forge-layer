package mtn.forge_layer.registry;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Client-only screen for the layered container menu. Draws the vanilla chest
 * GUI background ({@code generic_54.png}) so slot frames and item icons render;
 * the parent {@link AbstractContainerScreen} draws the slots from the
 * screen-handler's slot list.
 */
public class FabricHandledScreen
        extends AbstractContainerScreen<FabricScreenHandlerAdapter> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/container/generic_54.png");

    public FabricHandledScreen(FabricScreenHandlerAdapter menu, Inventory inventory,
            Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX,
            int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        // Blit the full 176x166 chest GUI from the left edge of generic_54.png
        // (the texture is 256x256; the chest layout lives in its top-left
        // 176x166 region): 3 chest slot rows plus player inv/hotbar + frames.
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight,
                256, 256);
    }
}
