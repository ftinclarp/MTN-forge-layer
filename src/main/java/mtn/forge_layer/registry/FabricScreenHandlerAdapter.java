package mtn.forge_layer.registry;

import mtn.minecraft_layer.net.minecraft.inventory.Container;
import mtn.minecraft_layer.net.minecraft.inventory.IInventory;
import mtn.minecraft_layer.net.minecraft.inventory.Slot;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Bridges a layered (Forge 1.7.10-style) {@link Container} onto Fabric's
 * {@link AbstractContainerMenu}. The forge container supplies the slot layout;
 * each forge slot is re-created as a vanilla {@link net.minecraft.world.inventory.Slot}
 * backed by a {@link FabricInventoryAdapter} so click mutations flow through the
 * normal menu machinery.
 *
 * <p>The vanilla {@code MenuType} is bypassed (its constructors are inaccessible in
 * 1.21.1), so this menu is constructed with a {@code null} MenuType and a custom
 * sync id. Subclasses of {@code AbstractContainerMenu} must implement
 * {@code stillValid} and {@code quickMoveStack}; both are no-ops here.
 */
public class FabricScreenHandlerAdapter extends AbstractContainerMenu {

    private final Container forgeContainer;
    private final Map<IInventory, FabricInventoryAdapter> adapters = new HashMap<>();

    public FabricScreenHandlerAdapter(int syncId, Container forgeContainer) {
        super(null, syncId);
        this.forgeContainer = forgeContainer;
        for (Slot forgeSlot : forgeContainer.getInventorySlots()) {
            FabricInventoryAdapter inv = adapters.computeIfAbsent(forgeSlot.inventory,
                    FabricInventoryAdapter::new);
            addSlot(new net.minecraft.world.inventory.Slot(inv, forgeSlot.slotIndex,
                    forgeSlot.xDisplayPosition, forgeSlot.yDisplayPosition));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    public Container getForgeContainer() {
        return forgeContainer;
    }
}
