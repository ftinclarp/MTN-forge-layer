package mtn.forge_layer.registry;

import mtn.minecraft_layer.net.minecraft.nbt.NBTTagCompound;
import mtn.minecraft_layer.net.minecraft.tileentity.TileEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

/**
 * Bridges a Forge-1.7.10-style {@link TileEntity} to a vanilla
 * {@link net.minecraft.world.level.block.entity.BlockEntity}, so ported
 * {@code TileEntity} code runs inside the real Fabric/vanilla world.
 * Phase 2 fills the delegation bodies (position sync, tick, NBT).
 */
public class FabricBlockEntityAdapter
        extends net.minecraft.world.level.block.entity.BlockEntity {
    private final TileEntity forgeTe;

    public FabricBlockEntityAdapter(
            net.minecraft.world.level.block.entity.BlockEntityType<?> type,
            net.minecraft.core.BlockPos pos,
            net.minecraft.world.level.block.state.BlockState state,
            TileEntity forgeTe) {
        super(type, pos, state);
        this.forgeTe = forgeTe;
        // There is no vanilla World on the forge side yet; leave null until the
        // block's world is wired in.
        this.forgeTe.worldObj = null;
        this.forgeTe.xCoord = pos.getX();
        this.forgeTe.yCoord = pos.getY();
        this.forgeTe.zCoord = pos.getZ();
    }

    /**
     * Invoked by the block's ticker. Delegates to the Forge
     * {@code TileEntity.updateEntity()}.
     */
    public void tick() {
        forgeTe.updateEntity();
    }

    @Override
    public void loadAdditional(net.minecraft.nbt.CompoundTag nbt,
            net.minecraft.core.HolderLookup.Provider registryLookup) {
        super.loadAdditional(nbt, registryLookup);
        // toForge reads all keys of the fabric tag into a Forge NBTTagCompound;
        // wiring only the mod's own subtree is best-effort and acceptable for R2.
        forgeTe.readFromNBT(NbtBridge.toForge(nbt));
    }

    @Override
    public void saveAdditional(net.minecraft.nbt.CompoundTag nbt,
            net.minecraft.core.HolderLookup.Provider registryLookup) {
        super.saveAdditional(nbt, registryLookup);
        // Build the Forge side first, then MERGE its keys into the Fabric nbt so
        // vanilla metadata written by super is preserved.
        NBTTagCompound forgeNbt = new NBTTagCompound();
        forgeTe.writeToNBT(forgeNbt);
        CompoundTag fabric = NbtBridge.toFabric(forgeNbt);
        for (String key : fabric.getAllKeys()) {
            Tag value = fabric.get(key);
            nbt.put(key, value);
        }
    }

    @Override
    public void setChanged() {
        forgeTe.markDirty();
        super.setChanged();
    }

    public TileEntity getForgeTe() {
        return forgeTe;
    }
}
