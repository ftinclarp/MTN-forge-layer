package mtn.forge_layer.cpw.mods.fml.common.registry;

import mtn.forge_layer.net.minecraft.block.Block;
import mtn.forge_layer.net.minecraft.item.Item;

/**
 * Dummy stand-in for {@code cpw.mods.fml.common.registry.GameRegistry}
 * (Forge 1.7.10). Registration is a no-op: it only prints a trace line so the
 * pipeline can prove the call was dispatched.
 */
public final class GameRegistry {
    private GameRegistry() {
    }

    public static void registerBlock(Block block, String name) {
        System.out.println("[MTN-LAYER] registerBlock: " + name);
    }

    public static void registerItem(Item item, String name) {
        System.out.println("[MTN-LAYER] registerItem: " + name);
    }
}
