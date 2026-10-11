package mtn.forge_layer.registry;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Client-only screen for the layered container menu. Renders nothing special:
 * {@code renderBg} is deliberately empty — placeholder backgrounds, slot icons
 * and textures are R3c. The screen opens via {@code Minecraft.setScreen(...)}.
 */
public class FabricHandledScreen
        extends AbstractContainerScreen<FabricScreenHandlerAdapter> {

    public FabricHandledScreen(FabricScreenHandlerAdapter menu, Inventory inventory,
            Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX,
            int mouseY) {
        // No background yet — R3c renders the texture.
    }
}
