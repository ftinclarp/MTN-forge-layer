package mtn.forge_layer.registry;

import mtn.minecraft_layer.net.minecraft.tileentity.TileEntity;

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
            net.minecraft.core.BlockPos pos,
            net.minecraft.world.level.block.state.BlockState state,
            TileEntity forgeTe) {
        super(null, pos, state);
        this.forgeTe = forgeTe;
        // TODO Phase 2: sync world/coords into forgeTe
    }

    /**
     * Called by the block's ticker. Phase 2: delegating tick().
     */
    public void tick() {
        // TODO Phase 2: forgeTe.updateEntity()
    }

    @Override
    public void loadAdditional(net.minecraft.nbt.CompoundTag nbt,
            net.minecraft.core.HolderLookup.Provider registryLookup) {
        super.loadAdditional(nbt, registryLookup);
        // TODO Phase 2: forgeTe.readFromNBT(NbtBridge.toForge(nbt))
    }

    @Override
    public void saveAdditional(net.minecraft.nbt.CompoundTag nbt,
            net.minecraft.core.HolderLookup.Provider registryLookup) {
        super.saveAdditional(nbt, registryLookup);
        // TODO Phase 2: NbtBridge.toFabric(forgeTe.writeToNBT(...))
    }

    @Override
    public void setChanged() {
        // TODO Phase 2: forgeTe.markDirty(); super.setChanged()
        super.setChanged();
    }

    public TileEntity getForgeTe() {
        return forgeTe;
    }
}
