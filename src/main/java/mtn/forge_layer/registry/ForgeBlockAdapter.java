package mtn.forge_layer.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Wraps a dummy Forge {@code mtn.minecraft_layer.net.minecraft.block.Block}
 * as a real Fabric/vanilla {@link Block}, so it can be registered via
 * {@link net.minecraft.core.Registry}. Right-clicks are forwarded to the
 * Forge block's {@code onBlockActivated}.
 */
public class ForgeBlockAdapter extends Block implements EntityBlock {
    private final mtn.minecraft_layer.net.minecraft.block.Block forgeBlock;
    private final ResourceLocation id;

    public ForgeBlockAdapter(
            mtn.minecraft_layer.net.minecraft.block.Block forgeBlock,
            ResourceLocation id) {
        super(BlockBehaviour.Properties.of().strength(1.0f));
        this.forgeBlock = forgeBlock;
        this.id = id;
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level,
            BlockPos pos, Player player, BlockHitResult hitResult) {
        ForgeWorldBridge forgeWorld = new ForgeWorldBridge(level);
        ForgePlayerBridge forgePlayer = new ForgePlayerBridge(player);
        int side = hitResult.getDirection().get3DDataValue();
        float hitX = (float) (hitResult.getLocation().x - pos.getX());
        float hitY = (float) (hitResult.getLocation().y - pos.getY());
        float hitZ = (float) (hitResult.getLocation().z - pos.getZ());
        boolean handled = forgeBlock.onBlockActivated(forgeWorld, pos.getX(), pos.getY(),
                pos.getZ(), forgePlayer, side, hitX, hitY, hitZ);
        return handled ? InteractionResult.sidedSuccess(level.isClientSide)
                : InteractionResult.PASS;
    }

    @Override
    public net.minecraft.world.level.block.entity.BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (!(forgeBlock instanceof mtn.minecraft_layer.net.minecraft.block.BlockContainer)) {
            return null;
        }
        net.minecraft.world.level.block.entity.BlockEntityType<?> type = blockEntityType();
        mtn.minecraft_layer.net.minecraft.tileentity.TileEntity te =
                ((mtn.minecraft_layer.net.minecraft.block.BlockContainer) forgeBlock)
                        .createNewTileEntity(new mtn.minecraft_layer.net.minecraft.world.World(), 0);
        return new FabricBlockEntityAdapter(type, pos, state, te);
    }

    /** Lazily builds and caches the block entity type valid for this block. */
    private volatile net.minecraft.world.level.block.entity.BlockEntityType<?> cachedType;

    private net.minecraft.world.level.block.entity.BlockEntityType<?> blockEntityType() {
        net.minecraft.world.level.block.entity.BlockEntityType<?> t = cachedType;
        if (t != null) {
            return t;
        }
        final mtn.minecraft_layer.net.minecraft.block.Block bc = forgeBlock;
        final Block self = this;
        // The factory runs at placement time (after cachedType is assigned below).
        net.minecraft.world.level.block.entity.BlockEntityType<?> type =
                net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder
                        .create(
                                (pos, state) -> new FabricBlockEntityAdapter(
                                        cachedType, pos, state,
                                        ((mtn.minecraft_layer.net.minecraft.block.BlockContainer) bc)
                                                .createNewTileEntity(
                                                        new mtn.minecraft_layer.net.minecraft.world.World(), 0)),
                                self)
                        .build();
        this.cachedType = type;
        return type;
    }

    public mtn.minecraft_layer.net.minecraft.block.Block getForgeBlock() {
        return forgeBlock;
    }

    public ResourceLocation getId() {
        return id;
    }
}
