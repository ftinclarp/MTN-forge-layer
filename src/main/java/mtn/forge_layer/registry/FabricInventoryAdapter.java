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
        return forgeInv.getSizeInventory();
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < forgeInv.getSizeInventory(); i++) {
            mtn.minecraft_layer.net.minecraft.item.ItemStack stack = forgeInv.getStackInSlot(i);
            if (stack != null && !stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public net.minecraft.world.item.ItemStack getItem(int slot) {
        mtn.minecraft_layer.net.minecraft.item.ItemStack stack = forgeInv.getStackInSlot(slot);
        return stack == null ? net.minecraft.world.item.ItemStack.EMPTY : NbtBridge.toFabricStack(stack);
    }

    @Override
    public net.minecraft.world.item.ItemStack removeItem(int slot, int count) {
        mtn.minecraft_layer.net.minecraft.item.ItemStack stack = forgeInv.decrStackSize(slot, count);
        return stack == null ? net.minecraft.world.item.ItemStack.EMPTY : NbtBridge.toFabricStack(stack);
    }

    @Override
    public net.minecraft.world.item.ItemStack removeItemNoUpdate(int slot) {
        mtn.minecraft_layer.net.minecraft.item.ItemStack stack = forgeInv.getStackInSlotOnClosing(slot);
        return stack == null ? net.minecraft.world.item.ItemStack.EMPTY : NbtBridge.toFabricStack(stack);
    }

    @Override
    public void setItem(int slot, net.minecraft.world.item.ItemStack stack) {
        forgeInv.setInventorySlotContents(slot,
                (stack == null || stack.isEmpty()) ? null : NbtBridge.toForgeStack(stack));
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public void setChanged() {
        forgeInv.markDirty();
    }

    @Override
    public boolean stillValid(net.minecraft.world.entity.player.Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < forgeInv.getSizeInventory(); i++) {
            forgeInv.setInventorySlotContents(i, null);
        }
    }

    /**
     * Forge ItemStack -> Fabric ItemStack. Delegates to {@link NbtBridge}.
     */
    public static net.minecraft.world.item.ItemStack toFabric(
            mtn.minecraft_layer.net.minecraft.item.ItemStack forgeStack) {
        return NbtBridge.toFabricStack(forgeStack);
    }

    /**
     * Fabric ItemStack -> Forge ItemStack. Delegates to {@link NbtBridge}.
     */
    public static mtn.minecraft_layer.net.minecraft.item.ItemStack toForge(
            net.minecraft.world.item.ItemStack fabricStack) {
        return NbtBridge.toForgeStack(fabricStack);
    }

    public IInventory getForgeInv() {
        return forgeInv;
    }
}
