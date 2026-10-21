package mtn.forge_layer.registry;

import mtn.minecraft_layer.net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * A real Fabric {@link AbstractContainerScreen} that drives the ported Forge
 * {@link GuiContainer} (the @SidedProxy ClientProxy's {@code getClientGuiElement}),
 * so the Iron Chest's own {@code drawGuiContainerBackgroundLayer} runs — binding
 * the mod's own {@code textures/gui/<type>container.png} texture and its
 * non-vanilla slot layout.
 *
 * <p>The ported GUI is a pure-client {@code mtn.minecraft_layer...GuiContainer};
 * it knows its own {@code xSize}/{@code ySize} and its registered texture. The
 * Fabric side must (a) install the current {@link GuiGraphics} on the ported
 * GUI's {@link mtn.minecraft_layer.net.minecraft.client.Minecraft} shim, (b)
 * invoke its "draw background" hook, and (c) label the screen from the layered
 * container's inventory name.
 */
public class PortedChestScreen extends AbstractContainerScreen<FabricScreenHandlerAdapter> {

    private final GuiContainer portedGui;

    public PortedChestScreen(FabricScreenHandlerAdapter menu, Inventory inventory,
            Component title, GuiContainer portedGui) {
        super(menu, inventory, title);
        this.portedGui = portedGui;
        if (portedGui != null) {
            // Ported GuiContainer reads xSize/ySize for its background placement.
            this.imageWidth = portedGui.xSize;
            this.imageHeight = portedGui.ySize;
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX,
            int mouseY) {
        if (portedGui == null) {
            return;
        }
        // The ported drawGuiContainerBackgroundLayer binds its own texture and
        // calls drawTexturedModalRect; wire the current GuiGraphics into the
        // minecraft-layer Minecraft shim so those calls reach the real renderer.
        mtn.minecraft_layer.net.minecraft.client.Minecraft layeredMc =
                mtn.minecraft_layer.net.minecraft.client.Minecraft.getMinecraft();
        if (portedGui.mc == null) {
            portedGui.mc = layeredMc;
        }
        Object saved = layeredMc.getActiveGuiGraphics();
        layeredMc.setActiveGuiGraphics(guiGraphics);
        // Ported GUI uses screen-absolute x/y for the background (as 1.7.10 did),
        // so align its local origin with ours.
        int savedW = portedGui.width;
        int savedH = portedGui.height;
        portedGui.width = this.width;
        portedGui.height = this.height;
        try {
            portedGui.mtnDrawBackground(partialTick, mouseX, mouseY);
        } finally {
            layeredMc.setActiveGuiGraphics(saved);
            portedGui.width = savedW;
            portedGui.height = savedH;
        }
    }

    /**
     * The title the ported chest draws (used by {@link #renderLabels}); from the
     * chest inventory's name, falling back to the screen title.
     */
    private String effectiveTitle() {
        if (portedGui != null
                && portedGui.inventorySlots != null
                && !portedGui.inventorySlots.getInventorySlots().isEmpty()) {
            mtn.minecraft_layer.net.minecraft.inventory.IInventory inv =
                    portedGui.inventorySlots.getInventorySlots().get(0).inventory;
            if (inv != null) {
                return inv.getInventoryName();
            }
        }
        return this.title.getString();
    }
}
