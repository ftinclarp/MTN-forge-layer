package mtn.forge_layer.cpw.mods.fml.common.registry;

import mtn.minecraft_layer.net.minecraft.block.Block;
import mtn.minecraft_layer.net.minecraft.item.Item;
import mtn.minecraft_layer.net.minecraft.item.ItemStack;
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
    private static final java.util.List<ForgeBlockAdapter> REGISTERED_BLOCKS =
            new java.util.ArrayList<>();

    private GameRegistry() {
    }

    /** Public accessor for a pre-registered block entity type by tile class. */
    public static BlockEntityType<?> blockEntityTypeFor(
            Class<? extends TileEntity> teClass) {
        return BY_CLASS.get(teClass);
    }

    /** Public accessor for a pre-registered block entity type by id string. */
    public static BlockEntityType<?> blockEntityTypeFor(String id) {
        return BY_ID.get(id);
    }

    /** Real Forge 1.7.10 signature: returns the registered Block. */
    public static Block registerBlock(Block forgeBlock, String name) {
        return registerBlock(forgeBlock, mtn.minecraft_layer.net.minecraft.item.ItemBlock.class, name);
    }

    /** Real Forge 1.7.10 signature: {@code registerBlock(Block, Class<? extends ItemBlock>, String)}. */
    public static Block registerBlock(Block forgeBlock,
            Class<? extends mtn.minecraft_layer.net.minecraft.item.ItemBlock> itemclass,
            String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(CURRENT_MOD_ID, name);
        ForgeBlockAdapter adapter = new ForgeBlockAdapter(forgeBlock, id);
        Registry.register(BuiltInRegistries.BLOCK, id, adapter);
        REGISTERED_BLOCKS.add(adapter);

        // BlockItem so it can exist in inventory
        net.minecraft.world.item.Item blockItem = new BlockItem(adapter, new net.minecraft.world.item.Item.Properties());
        Registry.register(BuiltInRegistries.ITEM, id, blockItem);
        System.out.println("[MTN-LAYER] registered block: " + id);
        return forgeBlock;
    }

    /** Real Forge 1.7.10 signature: returns Item. */
    public static Item registerItem(Item forgeItem, String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(CURRENT_MOD_ID, name);
        net.minecraft.world.item.Item fabricItem = new ForgeItemAdapter(forgeItem, new net.minecraft.world.item.Item.Properties());
        Registry.register(BuiltInRegistries.ITEM, id, fabricItem);
        System.out.println("[MTN-LAYER] registered item: " + id);
        return forgeItem;
    }

    public static void registerTileEntity(
            Class<? extends TileEntity> teClass,
            String id) {
        try {
            // Find the adapter(s) whose Forge block produces this TE class, so the
            // BlockEntityType's validBlocks include the owning block. Without it,
            // vanilla BlockEntity.validateSupports rejects placement.
            java.util.List<ForgeBlockAdapter> owning = new java.util.ArrayList<>();
            for (ForgeBlockAdapter adapter : REGISTERED_BLOCKS) {
                mtn.minecraft_layer.net.minecraft.block.Block fb = adapter.getForgeBlock();
                if (!(fb instanceof mtn.minecraft_layer.net.minecraft.block.BlockContainer bc)) {
                    continue;
                }
                mtn.minecraft_layer.net.minecraft.tileentity.TileEntity probe =
                        bc.createNewTileEntity(new mtn.minecraft_layer.net.minecraft.world.World(), 0);
                if (probe != null && probe.getClass().equals(teClass)) {
                    owning.add(adapter);
                }
            }
            net.minecraft.world.level.block.Block[] validBlocks =
                    new net.minecraft.world.level.block.Block[owning.size()];
            for (int i = 0; i < owning.size(); i++) {
                validBlocks[i] = owning.get(i);
            }

            // Instantiate a fresh Forge TE so each placed block gets its own instance.
            // FabricBlockEntityTypeBuilder.create's factory must return a NEW BlockEntity per call.
            final net.minecraft.world.level.block.entity.BlockEntityType<?>[] holder =
                    new net.minecraft.world.level.block.entity.BlockEntityType<?>[1];
            BlockEntityType<?> type = FabricBlockEntityTypeBuilder
                    .<mtn.forge_layer.registry.FabricBlockEntityAdapter>create(
                            (pos, state) -> {
                                try {
                                    TileEntity fresh =
                                            (TileEntity) teClass.getDeclaredConstructor().newInstance();
                                    return new mtn.forge_layer.registry.FabricBlockEntityAdapter(
                                            holder[0], pos, state, fresh);
                                } catch (ReflectiveOperationException e) {
                                    throw new RuntimeException(e);
                                }
                            },
                            validBlocks)
                    .build();

            holder[0] = type;

            ResourceLocation resId = ResourceLocation.fromNamespaceAndPath(CURRENT_MOD_ID, id);
            registerIntoRegistry(resId, type);

            BY_CLASS.put(teClass, type);
            BY_ID.put(id, type);

            // Give the owning adapters their type so placement resolves instantly.
            for (ForgeBlockAdapter adapter : owning) {
                adapter.setBlockEntityType(type);
            }

            System.out.println("[MTN-LAYER] registerTileEntity: " + id
                    + " (" + teClass.getSimpleName() + ") blocks=" + owning.size());
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

    /** Forge 1.7.10: register a TE with both a namespaced and legacy id. */
    public static void registerTileEntityWithAlternatives(
            Class<? extends TileEntity> teClass,
            String id, String legacyId) {
        registerTileEntity(teClass, id);
        // TODO(mtn-layer): also index the legacy id so old saves keep working.
    }

    /**
     * Forge 1.7.10: register a named custom itemstack (creative/NEI lists).
     * Stub — safe no-op.
     */
    public static void registerCustomItemStack(String name, ItemStack stack) {
        // TODO(mtn-layer): add the stack to the custom-item common list.
    }
}
