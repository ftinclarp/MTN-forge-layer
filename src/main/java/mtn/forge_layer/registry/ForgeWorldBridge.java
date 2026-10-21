package mtn.forge_layer.registry;

import mtn.minecraft_layer.net.minecraft.tileentity.TileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Bridges a vanilla {@link Level} into the Forge-style
 * {@link mtn.minecraft_layer...World} API: resolves tile entities at a position
 * and reflects {@link Level#isClientSide()} into {@code isRemote}.
 */
public class ForgeWorldBridge extends mtn.minecraft_layer.net.minecraft.world.World {

    private final Level level;

    public ForgeWorldBridge(Level level) {
        this.level = level;
        this.isRemote = level.isClientSide();
    }

    @Override
    public TileEntity getTileEntity(int x, int y, int z) {
        net.minecraft.world.level.block.entity.BlockEntity be =
                level.getBlockEntity(new BlockPos(x, y, z));
        if (be instanceof FabricBlockEntityAdapter) {
            return ((FabricBlockEntityAdapter) be).getForgeTe();
        }
        return null;
    }

    /** Helpers for the TESR bridge. */
    public static ForgeWorldBridge fromClientLevel(Level level) {
        return new ForgeWorldBridge(level);
    }
}
