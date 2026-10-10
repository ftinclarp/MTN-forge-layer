package mtn.forge_layer.registry;

import mtn.minecraft_layer.net.minecraft.inventory.IInventory;

/**
 * Bridges a Forge-1.7.10-style {@link IInventory} to a vanilla
 * {@link net.minecraft.world.Container} (Mojmap: the inventory interface),
 * so ported {@code IInventory} code can be exposed to real Fabric/vanilla
 * containerAndScreen interactions. Phase 2 fills the delegation bodies.
 */
public class FabricInventoryAdapter implements net.minecraft.world.Container {
    private final IInventory forgeInv;

    public FabricInventoryAdapter(IInventory forgeInv) {
        this.forgeInv = forgeInv;
    }

    @Override
    public int getContainerSize() {
        // TODO Phase 2: forgeInv.getSizeInventory()
        return 0;
    }

    @Override
    public boolean isEmpty() {
        // TODO Phase 2: sum of all slots empty
        return true;
    }

    @Override
    public net.minecraft.world.item.ItemStack getItem(int slot) {
        // TODO Phase 2: toFabric(forgeInv.getStackInSlot(slot))
        return null;
    }

    @Override
    public net.minecraft.world.item.ItemStack removeItem(int slot, int count) {
        // TODO Phase 2: toFabric(forgeInv.decrStackSize(slot, count))
        return null;
    }

    @Override
    public net.minecraft.world.item.ItemStack removeItemNoUpdate(int slot) {
        // TODO Phase 2: toFabric(forgeInv.getStackInSlotOnClosing(slot))
        return null;
    }

    @Override
    public void setItem(int slot, net.minecraft.world.item.ItemStack stack) {
        // TODO Phase 2: forgeInv.setInventorySlotContents(slot, toForge(stack))
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public void setChanged() {
        // TODO Phase 2: forgeInv.markDirty()
    }

    @Override
    public boolean stillValid(net.minecraft.world.entity.player.Player player) {
        // TODO Phase 2: forgeInv.isUseableByPlayer(forgePlayer)
        return true;
    }

    @Override
    public void clearContent() {
        // TODO Phase 2: iterate and remove
    }

    /**
     * Forge ItemStack -> Fabric ItemStack. Phase 2: full conversion with
     * count, damage and NBT preserved.
     */
    public static net.minecraft.world.item.ItemStack toFabric(
            mtn.minecraft_layer.net.minecraft.item.ItemStack forgeStack) {
        // TODO Phase 2
        return null;
    }

    /**
     * Fabric ItemStack -> Forge ItemStack. Phase 2: full conversion with
     * count, damage and NBT preserved.
     */
    public static mtn.minecraft_layer.net.minecraft.item.ItemStack toForge(
            net.minecraft.world.item.ItemStack fabricStack) {
        // TODO Phase 2
        return null;
    }

    public IInventory getForgeInv() {
        return forgeInv;
    }
}
