package mtn.forge_layer.cpw.mods.fml.common.registry;

import mtn.minecraft_layer.net.minecraft.block.Block;
import mtn.minecraft_layer.net.minecraft.item.Item;
import mtn.minecraft_layer.net.minecraft.tileentity.TileEntity;
import mtn.forge_layer.registry.ForgeBlockAdapter;
import mtn.forge_layer.registry.ForgeItemAdapter;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.entity.BlockEntityType;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;

/**
 * Stand-in for {@code cpw.mods.fml.common.registry.GameRegistry}
 * (Forge 1.7.10). Unlike the earlier no-op, these methods perform real
 * registration: the dummy Forge block/item is wrapped in an adapter and
 * registered under {@code <forge-modid>:<name>}.
 */
public final class GameRegistry {

    /** Mod id of the ported {@code @Mod} currently being dispatched, set by FabricEntry. */
    public static String CURRENT_MOD_ID = "mtnexample";

    private static final java.util.Map<Class<?>, BlockEntityType<?>> BY_CLASS = new java.util.HashMap<>();
    private static final java.util.Map<String, BlockEntityType<?>> BY_ID = new java.util.HashMap<>();

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

    public static void registerTileEntity(
            Class<? extends TileEntity> teClass,
            String id) {
        try {
            // Instantiate a fresh Forge TE so each placed block gets its own instance.
            // FabricBlockEntityTypeBuilder.create's factory must return a NEW BlockEntity per call.
            BlockEntityType<?> type = FabricBlockEntityTypeBuilder
                    .<mtn.forge_layer.registry.FabricBlockEntityAdapter>create(
                            (pos, state) -> {
                                try {
                                    TileEntity fresh =
                                            (TileEntity) teClass.getDeclaredConstructor().newInstance();
                                    return new mtn.forge_layer.registry.FabricBlockEntityAdapter(pos, state, fresh);
                                } catch (ReflectiveOperationException e) {
                                    throw new RuntimeException(e);
                                }
                            })
                    .build();

            ResourceLocation resId = ResourceLocation.fromNamespaceAndPath(CURRENT_MOD_ID, id);
            registerIntoRegistry(resId, type);

            BY_CLASS.put(teClass, type);
            BY_ID.put(id, type);
            System.out.println("[MTN-LAYER] registerTileEntity: " + id
                    + " (" + teClass.getSimpleName() + ")");
        } catch (RuntimeException e) {
            System.out.println("[MTN-LAYER] registerTileEntity FAILED for " + id
                    + " (" + teClass.getSimpleName() + "): " + e.getMessage());
        }
    }

    /** Registers a block entity type under the current mod id. */
    private static void registerIntoRegistry(
            ResourceLocation r, BlockEntityType<?> t) {
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, r, t);
    }

    public static void registerTileEntity(
            Class<? extends TileEntity> teClass,
            String id, String legacyId) {
        registerTileEntity(teClass, id);
    }
}
