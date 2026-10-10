package mtn.forge_layer.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Wraps a dummy Forge {@code mtn.forge_layer.net.minecraft.block.Block}
 * as a real Fabric/vanilla {@link Block}, so it can be registered via
 * {@link net.minecraft.core.Registry}.
 */
public class ForgeBlockAdapter extends Block {
    private final mtn.forge_layer.net.minecraft.block.Block forgeBlock;
    private final ResourceLocation id;

    public ForgeBlockAdapter(
            mtn.forge_layer.net.minecraft.block.Block forgeBlock,
            ResourceLocation id) {
        super(BlockBehaviour.Properties.of().strength(1.0f));
        this.forgeBlock = forgeBlock;
        this.id = id;
    }

    public mtn.forge_layer.net.minecraft.block.Block getForgeBlock() {
        return forgeBlock;
    }

    public ResourceLocation getId() {
        return id;
    }
}
