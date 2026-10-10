package mtn.forge_layer.registry;

import net.minecraft.world.item.Item;

/**
 * Wraps a dummy Forge {@code mtn.forge_layer.net.minecraft.item.Item}
 * as a real vanilla {@link Item}, so it can be registered via
 * {@link net.minecraft.core.Registry}.
 */
public class ForgeItemAdapter extends Item {
    private final mtn.forge_layer.net.minecraft.item.Item forgeItem;

    public ForgeItemAdapter(
            mtn.forge_layer.net.minecraft.item.Item forgeItem,
            Item.Properties properties) {
        super(properties);
        this.forgeItem = forgeItem;
    }

    public mtn.forge_layer.net.minecraft.item.Item getForgeItem() {
        return forgeItem;
    }
}
