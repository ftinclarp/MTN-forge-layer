package mtn.forge_layer.cpw.mods.fml.common.registry;

import mtn.forge_layer.net.minecraft.block.Block;
import mtn.forge_layer.net.minecraft.item.Item;
import mtn.forge_layer.registry.ForgeBlockAdapter;
import mtn.forge_layer.registry.ForgeItemAdapter;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;

/**
 * Stand-in for {@code cpw.mods.fml.common.registry.GameRegistry}
 * (Forge 1.7.10). Unlike the earlier no-op, these methods perform real
 * registration: the dummy Forge block/item is wrapped in an adapter and
 * registered under {@code <forge-modid>:<name>}.
 */
public final class GameRegistry {

    /** Mod id of the ported {@code @Mod} currently being dispatched, set by FabricEntry. */
    public static String CURRENT_MOD_ID = "mtnexample";

    private GameRegistry() {
    }

    public static void registerBlock(Block forgeBlock, String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(CURRENT_MOD_ID, name);
        net.minecraft.world.level.block.Block fabricBlock = new ForgeBlockAdapter(forgeBlock, id);
        Registry.register(BuiltInRegistries.BLOCK, id, fabricBlock);

        // BlockItem so it can exist in inventory
        net.minecraft.world.item.Item blockItem = new BlockItem(fabricBlock, new net.minecraft.world.item.Item.Properties());
        Registry.register(BuiltInRegistries.ITEM, id, blockItem);
        System.out.println("[MTN-LAYER] registered block: " + id);
    }

    public static void registerItem(Item forgeItem, String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(CURRENT_MOD_ID, name);
        net.minecraft.world.item.Item fabricItem = new ForgeItemAdapter(forgeItem, new net.minecraft.world.item.Item.Properties());
        Registry.register(BuiltInRegistries.ITEM, id, fabricItem);
        System.out.println("[MTN-LAYER] registered item: " + id);
    }
}
